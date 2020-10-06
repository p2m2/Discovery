package inrae.semantic_web

import inrae.semantic_web.internal.{pm, _}
import inrae.semantic_web.sparql._

import scala.concurrent.Future
import scala.util.Try


object QueryManager {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def queryNode(rootRequest : Node, n: Node, config : StatementConfiguration) : Future[QueryResult] = {
    queryVariables(rootRequest,n.references(),config)
  }

  def queryAll(rootRequest : Node,n: Node, config : StatementConfiguration) : Future[QueryResult] = {
    queryVariables(rootRequest,rootRequest.references(),config)
  }

  def queryVariables(n: Node, listVariables : Seq[String], config : StatementConfiguration) : Future[QueryResult] = {
    if (config.sources().length == 0) {
      throw new Exception(" ** None sources available ** ")
    } else if (config.sources().length == 1) {
      queryOnSource(rootRequest,listVariables,config.sources()(0))
    } else {
      // todo query planner
      Future { QueryResult("","") }
    }
  }

  def queryOnSource(n: Node, listVariables : Seq[String],  source : ConfigurationObject.Source): Future[QueryResult] = {
    val sg = new pm.SparqlGenerator()
    val query = sg.prolog(listVariables) + "\n" + sg.body(source, n) + sg.solutionModifier()

    QueryRunner(source).query(query)
  }



  def testNode(n: Node, source : ConfigurationObject.Source): Future[Boolean] = {
    val sg = new pm.SparqlGenerator()
    val query = sg.prologSourcesSelection() + "\n" +
                sg.sparqlNode(n,"varUp","varCur") +
                sg.solutionModifierSourcesSelection()

    val qr = QueryRunner(source).query(query)
    qr.map( res => true)
  }
}
