package inrae.semantic_web.sparql

import inrae.semantic_web._
import org.apache.jena.query._
import org.apache.jena.rdf.model._

import scala.concurrent.Future

case class QueryRunner(source: ConfigurationObject.Source) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def query(queryStr: String): Future[QueryResult] = {
    Future {
      /* Graph equiv Model => defined in configuration */
      val model = ModelFactory.createDefaultModel
      val query = QueryFactory.create(queryStr)

      //val authenticator = new Nothing("user", "password".toCharArray)
      val qexec: QueryExecution = QueryExecutionFactory.sparqlService(source.url, query)
      val results: ResultSet = qexec.execSelect()

      QueryResult(ResultSetFactory.copyResults(results))
    }
  }

  def ask(): Unit = {

  }

  def construct() : Unit = {

  }

  def describe() : Unit = {

  }
}
