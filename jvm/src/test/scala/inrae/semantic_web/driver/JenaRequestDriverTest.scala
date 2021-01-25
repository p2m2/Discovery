package inrae.semantic_web.driver

import inrae.data.DataTestFactory
import inrae.semantic_web.sparql.ConfigurationHttpRequest
import utest.{TestRunner, TestSuite, Tests, test}
import wvlet.log.{LogLevel, Logger}


object JenaRequestDriverTest extends TestSuite {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val insert_data = DataTestFactory.insert_virtuoso1(
    """
      <aaJenaRequestDriverTest> <bb> <cc> .
      """.stripMargin, this.getClass.getSimpleName)


  Logger.setDefaultLogLevel(LogLevel.OFF)

  val query = "select ?b ?c where { <aaJenaRequestDriverTest> ?b ?c . } limit 1"

  def tests = Tests {

    test("get") {
      insert_data.map(_ => {
      JenaRequestDriver().get(query, ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
        .map( qr => {
            assert(qr.json("results")("bindings").arr(0)("b")("value").value=="bb")
            assert(qr.json("results")("bindings").arr(0)("c")("value").value=="cc")
        })
      }).flatten
    }

    test("get bad request") {
      insert_data.map(_ => {
        JenaRequestDriver().get("bad request", ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
         .map( qr => assert(false) )
          .recover( _ => assert(true))
      }).flatten
    }

    test("get malformed endpoint") {
      insert_data.map(_ => {
      JenaRequestDriver().get(query, ConfigurationHttpRequest(url = "bidon"))
        .map( qr => assert(false) )
        .recover( _ => assert(true))
      }).flatten
    }

    test("get endpoint does not exist") {
      insert_data.map(_ => {
      JenaRequestDriver().get(query, ConfigurationHttpRequest(url = "http://bidon.com"))
        .map( qr => assert(false) )
        .recover( _ => assert(true))
      }).flatten
    }

    test("post") {
      insert_data.map(_ => {
      JenaRequestDriver().post(query, ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
        .map( qr => {
            assert(qr.json("results")("bindings").arr(0)("b")("value").value=="bb")
            assert(qr.json("results")("bindings").arr(0)("c")("value").value=="cc")
          })
      }).flatten
    }


    test("post bad request") {
      insert_data.map(_ => {
      JenaRequestDriver().post("bad request", ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
        .map( _ => {
            assert(false)
        }).recover( _ => assert(true))
      }).flatten
    }

    test("post malformed endpoint") {
      insert_data.map(_ => {
      JenaRequestDriver().post(query, ConfigurationHttpRequest(url = "bidon"))
        .map( qr => assert(false) )
        .recover( _ => assert(true))
      }).flatten
    }

    test("post endpoint does not exist") {
      insert_data.map(_ => {
      JenaRequestDriver().post(query, ConfigurationHttpRequest(url = "http://bidon.com"))
        .map( qr => assert(false) )
        .recover( _ => assert(true))
      }).flatten
    }
  }

  TestRunner.runAsync(tests).map { _ => {
    DataTestFactory.delete_virtuoso1(this.getClass.getSimpleName)
  }
  }
}
