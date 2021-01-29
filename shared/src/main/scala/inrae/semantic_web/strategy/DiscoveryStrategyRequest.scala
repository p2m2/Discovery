package inrae.semantic_web.strategy

import inrae.semantic_web.ConfigurationObject.Source
import inrae.semantic_web.driver._
import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent, Publisher, Subscriber}
import inrae.semantic_web.{SWTransaction, SparqlQueryBuilder}
import inrae.semantic_web.internal.{Root, pm}
import inrae.semantic_web.sparql.QueryResult
import wvlet.log.Logger.rootLogger.trace

import scala.concurrent.Future

case class DiscoveryStrategyRequest(source : Source) extends StrategyRequest {

  val driver : RequestDriver = RequestDriverFactory.build(source)

  driver.subscribe(this.asInstanceOf[Subscriber[DiscoveryRequestEvent,Publisher[DiscoveryRequestEvent]]])

  def execute(swt : SWTransaction) : Future[QueryResult] = {
    val (refToIdentifier, _) = pm.SparqlGenerator.correspondenceVariablesIdentifier(swt.sw.rootNode)
    trace(refToIdentifier.toString())
    publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.QUERY_BUILD))
    val query: String = SparqlQueryBuilder.selectQueryString(swt.sw.rootNode, refToIdentifier, swt.lSelectVariables,swt.limit,swt.offset)
    driver.request(query)
  }

  def request(query: String): Future[QueryResult] = driver.request(query)

}
