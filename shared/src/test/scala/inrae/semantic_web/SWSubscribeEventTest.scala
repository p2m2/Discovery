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

  def tests = Tests {
    test("DiscoveryRequestEvent steps") {
      val swr =
        SW(config).something("h1")
          .isSubjectOf(URI("bb"))
          .select(List("h1"))

      swr
        .raw
        .map( _=>{
          assert(swr.requestEvent == "REQUEST_DONE")
          assert(swr.progression == 1.0)
        })
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
          assert(swr.requestEvent == "ERROR_HTTP_REQUEST") } )
    }
  }
}
