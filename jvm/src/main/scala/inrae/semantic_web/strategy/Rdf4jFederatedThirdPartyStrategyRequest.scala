package inrae.semantic_web.strategy

import inrae.semantic_web.ConfigurationObject.Source
import inrae.semantic_web.driver.{RequestDriver, RequestDriverFactory}
import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent}
import inrae.semantic_web.internal.pm
import inrae.semantic_web.sparql.QueryResult
import inrae.semantic_web.{SWTransaction, SparqlQueryBuilder}

import scala.concurrent.Future

/**
 * Requests are the responsibility of a third party
 */
case class Rdf4jFederatedThirdPartyStrategyRequest(sources : Seq[Source]) extends StrategyRequest {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val driver : Seq[RequestDriver] = sources.map(RequestDriverFactory.build(_))

  def execute(swt : SWTransaction) : Future[QueryResult] = {
    val (refToIdentifier, _) = pm.SparqlGenerator.correspondenceVariablesIdentifier(swt.sw.rootNode)

    publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.QUERY_BUILD))
    val query: String = SparqlQueryBuilder.selectQueryString(swt.sw.rootNode, refToIdentifier, swt.lSelectVariables,swt.limit,swt.offset)

    Future { QueryResult("") }

  }
}
