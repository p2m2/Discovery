package inrae.semantic_web.driver

import inrae.semantic_web.sparql.QueryResult

import scala.concurrent.Future

case class Rdf4jFileRequestDriver(idName: String, path:String, format : String) extends RequestDriver {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def request(query: String): Future[QueryResult] = {
    Future { QueryResult("") }
  }
}
