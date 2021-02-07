package inrae.semantic_web.driver

import inrae.data.DataTestFactory
import utest.{TestRunner, TestSuite, Tests, test}
import wvlet.log.{LogLevel, Logger}

object HttpRequestDriverTest extends TestSuite {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val insert_data = DataTestFactory.insert_virtuoso1(
    """
      <aaRosHttpDriverTest> <bb> <cc> .
      """.stripMargin, this.getClass.getSimpleName)


  Logger.setDefaultLogLevel(LogLevel.OFF)


  val query = "select ?b ?c where { <aaRosHttpDriverTest> ?b ?c . } limit 1"

  def tests = Tests {

    test("get") {
      insert_data.map(_ => {
        AxiosRequestDriver(idName = "test", method = "get", url = DataTestFactory.url_endpoint, login = "", password = "", token = "", auth = "")
          .request(query)
          .map(qr => {
            assert(qr.json("results")("bindings").arr(0)("b")("value").value == "bb")
            assert(qr.json("results")("bindings").arr(0)("c")("value").value == "cc")
          })
      }).flatten
    }

    test("get bad request") {
      insert_data.map(_ => {
        AxiosRequestDriver(idName = "test", method = "get", url = DataTestFactory.url_endpoint, login = "", password = "", token = "", auth = "")
          .request("bad request")
          .map(qr => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }
/*
    test("get malformed endpoint") {
      insert_data.map(_ => {
        AxiosRequestDriver(idName = "test", method = "get", url = "bidon", login = "", password = "", token = "", auth = "")
          .request(query)
          .map(qr => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }

    test("get endpoint does not exist") {
      insert_data.map(_ => {
        AxiosRequestDriver(idName = "test", method = "get", url = "http://bidon.com", login = "", password = "", token = "", auth = "")
          .request(query)
          .map(qr => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }
*/
    test("post") {
      insert_data.map(_ => {
        AxiosRequestDriver(idName = "test", method = "post", url = DataTestFactory.url_endpoint, login = "", password = "", token = "", auth = "")
          .request(query)
          .map(qr => {
            assert(qr.json("results")("bindings").arr(0)("b")("value").value == "bb")
            assert(qr.json("results")("bindings").arr(0)("c")("value").value == "cc")
          })
      }).flatten
    }

    test("post bad request") {
      insert_data.map(_ => {
        AxiosRequestDriver(idName = "test", method = "post", url = DataTestFactory.url_endpoint, login = "", password = "", token = "", auth = "")
          .request("bad request")
          .map(qr => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }
    /*
    test("post malformed endpoint") {
      insert_data.map(_ => {
        AxiosRequestDriver(idName = "test", method = "post", url = "bidon", login = "", password = "", token = "", auth = "")
          .request(query)
          .map(qr => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }

    test("post endpoint does not exist") {
      insert_data.map(_ => {
        AxiosRequestDriver(idName = "test", method = "post", url = "http://bidon.com", login = "", password = "", token = "", auth = "")
          .post(query)
          .map(qr => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }
 */
  }

  TestRunner.runAsync(tests).map { _ => DataTestFactory.delete_virtuoso1(this.getClass.getSimpleName) }
}
