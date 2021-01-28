package inrae.semantic_web.driver

import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent}
import inrae.semantic_web.sparql.{QueryResult}
import org.portablescala.reflect.annotation.EnableReflectiveInstantiation
import sttp.client3._

import scala.concurrent.Future

@EnableReflectiveInstantiation
case class SHTTPDriver(idName: String,
                       method : String,
                       url : String,
                       login: String ,
                       password: String,
                       token: String,
                       auth: String) extends HttpRequestDriver(method)  {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  /*
    jvm :
    HttpURLConnectionBackend
   */

  val backendShttp = HttpURLConnectionBackend

  def send(request  : RequestT[Identity,Either[String,String],Any]) : QueryResult = {

    request.send(backendShttp()).body match {
            case Right(v) => {
              publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.FINISHED_HTTP_REQUEST))
              QueryResult(v)
            }
            case Left(e) => {
              publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.ERROR_HTTP_REQUEST))
              throw HttpRequestDriverException(e)
            }
        }
  }

  def post(query: String): Future[QueryResult] = {
    Future {
      publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.PROCESS_HTTP_REQUEST))

      val request = basicRequest.post(uri"${url}")
        .headers(Map("accept" -> "application/json",
          "content-type" -> "application/x-www-form-urlencoded"))
        .body(Map("query" -> query))


      send(request)
      }.recover( e => {
        publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.ERROR_HTTP_REQUEST))
        throw HttpRequestDriverException(e.getMessage())
      })
  }

  def get(query: String): Future[QueryResult] = {
      Future {
        publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.PROCESS_HTTP_REQUEST))
        val request = basicRequest.get(uri"${url}")
            .headers(Map("accept" -> "application/json"))
            .body(Map("query" -> query))

        send(request)
      }.recover( e => {
        publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.ERROR_HTTP_REQUEST))
        throw HttpRequestDriverException(e.getMessage())
      })
  }

}
