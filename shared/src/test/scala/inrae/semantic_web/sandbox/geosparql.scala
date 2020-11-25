package inrae.semantic_web.sandbox

import inrae.semantic_web.{SW, StatementConfiguration}
import inrae.semantic_web.rdf._
import utest._

import scala.concurrent.Future
import scala.util.{Failure, Success}


object GeoSparql extends TestSuite {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val configTest: StatementConfiguration = new StatementConfiguration()
  configTest.setConfigString(
    """
      {
      |          "sources" : [{
      |          "id"  : "dbpedia",
      |          "url" : "https://www.navigae.fr/repositories/Navigae",
      |          "typ" : "Navigae",
      |          "method" : "POST_ENCODED",
      |          "mimetype" : "json"
      |           }]}
      |""".stripMargin)

  def tests = Tests {
    test("GeoSparql Test") {
      val query = SW(configTest)

      Future {
        query.something("instance")
          .isSubjectOf(URI("a"))
          .set(URI("https://www.navigae.fr/ontology#AerialPhoto"))
          .focus("instance")
          .isSubjectOf(URI("http://navigae.fr/ontology#hasPolygonGeometry"))
          .isSubjectOf(URI("http://www.opengis.net/ont/geosparql#asWKT"), "geometry")
          .focus("instance")
          .isSubjectOf(URI("label", "rdfs"), "label")
          .debug()
          .select(List("label", "geometry"))
          .onComplete {
            case Success(response) => {
              response("results")("bindings").arr.map( rec => println("====>>"+rec.toString)  )
              assert(true)
            }
            case Failure(exception) => println(exception); assert(false)
          }
      }
    }
  }
}

