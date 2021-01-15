package inrae.semantic_web.driver

import inrae.semantic_web.sparql.{ConfigurationHttpRequest, HttpRequestDriver, QueryResult}
import org.portablescala.reflect.annotation.EnableReflectiveInstantiation
import sttp.client3.{UriContext, basicRequest}

import scala.concurrent.Future

@EnableReflectiveInstantiation
case class SHTTPDriver() extends HttpRequestDriver {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  /*
    jvm :
    HttpURLConnectionBackend
   */

  val backendShttp = FetchBackend

  def post(query: String, config: ConfigurationHttpRequest): Future[QueryResult] = {
    Future {
      val request = basicRequest.post(uri"${config.url}")
        .headers(Map("accept" -> "application/json",
          "content-type" -> "application/x-www-form-urlencoded"))
        .body(Map("query" -> query))
      val backend = backendShttp()
      val response = request.send(backend)
      response.body match {
        case Right(v) => QueryResult(v)
        case Left(v) => QueryResult(v)
      }
    }
  }

  def get(query: String, config: ConfigurationHttpRequest): Future[QueryResult] = {
    Future {
      val request = basicRequest.get(uri"${config.url}")
        .headers(Map("accept" -> "application/json"))
        .body(Map("query" -> query))
      val backend = backendShttp()
      val response = request.send(backend)
      response.body match {
        case Right(v) => QueryResult(v)
        case Left(v) => QueryResult(v)
      }
    }
  }

}
