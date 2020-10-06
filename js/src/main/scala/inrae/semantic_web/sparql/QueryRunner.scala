package inrae.semantic_web.sparql
import inrae.http.Request
import inrae.semantic_web.ConfigurationObject

import scala.concurrent.Future
import scala.scalajs.js
/**
 *
 * @param source
 */
case class QueryRunner(source: ConfigurationObject.Source) {

  def query(queryStr: String): Future[QueryResult] = {
    val r = Request(source.url)
    source.method match {
      case "POST" => r.queryViaPost(queryStr,"json")
      case "POST2" => r.queryViaUrlEncodedPost(queryStr,"json")
      case "GET" => r.queryViaGet(queryStr,mimetype = "json")
      case _ => throw new js.JavaScriptException("Unknown method :"+_)
    }

  }

  def ask(): Unit = {

  }

  def construct() : Unit = {

  }

  def describe() : Unit = {

  }
}
