package inrae.semantic_web.internal

import inrae.semantic_web.{SW, StatementConfiguration}
import inrae.semantic_web.rdf._
import utest._
import scala.language.postfixOps
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
//import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.concurrent.{Await,blocking}
import scala.concurrent.duration._

object SWTest extends TestSuite {

  def tests = Tests {
    test("Create a simple query") {
      val config: StatementConfiguration = new StatementConfiguration()
      config.setConfigString(""" { "sources" : [] } """)
      val query = new SW(config)
      val r = query.something("h1")
    }

    test("Create a query finding a subject") {
      val config: StatementConfiguration = new StatementConfiguration()
      config.setConfigString(
        """
          |{
          | "sources" : [{
          |   "id"  : "dbpedia",
          |   "url" : "https://dbpedia.org/sparql",
          |   "typ" : "tps",
          |   "method" : "POST"
          | }]}
          |""".stripMargin)
      val query = new SW(config)

      query.something("h1")
        .set(URI("http://dbpedia.org/resource/%C3%84lvdalen"))
        .isSubjectOf(URI("http://www.w3.org/2002/07/owl#sameAs"))
        .select
        .onComplete {
          case Success(result) => println(result.get); assert(true)
          case Failure(exception) => println(exception); assert(false)
        }

      query
        .select
        .onComplete {
          case Success(result) => println(result.get); assert(true)
          case Failure(exception) => println(exception); assert(false)
        }

      val query2 = new SW(config)

      query2.something("h1")
        .isSubjectOf(URI("http://www.w3.org/2002/07/owl#sameAs"))
        .select
        .onComplete {
          case Success(result) => println(result.get); assert(true)
          case Failure(exception) => println(exception); assert(false)
        }
    }
  }
}