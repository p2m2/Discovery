package inrae.semantic_web

import inrae.data.DataTestFactory
import inrae.semantic_web.rdf._
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.util.{Failure, Success}

object SWSubscribeEventTest extends TestSuite {

  DataTestFactory.delete_virtuoso1(this.getClass.getSimpleName)

  DataTestFactory.insert_virtuoso1(
    """
      <aaSWSubscribeEventTest> <bb> <cc> .
      <aa> <datatype> "testdatatype" .
      """.stripMargin, this.getClass.getSimpleName)

  val config: StatementConfiguration = DataTestFactory.getConfigVirtuoso1()

  def stepDiscoveryExecutor(unsubscribe : Boolean = false) = {
    var stepDiscovery : Map[String,Boolean] = Map(
      "QUERY_BUILD" -> false,
      "DATATYPE_BUILD" -> false,
      "DATATYPE_DONE" -> false,
      "START_HTTP_REQUEST" -> false,
      "PROCESS_HTTP_REQUEST" -> false,
      "FINISHED_HTTP_REQUEST" -> false,
      "RESULTS_BUILD" -> false,
      "RESULTS_DONE"-> false)

    def funEvent( event: String ) =  {
      println(event)
      stepDiscovery = stepDiscovery + (event -> true)
    }

    def funProg( percent: Double ) =  {
      println(percent)
    }

    val sw = SW(config)

    val swr = sw.something("h1")
      .isSubjectOf(URI("bb"))
      .select(List("h1"))

    if(unsubscribe) {
      swr.requestEvent(funEvent)
      swr.progression(funProg)
    }
    swr.raw.map(_ => {
          assert(swr.currentRequestEvent == "REQUEST_DONE")
      println(stepDiscovery)
          if (unsubscribe)
            assert(stepDiscovery.forall( x => ! x._2))
          else
            assert(stepDiscovery.forall( x => x._2))
        })
    }

  def tests = Tests {

    test("DiscoveryRequestEvent steps") {
      stepDiscoveryExecutor(false)
    }

    test("unsubscribe") {
      stepDiscoveryExecutor(true)
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

      var swr =
        SW(config).something("h1")
        .isSubjectOf(URI("bb"))
        .select(List("h1"))

      swr.raw.map( _=> assert(false))
        .recover( _ => {
          assert(swr.currentRequestEvent == "ERROR_HTTP_REQUEST") } )
    }
  }
}
