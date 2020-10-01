package inrae.semantic_web.sparql
import inrae.http.Request
import inrae.semantic_web.ConfigurationObject

import scala.concurrent.Future

/**
 *
 * @param source
 */
case class QueryRunner(source: ConfigurationObject.Source) {

  def query(queryStr: String): Future[QueryResult] = {
    val r = Request(source.url)
    r.queryViaPost(queryStr,"json")
  }

  def ask(): Unit = {

  }

  def construct() : Unit = {

  }

  def describe() : Unit = {

  }
}
