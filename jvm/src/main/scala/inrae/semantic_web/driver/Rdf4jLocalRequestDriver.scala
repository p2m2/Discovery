package inrae.semantic_web.driver

import inrae.semantic_web.sparql.QueryResult
import org.eclipse.rdf4j.repository.RepositoryConnection

import scala.concurrent.Future

case class Rdf4jLocalRequestDriver(con : RepositoryConnection) extends Rdf4jRequestDriver {
  def requestOnSWDB(query: String): Future[QueryResult] = requestConnexionRepository(con,query)
}
