package inrae.semantic_web.driver

import inrae.data.DataTestFactory
import inrae.semantic_web.sparql.ConfigurationHttpRequest
import utest.{TestRunner, TestSuite, Tests, test}
import wvlet.log.{LogLevel, Logger}

object SHTTPDriverTest extends TestSuite {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val insert_data = DataTestFactory.insert_virtuoso1(
    """
      <aaSHTTPDriverTestDriverTest> <bb> <cc> .
      """.stripMargin, this.getClass.getSimpleName)


  Logger.setDefaultLogLevel(LogLevel.OFF)

  val query = "select ?b ?c where { <aaSHTTPDriverTestDriverTest> ?b ?c . } limit 1"

  def tests = Tests {

    test("get") {
      insert_data.map(_ => {
        SHTTPDriver().get(query, ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
          .map(qr => {
            assert(qr.json("results")("bindings").arr(0)("b")("value").value == "bb")
            assert(qr.json("results")("bindings").arr(0)("c")("value").value == "cc")
          })
      }).flatten
    }

    test("get bad request") {
      insert_data.map(_ => {
        SHTTPDriver().get("bad request", ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
          .map(_ => assert(false))
          .recover(v => assert(true))
      }).flatten
    }

    test("get malformed endpoint") {
      insert_data.map(_ => {
        SHTTPDriver().get(query, ConfigurationHttpRequest(url = "bidon"))
          .map(_ => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }

    test("get endpoint does not exist") {
      insert_data.map(_ => {
        SHTTPDriver().get(query, ConfigurationHttpRequest(url = "http://bidon.com"))
          .map(_ => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }

    test("post") {
      insert_data.map(_ => {
        SHTTPDriver().post(query, ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
          .map(qr => {
            assert(qr.json("results")("bindings").arr.length > 0)
            assert(qr.json("results")("bindings").arr(0)("b")("value").value == "bb")
            assert(qr.json("results")("bindings").arr(0)("c")("value").value == "cc")
          })
      }).flatten
    }

    test("post bad request") {
      insert_data.map(_ => {
        SHTTPDriver().post("bad request", ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
          .map(_ => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }

    test("post malformed endpoint") {
      insert_data.map(_ => {
        SHTTPDriver().post(query, ConfigurationHttpRequest(url = "bidon"))
          .map(_ => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }

    test("post endpoint does not exist") {
      insert_data.map(_ => {
        SHTTPDriver().post(query, ConfigurationHttpRequest(url = "http://bidon.com"))
          .map(_ => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }
  }

  TestRunner.runAsync(tests).map { _ => {
    DataTestFactory.delete_virtuoso1(this.getClass.getSimpleName)
  }
  }
}
