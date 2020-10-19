package inrae.semantic_web

import inrae.semantic_web.QueryPlanner
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

  def queryNode(rootRequest : Root, n: Node, config : StatementConfiguration,prefixes : Map[String,String]) : Future[QueryResult] = {
    val (refToIdentifier,_) = pm.SparqlGenerator.setAllVariablesIdentifiers(n)
    queryVariables(rootRequest,refToIdentifier.values.toSeq,config,prefixes)
  }

  def queryPropertyNode(rootRequest : Node, n: Node, config : StatementConfiguration) : Future[Seq[URI]] = {
    Future {
      Seq[URI]()
    }
  }
  def queryAll(rootRequest : Root,
               config : StatementConfiguration,
               prefixes : Map[String,String]) : Future[QueryResult] = {
    queryVariables(rootRequest,rootRequest.references(),config,prefixes)
  }

  def countNbSolutions(n : Node,  config : StatementConfiguration,prefixes : Map[String,String]) : Future[Option[RdfType]] = {

    if (config.sources().length == 0) {
      throw new Exception(" ** None sources available ** ")
    } else if (config.sources().length == 1) {
      val source = config.sources()(0)
      val (refToIdentifier, _) = pm.SparqlGenerator.setAllVariablesIdentifiers(n)
      val varCount = "count"
      val prolog = {
        n.reference() match {
          case Some(r) => pm.SparqlGenerator.prologCountSelection(varCount,refToIdentifier(r))
          case None => pm.SparqlGenerator.prologCountSelection(varCount)
        }
      }

      val query =
        pm.SparqlGenerator.prefixes(prefixes) +
        prolog +
        pm.SparqlGenerator.body(source, n, refToIdentifier) +
        pm.SparqlGenerator.solutionModifier()

      scribe.info(query)

      val res: Future[QueryResult] = QueryRunner(source).query(query)
      res.map(v => v.get.row(0).key(varCount))
    } else {
      // todo query planner
      scribe.error("QueryPlanner is not available .")
      throw new Exception("not manage.......")
    }
  }

  def queryVariables(root: Root,
                     listVariables : Seq[String],
                     config : StatementConfiguration,
                     prefixes : Map[String,String]) : Future[QueryResult] = {
    if (config.sources().length == 0) {
      throw new Exception(" ** None sources available ** ")
    } else if (config.sources().length == 1) {
      queryOnSource(root,listVariables,config.sources()(0),prefixes)
    } else {
      //
      val plan = QueryPlanner.buildPlanning(root)//,listVariables,config)
      println(" ----------------------- PLAN -----------------------------")
      println(plan)
      //QueryManager.executePlan(plan)
      Future {
        QueryResult(null)
      }
    }
  }

  def queryOnSource(n: Node,
                    listVariables : Seq[String],
                    source : ConfigurationObject.Source,
                    prefixes : Map[String,String]): Future[QueryResult] = {
    val (refToIdentifier,_) = pm.SparqlGenerator.setAllVariablesIdentifiers(n)
    val query = pm.SparqlGenerator.prefixes(prefixes)+"\n" +
        pm.SparqlGenerator.prolog(listVariables) + "\n" +
        pm.SparqlGenerator.body(source, n, refToIdentifier) +
        pm.SparqlGenerator.solutionModifier()

    QueryRunner(source).query(query)
  }

  //def executePlan() : Future[QueryResult] = {

  //}

  /**
   * Assign a list of source (existence of a remote persistence) if possible
   * @param n
   * @param source
   * @return
   */
  def setUpSourcesNode(n: Node,
                       config : StatementConfiguration,
                       prefixes : Map[String,String]): Future[Node] = {

    n match {
      case _ : Something =>
        /* Something is Everywhere !! */
        Future {
          new SourcesNode(n,config.sources().map( _.id ))
        }
      case _ : SubjectOf | _: ObjectOf | _: LinkTo | _: LinkFrom =>
        val query = pm.SparqlGenerator.prefixes(prefixes) + "\n" +
          pm.SparqlGenerator.prologSourcesSelection() + "\n" +
          pm.SparqlGenerator.sparqlNode(n,"varUp","varCur") +
          pm.SparqlGenerator.solutionModifierSourcesSelection()

        scribe.debug(query)

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
          case Success(lCheck) => {
            y3 success (new SourcesNode(n, lCheck.zip(config.sources()).filter( _._1).map( _._2.id)))
          }
          case msg => {
            System.err.println(msg)
            y3 success (n)
          }
        }

        y3.future

      case _ => Future(n)
    }
  }
}
