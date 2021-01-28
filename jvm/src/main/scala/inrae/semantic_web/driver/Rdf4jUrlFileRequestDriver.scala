package inrae.semantic_web.driver

import inrae.semantic_web.sparql.QueryResult

import scala.concurrent.Future

case class Rdf4jUrlFileRequestDriver(idName: String,  url:String, format : String) extends UrlFileRequestDriver(idName,url,format) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def request(query: String): Future[QueryResult] = {
    Future { QueryResult("") }
  }
}
