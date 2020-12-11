package inrae.semantic_web.sandbox

import inrae.semantic_web.{SW, StatementConfiguration}
import inrae.semantic_web.rdf._
import utest._

import scala.concurrent.Future
import scala.util.{Failure, Success}


object GeoSparql extends TestSuite {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val configTest: StatementConfiguration = StatementConfiguration()
    .setConfigString(
    """
      {
                "sources" : [{
                "id"  : "geosparql",
                "url" : "https://www.navigae.fr/repositories/Navigae",
                "type" : "tps",
                "method" : "POST",
                "mimetype" : "json"
                 }]}
      """.stripMargin)

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
          .datatype(URI("label", "rdfs"), "label")
          .datatype(URI("http://purl.org/dc/terms/isPartOf"), "dbpedia")
          .select(List("label", "geometry","dbpedia"))
          .onComplete {
            case Success(response) => {
              println("RESULTATS ==============================")
              println(response("results")("datatypes")("label"))
              println(response("results")("datatypes")("dbpedia"))
              assert(true)
            }
            case Failure(exception) => println(exception); assert(false)
          }
      }
    }
  }
}

