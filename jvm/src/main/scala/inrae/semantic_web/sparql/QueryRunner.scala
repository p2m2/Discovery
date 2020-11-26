package inrae.semantic_web.sparql
import java.io.ByteArrayOutputStream

import inrae.semantic_web._
import org.apache.jena.query._
import org.apache.jena.query.ResultSetFormatter

import scala.concurrent.Future

case class QueryRunner(source: ConfigurationObject.Source) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def infoLogger() = {
    println("----------------------------LOG INFO ----------------------------------")
    println("-------------------------- getInfoLogger ------------------------------------")
    println("RQ.getInfoLogger DEBUG : " + ARQ.getInfoLogger.isDebugEnabled())
    println("RQ.getInfoLogger INFO  : " + ARQ.getInfoLogger().isInfoEnabled())
    println("RQ.getInfoLogger WARN  : " + ARQ.getInfoLogger().isWarnEnabled())
    println("RQ.getInfoLogger ERROR : " + ARQ.getInfoLogger().isErrorEnabled())
    println("RQ.getInfoLogger TRACE : " + ARQ.getInfoLogger().isTraceEnabled())
    println("-------------------------- getHttpRequestLogger ------------------------------------")
    println("RQ.getInfoLogger DEBUG : " + ARQ.getHttpRequestLogger.isDebugEnabled())
    println("RQ.getInfoLogger INFO  : " + ARQ.getHttpRequestLogger().isInfoEnabled())
    println("RQ.getInfoLogger WARN  : " + ARQ.getHttpRequestLogger().isWarnEnabled())
    println("RQ.getInfoLogger ERROR : " + ARQ.getHttpRequestLogger().isErrorEnabled())
    println("RQ.getInfoLogger TRACE : " + ARQ.getHttpRequestLogger().isTraceEnabled())
    println("---------------------------- getExecLogger ----------------------------------")
    println("RQ.getExecLogger DEBUG : " + ARQ.getExecLogger().isDebugEnabled())
    println("RQ.getExecLogger INFO  : " + ARQ.getExecLogger().isInfoEnabled())
    println("RQ.getExecLogger WARN  : " + ARQ.getExecLogger().isWarnEnabled())
    println("RQ.getExecLogger ERROR : " + ARQ.getExecLogger().isErrorEnabled())
    println("RQ.getExecLogger TRACE : " + ARQ.getExecLogger().isTraceEnabled())
    println("----------------------------END LOG INFO----------------------------------")
  }

  def query(queryStr: String): Future[QueryResult] = {
    scribe.debug("query")
    scribe.debug(source.url)
    Future {
      /* Graph equiv Model => defined in configuration */
      //val model = ModelFactory.createDefaultModel
      val query = QueryFactory.create(queryStr)

      //val authenticator = new Nothing("user", "password".toCharArray)
      try {
        val qexec: QueryExecution = QueryExecutionFactory.sparqlService(source.url, query)
        val results: ResultSet = qexec.execSelect()

        val outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results)
        // and turn that into a String
        val json = new String(outputStream.toByteArray)
        QueryResult(json,"json")
      } catch {
        case e: QueryParseException => scribe.error(e.getMessage)
          QueryResult(e.getMessage,"error")
        case e : Throwable => {
          System.err.println(e.getMessage)
          QueryResult(e.getMessage,"error")
        }
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
