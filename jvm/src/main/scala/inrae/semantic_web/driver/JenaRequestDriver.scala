package inrae.semantic_web.driver

import inrae.semantic_web.sparql.{QueryResult}
import org.apache.jena.query._

import java.io.ByteArrayOutputStream
import scala.concurrent.Future
import org.portablescala.reflect.annotation.EnableReflectiveInstantiation
import wvlet.log.Logger.rootLogger.debug

import scala.util.{Failure, Success, Try}

@EnableReflectiveInstantiation
case class JenaRequestDriver(idName: String,
                             method : String,
                             url : String,
                             login: String ,
                             password: String,
                             token: String,
                             auth: String) extends HttpRequestDriver(method) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def process(queryStr: String): Future[QueryResult] = {

    Future {
      /* Graph equiv Model => defined in configuration */
      //val model = ModelFactory.createDefaultModel
      val query = QueryFactory.create(queryStr)

      //val authenticator = new Nothing("user", "password".toCharArray)
      val qexec: QueryExecution = Try(QueryExecutionFactory.sparqlService(url, query)) match {
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

  def get( query: String ) : Future[QueryResult] = {
    process(query)
  }

  def post( query: String ) : Future[QueryResult] = {
    process(query)
  }
}
