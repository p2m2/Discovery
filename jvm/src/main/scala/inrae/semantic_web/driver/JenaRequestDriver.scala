package inrae.semantic_web.driver

import inrae.semantic_web.sparql.{ConfigurationHttpRequest, HttpRequestDriver, HttpRequestDriverException, QueryResult}
import org.apache.jena.query._

import java.io.ByteArrayOutputStream
import scala.concurrent.Future
import org.portablescala.reflect.annotation.EnableReflectiveInstantiation
import wvlet.log.Logger.rootLogger.debug

import scala.util.{Failure, Success, Try}

@EnableReflectiveInstantiation
case class JenaRequestDriver() extends HttpRequestDriver {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def process(queryStr: String, config : ConfigurationHttpRequest): Future[QueryResult] = {

    Future {
      /* Graph equiv Model => defined in configuration */
      //val model = ModelFactory.createDefaultModel
      val query = QueryFactory.create(queryStr)

      //val authenticator = new Nothing("user", "password".toCharArray)
      val qexec: QueryExecution = Try(QueryExecutionFactory.sparqlService(config.url, query)) match {
        case Success(result) => result
        case Failure(e) => throw HttpRequestDriverException(e.getMessage())
      }

      debug("queryExecution Ok !")

      val results: ResultSet = Try(qexec.execSelect()) match {
        case Success(result) => result
        case Failure(e) => throw HttpRequestDriverException(e.getMessage())
      }

      debug("execSelect Ok !")

      val outputStream = new ByteArrayOutputStream();
      ResultSetFormatter.outputAsJSON(outputStream, results)
      // and turn that into a String
      val json = new String(outputStream.toByteArray)
      QueryResult(json, "json")
    }
  }

  def get( query: String, config : ConfigurationHttpRequest ) : Future[QueryResult] = {
    process(query,config)
  }

  def post( query: String, config : ConfigurationHttpRequest ) : Future[QueryResult] = {
    process(query,config)
  }
}
