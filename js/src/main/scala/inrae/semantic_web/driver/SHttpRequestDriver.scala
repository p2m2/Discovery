package inrae.semantic_web.driver

import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent}
import inrae.semantic_web.sparql.QueryResult
import sttp.client3._
import wvlet.log.Logger.rootLogger.debug

import scala.concurrent.Future

case class SHttpRequestDriver(
                               idName : String,
                               method : String,
                               url: String,
                               login : String,
                               password: String,
                               token : String,
                               auth : String)
  extends HttpRequestDriver {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val backendShttp = FetchBackend

  def request(query: String): Future[QueryResult] = {
    publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.START_HTTP_REQUEST))
    debug(" -- HttpRequestDriver > " + this.getClass.getName )

    method.toLowerCase() match {
      case "post" => post(query)
      case "get" => get(query)
      case _ => {
        throw HttpRequestDriverException(s"Unknown http type request : ${method}")
      }
    }
  }

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

  def post(query: String): Future[QueryResult] = {
      publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.PROCESS_HTTP_REQUEST))
      send(basicRequest.post(uri"${url}")
        .headers(Map("accept" -> "application/json",
          "content-type" -> "application/x-www-form-urlencoded"))
        .body(Map("query" -> query)))
  }

  def get(query: String): Future[QueryResult] = {
      publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.PROCESS_HTTP_REQUEST))
      send(basicRequest.get(uri"${url}")
        .headers(Map("accept" -> "application/json"))
        .body(Map("query" -> query)))
  }

}
