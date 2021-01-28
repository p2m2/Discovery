package inrae.semantic_web.driver

import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent}
import inrae.semantic_web.sparql.QueryResult
import wvlet.log.Logger.rootLogger.debug

import scala.concurrent.Future

final case class HttpRequestDriverException(private val message: String = "",
                                            private val cause: Throwable = None.orNull) extends Exception(message,cause)

abstract class HttpRequestDriver(method : String) extends RequestDriver  {

  def request(query: String): Future[QueryResult] = {
    publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.START_HTTP_REQUEST))
    debug(" -- HttpRequestDriver > " + this.getClass.getName )

    method.toLowerCase() match {
      case "post" => post( query)
      case "get" => get( query  )
      case _ => {
        throw HttpRequestDriverException(s"Unknown http type request : ${method}")
      }
    }
  }

  def get( query: String ) : Future[QueryResult]

  def post( query: String ) : Future[QueryResult]

}
