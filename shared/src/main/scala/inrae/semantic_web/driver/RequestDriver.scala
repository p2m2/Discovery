package inrae.semantic_web.driver

import inrae.semantic_web.event.{DiscoveryRequestEvent, Publisher}
import inrae.semantic_web.sparql.QueryResult

import scala.concurrent.Future

abstract class RequestDriver extends Publisher[DiscoveryRequestEvent] {
  def request(query: String): Future[QueryResult]
}
