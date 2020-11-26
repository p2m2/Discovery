package inrae.semantic_web

import inrae.semantic_web.QueryPlanner
import inrae.semantic_web.internal._
import inrae.semantic_web.rdf.{IRI, Literal, SparqlBuilder, SparqlDefinition, URI}
import inrae.semantic_web.sparql.{QueryResult, _}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}


object QueryManager {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  /**
   * Find source available in configuration for this node
   * @param n
   */

  def sparql_string(root: Root, n: Node): String = {
    val (refToIdentifier,_) = pm.SparqlGenerator.correspondenceVariablesIdentifier(n)
    SparqlQueryBuilder.queryString(root,refToIdentifier,refToIdentifier.values.toSeq,root.prefixes)
  }

  def queryPropertyNode(rootRequest : Node, n: Node, config : StatementConfiguration) : Future[Seq[URI]] = {
    Future {
      Seq[URI]()
    }
  }
  def queryAll(rootRequest : Root,
               config : StatementConfiguration) : Future[QueryResult] = {
    scribe.debug("queryAll")
    queryVariables(rootRequest,Node.references(rootRequest),config)
  }

  def countNbSolutions(root : Root,  config : StatementConfiguration) : Future[Int] = {
    scribe.debug("countNbSolutions")

    if (config.sources().length == 0) {
      throw new Exception(" ** None sources available ** ")
    } else if (config.sources().length == 1) {
      val source = config.sources()(0)
      val (refToIdentifier, _) = pm.SparqlGenerator.correspondenceVariablesIdentifier(root)
      val varCount = "count"
     // val prolog = pm.SparqlGenerator.prologCountSelection(varCount,refToIdentifier(Node.references(n)))
     val prolog = pm.SparqlGenerator.prologCountSelection(varCount)


      val query =
        pm.SparqlGenerator.prefixes(root.prefixes) +
        prolog +
        pm.SparqlGenerator.body(root, refToIdentifier) +
        pm.SparqlGenerator.solutionModifier()

      val res: Future[QueryResult] = QueryRunner(source).query(query)
      res.map(v => {
        SparqlBuilder.createLiteral(v.json("results")("bindings")(0)(varCount)).toInt()
      })
    } else {
      // todo query planner
      scribe.error("QueryPlanner is not available .")
      throw new Exception("not manage.......")
    }
  }

  def queryVariables(root: Root,
                     listVariables : Seq[String],
                     config : StatementConfiguration) : Future[QueryResult] = {
    scribe.debug("queryVariables")
    if (config.sources().length == 0) {
      throw new Exception(" ** None sources available ** ")
    } else if (config.sources().length == 1) {
      val (refToIdentifier,_) = pm.SparqlGenerator.correspondenceVariablesIdentifier(root)

      scribe.info(refToIdentifier.toString())
      val query : String = SparqlQueryBuilder.queryString(root,refToIdentifier, listVariables, root.prefixes)
      scribe.info(query)
      QueryRunner(config.sources()(0)).query(query)
    } else {

      val plan = QueryPlanner.buildPlanning(root)//,listVariables,config)
      val plan_results_set = QueryPlanner.ordonnanceBySource(plan,root)
      QueryPlannerExecutor.executePlanning(root,plan_results_set,listVariables,config,root.prefixes)
    }
  }

  /**
   * Assign a list of source (existence of a remote persistence) if possible
   * @param n
   * @param source
   * @return
   */
  def setUpSourcesNode(n: Node,
                       config : StatementConfiguration,
                       prefixes : Map[String,IRI]): Future[Option[SourcesNode]] = {

    n match {
      case s : Something =>
        /* Something is Everywhere !! */
        Future {
          Some(SourcesNode(s.reference(),config.sources().map( _.id )))
        }
      case r : RdfNode =>
        val (refToIdentifier,_) = pm.SparqlGenerator.correspondenceVariablesIdentifier(n)
        val query = pm.SparqlGenerator.prefixes(prefixes) + "\n" +
          pm.SparqlGenerator.prologSourcesSelection() + "\n" +
          pm.SparqlGenerator.sparqlNode(r,refToIdentifier,"varUp","varCur") +
          pm.SparqlGenerator.solutionModifierSourcesSelection()

        scribe.debug(query)

        val nbRowResultsBySource : Seq[Future[Boolean]] = {
          config.sources().map(
              source => QueryRunner(source).query(query)
            ).map( {
              ( _.map(rr => rr.json("results")("bindings").arr.length>0))
            })
          }

        val y2 = Future.sequence(nbRowResultsBySource)
        val y3 = Promise[Option[SourcesNode]]()

        y2.onComplete {
          case Success(lCheck) => {
            y3 success Some(SourcesNode(r.reference(), lCheck.zip(config.sources()).filter( _._1).map( _._2.id)))
          }
          case msg => {
            System.err.println(msg)
            y3 success (None)
          }
        }

        y3.future

      case _ => Future(None)
    }
  }
}
