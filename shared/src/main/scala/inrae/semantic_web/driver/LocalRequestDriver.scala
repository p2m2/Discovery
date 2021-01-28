package inrae.semantic_web.driver

import inrae.semantic_web.sparql.QueryResult

import scala.concurrent.Future

abstract class LocalRequestDriver(idName: String, inline_def:String,format : String) extends RequestDriver {
  def request(query: String): Future[QueryResult]
}
