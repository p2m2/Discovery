package inrae.semantic_web
import wvlet.log.Logger.rootLogger._
import java.util.UUID.randomUUID

import inrae.semantic_web.internal.{Node, Root, Something, pm}
import inrae.semantic_web.QueryPlanner.{AND_RESULTS_SET, INTERSECTION_RESULTS_SET, ORDONNANCEMENT_RESULTS_SET, OR_RESULTS_SET}
import inrae.semantic_web.rdf.IRI
import inrae.semantic_web.sparql.{QueryResult, _}

import scala.annotation.tailrec
import scala.concurrent.{Future, Promise}
import scala.util.Success

object QueryPlannerExecutor {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def executePlanning( root : Root,
                       rs : ORDONNANCEMENT_RESULTS_SET,
                       listVariables : Seq[String],
                       config : StatementConfiguration,
                       prefixes : Map[String,IRI]): Future[QueryResult] = {
    rs match {
      //case or: OR_RESULTS_SET =>
      //case and: AND_RESULTS_SET =>
      case bgps: INTERSECTION_RESULTS_SET =>executeSet(root,rs,listVariables,config,prefixes)
      case _ => Future { QueryResult("null") }
    }
  }


  def buildRootNode( swRootNode : Root, lbgp : Seq[Node]) : Node = {
    trace("buildRootNode lbgp=>"+lbgp)
    if ( lbgp.length == 0 ) {
      Something("__var"+randomUUID.toString)
    } else if ( lbgp.length == 1 ) {
      lbgp(0)
    } else {
      lbgp(0).addChildren(buildRootNode(swRootNode,lbgp.drop(1)))
    }
  }

  def executeSet(root : Root,
                 rs : ORDONNANCEMENT_RESULTS_SET,
                 listVariables : Seq[String],
                 config : StatementConfiguration,
                 prefixes : Map[String,IRI]) : Future[QueryResult] = {

    val promise = Promise[QueryResult]()

    rs match {
      case or: OR_RESULTS_SET => {

        /* union des resultats */
        Future.sequence(or.lbgp.map( executeSet(root,_,listVariables,config,prefixes))).onComplete( {
          case Success(lQueryResu) => {
            promise success ( lQueryResu(0)) // todo => union
          }
          case msg => {
            error(msg)
            promise success (QueryResult("null"))
          }
        })
        promise.future
      }
      case and: AND_RESULTS_SET => {
        /* intersection des resultats */
        //and.lbgp.map( executeSet(_,listVariables,config) )
        Future.sequence(and.lbgp.map( executeSet(root,_,listVariables,config,prefixes))).onComplete( {
          case Success(lQueryResu) => {
            promise success ( lQueryResu(0)) // todo => intersection
          }
          case msg => {
            error(msg)
            promise success (QueryResult("null"))
          }
        })
        promise.future
      }
      case bgps: INTERSECTION_RESULTS_SET =>
        // pour l'instant pas d'ordonnancement sur les sources
        /* piste d optimisation : trouver les resultats les plus limitants ... */
        for ((source,lbgp) <- bgps.lns) {
          /* reconstruction d'une requete au format easySparql */
          // todo : Verifier qu'on ne casse jamais de lien de parenté
          var r :Root = Root()
          r.addChildren(buildRootNode(root,lbgp))
          trace(r.toString())
          val refToIdentifier = pm.SparqlGenerator.correspondenceVariablesIdentifier(root)
            ._1.view.filterKeys( k => listVariables.contains(k) ).toMap
          val qr = QueryRunner(config.source(source),config.conf.settings).query(
            SparqlQueryBuilder.selectQueryString(r,refToIdentifier,refToIdentifier.values.toSeq)
          )
        }
        promise success (QueryResult("",""))
        promise.future
    }
  }

}
