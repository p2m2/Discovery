package inrae.semantic_web.strategy

import inrae.semantic_web.SWTransaction
import inrae.semantic_web.sparql.QueryResult

import scala.concurrent.Future

case class ProxyStrategyRequest(urlProxy : String) extends StrategyRequest {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  def execute(sw : SWTransaction) : Future[QueryResult] = {
    Future {QueryResult("")}
  }
}
