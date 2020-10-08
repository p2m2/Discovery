package inrae.semantic_web

import inrae.semantic_web.internal.{pm,Node}
import inrae.semantic_web.sparql._

import scala.concurrent.Future
import scala.util.Try


object QueryManager {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def queryNode(rootRequest : Node, n: Node, config : StatementConfiguration) : Future[QueryResult] = {
    val (refToIdentifier,_) = pm.SparqlGenerator.getAllVariablesIdentifiers(n)

    queryVariables(rootRequest,refToIdentifier.values.toSeq,config)
  }

  def queryAll(rootRequest : Node,n: Node, config : StatementConfiguration) : Future[QueryResult] = {
    queryVariables(rootRequest,rootRequest.references(),config)
  }

  def queryVariables(n: Node, listVariables : Seq[String], config : StatementConfiguration) : Future[QueryResult] = {
    println("------- queryVariables -----------")
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
    println("------- queryOnSource -----------")
    val (refToIdentifier,_) = pm.SparqlGenerator.getAllVariablesIdentifiers(n)
    val query = pm.SparqlGenerator.prolog(listVariables) + "\n" +
                pm.SparqlGenerator.body(source, n, refToIdentifier) +
                pm.SparqlGenerator.solutionModifier()

    QueryRunner(source).query(query)
  }

  def testNode(n: Node, source : ConfigurationObject.Source): Future[Boolean] = {
    println("------- testNode -----------")
    val (refToIdentifier,_) = pm.SparqlGenerator.getAllVariablesIdentifiers(n)


    n.reference() match {
      case Some(value) => {
        val variableIdentifier : String = refToIdentifier(value)
        val query =  pm.SparqlGenerator.prologSourcesSelection(variableIdentifier) +
        pm.SparqlGenerator.sparqlNode(n,"varUp","varCur") +
          pm.SparqlGenerator.solutionModifierSourcesSelection()
        val qr = QueryRunner(source).query(query)
        qr.map( res => true) // todo : check count > 0
      }
      case None => Future { true }
    }
  }
}
