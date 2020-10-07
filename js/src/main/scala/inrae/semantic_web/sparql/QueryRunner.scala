package inrae.semantic_web.sparql
import inrae.http.Request
import inrae.semantic_web.ConfigurationObject

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
/**
 *
 * @param source
 */
case class QueryRunner(source: ConfigurationObject.Source) {

  def query(queryStr: String): Future[QueryResult] = {
      val r = Request(source.url)
      source.method match {
        case "POST" => r.queryViaPost(queryStr, source.mimetype)
        case "POST_ENCODED" => r.queryViaUrlEncodedPost(queryStr, source.mimetype)
        case "GET" => r.queryViaGet(queryStr, source.mimetype)
        case _ => {
          val p = Promise[QueryResult]()
          p.failure(js.JavaScriptException("[Configuration] Unknown HTTP request method :" + source.method))
          p.future
        }
      }
  }

  def ask(): Unit = {

  }

  def construct() : Unit = {

  }

  def describe() : Unit = {

  }
}
