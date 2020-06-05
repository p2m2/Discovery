package EasySparql

import org.apache.jena.query.{QueryExecutionFactory, QueryFactory, ResultSetFormatter, ResultSetRewindable}
import org.apache.jena.sparql.engine.http.QueryEngineHTTP

import scala.util.Try


case class QueryRunner(service: String) {
  def query(queryStr: String): QueryResult = {
    val query = QueryFactory.create(queryStr)

    val executor = QueryExecutionFactory.sparqlService(service, query)
    executor match {
      case httpEngine: QueryEngineHTTP => httpEngine.addParam("timeout", "10")
      case _ =>
    }
    QueryResult(executor, query)
  }
}