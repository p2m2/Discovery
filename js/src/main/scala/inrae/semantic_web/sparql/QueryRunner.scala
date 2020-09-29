package inrae.semantic_web.sparql
import inrae.http.{Request, ResultSet}
import inrae.semantic_web.ConfigurationObject

/**
 *
 * @param source
 */
case class QueryRunner(source: ConfigurationObject.Source) {

  def query(queryStr: String): QueryResult = {
    val r = Request(source.url)
    r.queryViaPost(queryStr,"json")
    QueryResult(ResultSet())
  }

  def ask(): Unit = {

  }

  def construct() : Unit = {

  }

  def describe() : Unit = {

  }
}
