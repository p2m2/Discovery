package inrae.semantic_web.driver

import inrae.semantic_web.SWDiscoveryException
import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent}
import inrae.semantic_web.sparql.QueryResult
import org.eclipse.rdf4j.federated.endpoint.SparqlEndpointConfiguration
import org.eclipse.rdf4j.federated.endpoint.provider.SPARQLRepositoryInformation
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository

import java.io.ByteArrayOutputStream
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

case class Rdf4jSparqlRequestDriver(idName: String,
                                    url : String,
                                    login: String,
                                    password: String,
                                    token: String,
                                    auth: String) extends RequestDriver {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  var p : SPARQLRepositoryInformation = new SPARQLRepositoryInformation(idName, url)

  val conf = new SparqlEndpointConfiguration()
  val repo = new SPARQLRepository(url)
  if (login != "" && password != "") repo.setUsernameAndPassword(login,password)

  def request(query: String): Future[QueryResult] = {
      Future {
        println("====> Start")
        publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.START_HTTP_REQUEST))
        publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.PROCESS_HTTP_REQUEST))
        val out = new ByteArrayOutputStream()
/*
        val result = repo.getConnection()
          .prepareTupleQuery(query).evaluate()
        while (result.hasNext()) println(result.next())
*/
          Try(repo.getConnection()
          .prepareTupleQuery(query)
          .evaluate(new SPARQLResultsJSONWriter(out))) match {
          case Success(_) => {
            publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.FINISHED_HTTP_REQUEST))
            publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.RESULTS_BUILD))
            val response = inrae.semantic_web.sparql.QueryResult(out.toString())
            publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.RESULTS_DONE))
            println("====> END")
            response
          }
          case Failure(e) => {
            publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.ERROR_HTTP_REQUEST))
            throw SWDiscoveryException("\n"+query+"\n"+e.getMessage)
          }
        }
      }
    }

}
