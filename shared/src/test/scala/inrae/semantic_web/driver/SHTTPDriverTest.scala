package inrae.semantic_web.driver

import inrae.data.DataTestFactory
import inrae.semantic_web.sparql.ConfigurationHttpRequest
import utest.{TestSuite, Tests, test}
import wvlet.log.Logger.rootLogger.error
import wvlet.log.{LogLevel, Logger}

import scala.util.{Failure, Success}

object SHTTPDriverTest extends TestSuite {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  DataTestFactory.delete_virtuoso1(this.getClass.getSimpleName)

  DataTestFactory.insert_virtuoso1(
    """
      <aaSHTTPDriverTestDriverTest> <bb> <cc> .
      """.stripMargin, this.getClass.getSimpleName)


  Logger.setDefaultLogLevel(LogLevel.OFF)

  val query = "select ?b ?c where { <aaSHTTPDriverTestDriverTest> ?b ?c . } limit 1"

  def tests = Tests {

    test("get") {
      SHTTPDriver().get(query, ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
        .map( qr => {
            assert(qr.json("results")("bindings").arr(0)("b")("value").value == "bb")
            assert(qr.json("results")("bindings").arr(0)("c")("value").value == "cc")
          }
        )
    }

    test("get bad request") {
      SHTTPDriver().get("bad request", ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
        .map(_ => assert(false))
        .recover( v => assert(true) )
    }

    test("get malformed endpoint") {
      SHTTPDriver().get(query, ConfigurationHttpRequest(url = "bidon"))
        .map(_ => assert(false))
        .recover( _ => assert(true))
    }

    test("get endpoint does not exist") {
      SHTTPDriver().get(query, ConfigurationHttpRequest(url = "http://bidon.com"))
        .map(_ => assert(false))
        .recover( _ => assert(true))
    }

    test("post") {
      SHTTPDriver().post(query, ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
        .map(qr => {
          assert(qr.json("results")("bindings").arr.length>0)
          assert(qr.json("results")("bindings").arr(0)("b")("value").value == "bb")
          assert(qr.json("results")("bindings").arr(0)("c")("value").value == "cc")
        })
    }

    test("post bad request") {
      //NOSONAR
      SHTTPDriver().post("bad request", ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
        .map(_ => assert(false))
        .recover( _ => assert(true))
    }

    test("post malformed endpoint") {
      //NOSONAR
      SHTTPDriver().post(query, ConfigurationHttpRequest(url = "bidon"))
        .map(_ => assert(false))
        .recover( _ => assert(true))
    }

    test("post endpoint does not exist") {
      SHTTPDriver().post(query, ConfigurationHttpRequest(url = "http://bidon.com"))
        .map(_ => assert(false))
        .recover( _ => assert(true))
    }
  }
}
