package EasySparql

import java.io.ByteArrayOutputStream

import org.apache.jena.query.{Query, QueryExecution, ResultSet, ResultSetFormatter}

import scala.util.{Failure, Success, Try}

case class QueryResult(executor: QueryExecution, query: Query) {
  def print(): Unit = {
    Try(executor.execSelect()).foreach(ResultSetFormatter.out(System.out, _, query))
  }

  def asJson(): Option[String] = {
    Try(executor.execSelect()).toOption.map { result =>
      val outStream = new ByteArrayOutputStream()
      ResultSetFormatter.outputAsJSON(outStream, result)
      outStream.toString
    }
  }

  def asJsonOrError(): String = {
    Try(executor.execSelect()).map { result =>
      val outStream = new ByteArrayOutputStream()
      ResultSetFormatter.outputAsJSON(outStream, result)
      outStream.toString
    } match {
      case Success(value) => executor.close(); value
      case Failure(exception) => executor.close(); exception.getMessage
    }
  }

  def asXML(): Option[String] = {
    Try(executor.execSelect()).toOption.map(ResultSetFormatter.asXMLString)
  }

  def asXMLOrError(): String = {
    Try(executor.execSelect()).map(ResultSetFormatter.asXMLString) match {
      case Success(value) => executor.close(); value
      case Failure(exception) => executor.close(); exception.getMessage
    }
  }

  def asCSV(): Option[String] = {
    Try(executor.execSelect()).toOption.map { result =>
      val outStream = new ByteArrayOutputStream()
      ResultSetFormatter.outputAsCSV(outStream, result)
      outStream.toString
    }
  }

  def asCSVOrError(): String = {
    Try(executor.execSelect()).map { result =>
      val outStream = new ByteArrayOutputStream()
      ResultSetFormatter.outputAsCSV(outStream, result)
      outStream.toString
    } match {
      case Success(value) => executor.close(); value
      case Failure(exception) => executor.close(); exception.getMessage
    }
  }

  def asTextOrError(): String = {
    Try(executor.execSelect()).map { result =>
      ResultSetFormatter.asText(result, query)
    } match {
      case Success(value) => executor.close(); value
      case Failure(exception) => executor.close(); exception.getMessage
    }
  }
}