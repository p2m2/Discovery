package inrae.semantic_web.strategy

import inrae.semantic_web.SWTransaction
import inrae.semantic_web.event.{DiscoveryRequestEvent, Publisher}
import inrae.semantic_web.sparql.QueryResult

import scala.concurrent.Future

/** Management of discovery requests
 *
 *  This is further documentation of what we're documenting.
 *  Here are more details about how it works and what it does.
 */
abstract class StrategyRequest() extends Publisher[DiscoveryRequestEvent] {

  def execute(swt : SWTransaction) : Future[QueryResult]

}
