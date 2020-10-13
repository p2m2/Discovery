package inrae.semantic_web

import inrae.semantic_web.internal._
import inrae.semantic_web.rdf.{Literal, RdfType, URI}
import inrae.semantic_web.sparql.{QueryResult, _}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}


object QueryManager {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  /**
   * Find source available in configuration for this node
   * @param n
   */

  def queryNode(rootRequest : Node, n: Node, config : StatementConfiguration) : Future[QueryResult] = {
    val (refToIdentifier,_) = pm.SparqlGenerator.setAllVariablesIdentifiers(n)
    queryVariables(rootRequest,refToIdentifier.values.toSeq,config)
  }

  def queryPropertyNode(rootRequest : Node, n: Node, config : StatementConfiguration) : Future[Seq[URI]] = {
    Future {
      Seq[URI]()
    }
  }
  def queryAll(rootRequest : Node,n: Node, config : StatementConfiguration) : Future[QueryResult] = {
    queryVariables(rootRequest,rootRequest.references(),config)
  }

  def countNbSolutions(n : Node,  config : StatementConfiguration) : Future[Option[RdfType]] = {

    if (config.sources().length == 0) {
      throw new Exception(" ** None sources available ** ")
    } else if (config.sources().length == 1) {
      val source = config.sources()(0)
      val (refToIdentifier, _) = pm.SparqlGenerator.setAllVariablesIdentifiers(n)
      val query = pm.SparqlGenerator.prologSourcesSelection()  +
        pm.SparqlGenerator.body(source, n, refToIdentifier) +
        pm.SparqlGenerator.solutionModifier()

      val res: Future[QueryResult] = QueryRunner(source).query(query)
    /*
      Future {
        val y = res.onComplete {
          case Success(count) => count.get.row(0).key("COUNT")
          case _ => None
        }
        y
      }*/
      res.map(v => v.get.row(0).key("COUNT"))
    } else {
      // todo query planner
      throw new Exception("not manage.......")
    }
  }

  def queryVariables(n: Node, listVariables : Seq[String], config : StatementConfiguration) : Future[QueryResult] = {
    if (config.sources().length == 0) {
      throw new Exception(" ** None sources available ** ")
    } else if (config.sources().length == 1) {
      queryOnSource(n,listVariables,config.sources()(0))
    } else {
      // todo query planner
        throw new Exception("not manage.......")
    }
  }

  def queryOnSource(n: Node, listVariables : Seq[String],  source : ConfigurationObject.Source): Future[QueryResult] = {
    val (refToIdentifier,_) = pm.SparqlGenerator.setAllVariablesIdentifiers(n)
    val query = pm.SparqlGenerator.prolog(listVariables) + "\n" +
                pm.SparqlGenerator.body(source, n, refToIdentifier) +
                pm.SparqlGenerator.solutionModifier()

    QueryRunner(source).query(query)
  }

  /**
   * Assign a list of source (existence of a remote persistence) if possible
   * @param n
   * @param source
   * @return
   */
  def setUpSourcesNode(n: Node, config : StatementConfiguration): Future[Node] = {
    println("---------------- setUpSourcesNode ----------------------")
    n match {
      case _ : SubjectOf | _: ObjectOf | _: LinkTo | _: LinkFrom =>
        val query = pm.SparqlGenerator.prologSourcesSelection() +
                    pm.SparqlGenerator.sparqlNode(n,"varUp","varCur") +
                    pm.SparqlGenerator.solutionModifierSourcesSelection()

        //println(" ----- query --------")
        //println(query)


        val nbRowResultsBySource : Seq[Future[Boolean]] = {
          config.sources().map(
              source => QueryRunner(source).query(query)
            ).map( {
              ( _.map(rr => rr.get.rows.length>0))
            })
          }
        val y2 = Future.sequence(nbRowResultsBySource)
        val y3 = Promise[Node]()

        y2.onComplete {
          case Success(lCheck) => y3 success (new SourcesNode(n, lCheck.zip(config.sources()).filter( _._1).map( _._2.id)))
        }

        y3.future
        //val sources : Seq[(String,Int)] = config.sources().map(_.id).zip(nbRowResultsBySource)
        //new SourcesNode(n,sources)

      case _ => Future(n)
    }
  }
}
