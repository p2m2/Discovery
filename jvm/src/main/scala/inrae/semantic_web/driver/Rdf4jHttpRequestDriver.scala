package inrae.semantic_web.driver

import inrae.semantic_web.sparql.QueryResult

import scala.concurrent.Future

case class Rdf4jHttpRequestDriver(idName: String,
                                  method : String,
                                  url : String,
                                  login: String ,
                                  password: String,
                                  token: String,
                                  auth: String) extends HttpRequestDriver(method){

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def apply(query: String): Future[QueryResult] = {
    Future { QueryResult("") }
  }

  def get( query: String ) : Future[QueryResult] = apply(query: String)

  def post( query: String ) : Future[QueryResult] = apply(query: String)
}
