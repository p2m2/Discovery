package inrae.semantic_web.driver

import inrae.semantic_web.sparql.QueryResult

import scala.concurrent.Future

case class Rdf4jLocalRequestDriver(idName: String, inline_def:String,format : String) extends LocalRequestDriver(idName,inline_def,format) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def request(query: String): Future[QueryResult] = {
    Future { QueryResult("") }
  }
}
