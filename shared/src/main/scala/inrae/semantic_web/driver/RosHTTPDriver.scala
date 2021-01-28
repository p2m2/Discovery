package inrae.semantic_web.driver

import fr.hmil.roshttp.HttpRequest
import fr.hmil.roshttp.Method.{GET, POST}
import fr.hmil.roshttp.exceptions.HttpException
import fr.hmil.roshttp.response.SimpleHttpResponse
import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent}
import monix.execution.Scheduler.Implicits.global
import inrae.semantic_web.sparql.{QueryResult}
import org.portablescala.reflect.annotation.EnableReflectiveInstantiation
import wvlet.log.Logger.rootLogger.debug


import scala.concurrent.Future

@EnableReflectiveInstantiation
case class RosHTTPDriver(idName: String,
                         method : String,
                         url : String,
                         login: String ,
                         password: String,
                         token: String,
                         auth: String) extends HttpRequestDriver(method) {

  def post(query: String): Future[QueryResult] = {
    publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.PROCESS_HTTP_REQUEST))

    (HttpRequest(url)
      .withHeader("Accept", "application/json")
      .withHeader("Content-Type", "application/x-www-form-urlencoded")
      .withQueryParameter("query", query)
      .withMethod(POST)
      .send()
      .recover {
        case HttpException(e: SimpleHttpResponse) =>
          // Here we may have some detailed application-level insight about the error
          //error("There was an issue with your request." +
          //  " Here is what the application server says: " + e.body)
          publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.ERROR_HTTP_REQUEST))
          throw HttpRequestDriverException(e.body)
        case e: Throwable => {
          publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.ERROR_HTTP_REQUEST))
          throw HttpRequestDriverException(e.getMessage())
        }
      }).map(v => {
      publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.FINISHED_HTTP_REQUEST))
      debug(v.asInstanceOf[SimpleHttpResponse].body.substring(0, 100))
      QueryResult(v.asInstanceOf[SimpleHttpResponse].body)
    })
  }

  def get(query: String): Future[QueryResult] = {

    publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.PROCESS_HTTP_REQUEST))

    (HttpRequest(url)
      .withHeader("Accept", "application/json")
      .withMethod(GET)
      .withQueryParameter("query", query)
      .send()
      .recover {
        case HttpException(e: SimpleHttpResponse) =>
          publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.ERROR_HTTP_REQUEST))
          throw HttpRequestDriverException(e.body)
        case e: Throwable => {
          publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.ERROR_HTTP_REQUEST))
          throw HttpRequestDriverException(e.getMessage())
        }
      }).map(v => {
      publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.FINISHED_HTTP_REQUEST))
      QueryResult(v.asInstanceOf[SimpleHttpResponse].body)
    })
  }

}
