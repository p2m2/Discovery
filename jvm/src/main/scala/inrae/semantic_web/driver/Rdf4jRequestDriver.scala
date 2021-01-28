package inrae.semantic_web.driver

import inrae.semantic_web.sparql.QueryResult
import org.eclipse.rdf4j.repository.RepositoryConnection

import scala.concurrent.Future

case class Rdf4jRequestDriver(con : RepositoryConnection) extends RequestDriver {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def request(query: String): Future[QueryResult] = {
    Future { QueryResult("") }
  }
}
