package inrae.semantic_web

import inrae.data.DataTestFactory
import inrae.semantic_web.rdf._
import utest._
import wvlet.log.Logger.rootLogger.error

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.util.{Failure, Success}

object SWSubscribeEventTest extends TestSuite {

  DataTestFactory.insert_virtuoso1(
    """
      <aaSWSubscribeEventTest> <bb> <cc> .
      <aa> <datatype> "testdatatype" .
      """.stripMargin, this.getClass.getSimpleName)

  val config: StatementConfiguration = DataTestFactory.getConfigVirtuoso1()



  def tests = Tests {
    test("DiscoveryRequestEvent steps") {
      var stepDiscovery : Map[String,Boolean] = Map(
        "QUERY_BUILD" -> false,
        "DATATYPE_BUILD" -> false,
        "DATATYPE_DONE" -> false,
        "START_HTTP_REQUEST" -> false,
        "PROCESS_HTTP_REQUEST" -> false,
        "FINISHED_HTTP_REQUEST" -> false,
        "RESULTS_BUILD" -> false,
        "RESULTS_DONE"-> false)

      def funsub( event: String ) =  {
        stepDiscovery = stepDiscovery + (event -> true)
      }

      var sw = SW(config)
      sw.subscribe("myfun", funsub)

        sw.something("h1")
          .isSubjectOf(URI("bb"))

        .select(List("h1"))
        .onComplete {
          case Success(_) => {
            assert(stepDiscovery.forall( x => x._2))
          }
          case Failure(_) => {
            assert(false)
          }
        }
    }

    test("DiscoveryRequestEvent ERROR_HTTP_REQUEST") {
      val config: StatementConfiguration = StatementConfiguration()
      config.setConfigString(""" {
                               |         "sources" : [{
                               |           "id"       : "badtps",
                               |           "url"      : "http://bidon",
                               |           "type"     : "tps",
                               |           "method"   : "POST",
                               |           "mimetype" : "json"
                               |         }]
                               |         } """.stripMargin)

      var stepDiscovery : Map[String,Boolean] = Map(
        "ERROR_HTTP_REQUEST" -> false
      )

      var sw = SW(config)
      sw.subscribe("myfun", (s => { stepDiscovery = stepDiscovery + (s -> true) }))
      sw.something("h1")
        .isSubjectOf(URI("bb"))
        .select(List("h1"))
        .onComplete {
          case Success(_) => {
            assert(false)
          }
          case Failure(_) => {
            assert(stepDiscovery("ERROR_HTTP_REQUEST"))
          }
        }
    }
  }
}
