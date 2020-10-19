package inrae.semantic_web

import inrae.semantic_web.{SW, StatementConfiguration}
import inrae.semantic_web.rdf._
import utest._

import scala.language.postfixOps
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
//import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.concurrent.{Await,blocking}
import scala.concurrent.duration._

object SWTest extends TestSuite {

  def tests = Tests {
    /*
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
          |   "method" : "GET",
          |   "mimetype" : "json"
          | }]}
          |""".stripMargin)
      val query = new SW(config)

      Future {
        query.something("h1")
          .set(URI("http://dbpedia.org/resource/%C3%84lvdalen"))
          .isSubjectOf(URI("http://www.w3.org/2002/07/owl#sameAs"))
          .select
          .onComplete {
            case Success(result) => println(result.get); assert(true)
            case Failure(exception) => println(exception); assert(false)
          }
      }
    }*/
    /*
    test("debug") {
      val config: StatementConfiguration = new StatementConfiguration()
      config.setConfigString(
        """
          |{
          | "sources" : [{
          |   "id"  : "dbpedia",
          |   "url" : "https://dbpedia.org/sparql",
          |   "typ" : "tps",
          |   "method" : "GET",
          |   "mimetype" : "json"
          | }]}
          |""".stripMargin)
      val query = new SW(config)

      query.something("h1")
          .set(URI("http://dbpedia.org/resource/%C3%84lvdalen"))
          .isSubjectOf(URI("http://www.w3.org/2002/07/owl#sameAs"))
    }
*/
    test("count") {
      val config: StatementConfiguration = new StatementConfiguration()
      config.setConfigString(
        """
          |{
          | "sources" : [{
          |   "id"  : "dbpedia",
          |   "url" : "https://dbpedia.org/sparql",
          |   "typ" : "tps",
          |   "method" : "POST_ENCODED",
          |   "mimetype" : "json"
          | }]}
          |""".stripMargin)
      val query = new SW(config)
      query.something("h1") //http://rdf.ebi.ac.uk/terms/chembl#BioComponent
        .isSubjectOf(URI("http://dbpedia.org/ontology/deathDate"))
        .count
        .onComplete {
        case Success(count) => {
          println(count)
          assert(true)
        }
        case Failure(exception) => {
          System.err.println(exception)
          assert(false)
        }
      }
    }
/*
    test("findTypeOf") {
      val config: StatementConfiguration = new StatementConfiguration()
      config.setConfigString(
        """
          |{
          | "sources" : [{
          |   "id"  : "dbpedia",
          |   "url" : "https://dbpedia.org/sparql",
          |   "typ" : "tps",
          |   "method" : "GET",
          |   "mimetype" : "json"
          | }]}
          |""".stripMargin)
      val query = new SW(config)

      Future {
        query.something("h1")
          .set(URI("http://dbpedia.org/resource/Abbie_Hoffman"))
          .findClassesOf()
          .onComplete {
            case Success(types) => println(types); assert(true)
            case Failure(exception) => println(exception); assert(false)
          }
      }
    }
    test("findTypeOf - owl:Class") {
      val config: StatementConfiguration = new StatementConfiguration()
      config.setConfigString(
        """
          |{
          | "sources" : [{
          |   "id"  : "dbpedia",
          |   "url" : "https://dbpedia.org/sparql",
          |   "typ" : "tps",
          |   "method" : "GET",
          |   "mimetype" : "json"
          | }]}
          |""".stripMargin)
      val query = new SW(config)

      Future {
        query.something("h1")
          .set(URI("http://dbpedia.org/resource/Abbie_Hoffman"))
          .findClassesOf(URI("http://www.w3.org/2002/07/owl#Class"))
          .onComplete {
            case Success(types) => println(types); assert(true)
            case Failure(exception) => println(exception); assert(false)
          }
      }
    }

    test("findTypeOf - rdf:type") {
      val config: StatementConfiguration = new StatementConfiguration()
      config.setConfigString(
        """
          |{
          | "sources" : [{
          |   "id"  : "dbpedia",
          |   "url" : "https://dbpedia.org/sparql",
          |   "typ" : "tps",
          |   "method" : "GET",
          |   "mimetype" : "json"
          | }]}
          |""".stripMargin)
      val query = new SW(config)

      Future {
        query.something("h1")
          .set(URI("http://dbpedia.org/resource/Abbie_Hoffman"))
          .findClassesOf(URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
          .onComplete {
            case Success(types) => println(types); assert(true)
            case Failure(exception) => println(exception); assert(false)
          }
      }
    }
    test("findObjectPropertiesOf") {
      val config: StatementConfiguration = new StatementConfiguration()
      config.setConfigString(
        """
          |{
          | "sources" : [{
          |   "id"  : "dbpedia",
          |   "url" : "https://dbpedia.org/sparql",
          |   "typ" : "tps",
          |   "method" : "GET",
          |   "mimetype" : "json"
          | }]}
          |""".stripMargin)
      val query = new SW(config)

      Future {
        query.something("h1")
          .set(URI("http://dbpedia.org/resource/Abbie_Hoffman"))
          .findObjectPropertiesOf()
          .onComplete {
            case Success(types) => println(types); assert(true)
            case Failure(exception) => println(exception); assert(false)
          }
      }
    }*/
  }
}