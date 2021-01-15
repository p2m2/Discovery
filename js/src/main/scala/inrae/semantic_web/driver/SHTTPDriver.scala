package inrae.semantic_web.driver

import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent}
import inrae.semantic_web.sparql.{ConfigurationHttpRequest, HttpRequestDriver, HttpRequestDriverException, QueryResult}
import org.portablescala.reflect.annotation.EnableReflectiveInstantiation
import sttp.client3._

import scala.concurrent.Future

@EnableReflectiveInstantiation
case class SHTTPDriver() extends HttpRequestDriver {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val backendShttp = FetchBackend

  def send(request  : RequestT[Identity,Either[String,String],Any]) : Future[QueryResult] = {
    request.send(backendShttp())
      .map( response =>  response.body match {
        case Right(v) => {
          publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.FINISHED_HTTP_REQUEST))
          QueryResult(v)
        }
        case Left(e) => {
          publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.ERROR_HTTP_REQUEST))
          throw HttpRequestDriverException(e)
        }
      })
  }

  def post(query: String, config: ConfigurationHttpRequest): Future[QueryResult] = {
      publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.PROCESS_HTTP_REQUEST))
      send(basicRequest.post(uri"${config.url}")
        .headers(Map("accept" -> "application/json",
          "content-type" -> "application/x-www-form-urlencoded"))
        .body(Map("query" -> query)))
  }

  def get(query: String, config: ConfigurationHttpRequest): Future[QueryResult] = {
      publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.PROCESS_HTTP_REQUEST))
      send(basicRequest.get(uri"${config.url}")
        .headers(Map("accept" -> "application/json"))
        .body(Map("query" -> query)))
  }

}
