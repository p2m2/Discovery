package inrae.application

import inrae.semantic_web.StatementConfiguration
import inrae.semantic_web.SW
import inrae.semantic_web.rdf.{IRI, URI}

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
//https://bbip.askomics.org/virtuoso/sparql
//https://dbpedia.org/sparql
object TutorialApp {
  def main(args: Array[String]): Unit = {

    val config: StatementConfiguration = new StatementConfiguration()
    config.setConfigString(
      """
        |{
        | "sources" : [{
        |   "id"  : "bbip",
        |   "url" : "https://bbip.askomics.org/virtuoso/sparql",
        |   "typ" : "tps",
        |   "method" : "GET",
        |   "mimetype" : "json"
        | }]}
        |""".stripMargin)
    val query = SW(config)
    query
      .prefix("owl", IRI("http://www.w3.org/2002/07/owl#"))
      .prefix("asko", IRI("http://askomics.org/internal/"))
      .something("entity")
      .isA(URI("Class", "owl"))
      .filter.not.contains("http://www.w3.org/2002/07/owl#")
      //.debug()
      //.sparql_console()
      .select(List("entity"))
      .onComplete {
        case Success(result) => {
          println("========================= RESULTS ===============================")
          println(result)
        }
        case Failure(exception) => println(exception)
      }

  }
}
