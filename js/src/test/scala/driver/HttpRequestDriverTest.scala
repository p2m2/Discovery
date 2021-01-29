package driver

import inrae.data.DataTestFactory
import inrae.semantic_web.StatementConfigurationException
import inrae.semantic_web.driver.HttpRequestDriver
import org.portablescala.reflect.{InvokableConstructor, Reflect}
import utest.{TestRunner, TestSuite, Tests, test}
import wvlet.log.{LogLevel, Logger}

object HttpRequestDriverTest extends TestSuite {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val insert_data = DataTestFactory.insert_virtuoso1(
    """
      <aaRosHttpDriverTest> <bb> <cc> .
      """.stripMargin, this.getClass.getSimpleName)


  Logger.setDefaultLogLevel(LogLevel.OFF)

  def getInstanceDriver(driver: String,
                        idName: String,
                        method: String,
                        url: String,
                        login: String,
                        password: String,
                        token: String,
                        auth: String
                       ): HttpRequestDriver = Reflect.lookupInstantiatableClass(driver) match {
    case Some(cls) => {
      val ctor: Option[InvokableConstructor] =
        cls.getConstructor(classOf[String], classOf[String], classOf[String], classOf[String], classOf[String], classOf[String], classOf[String], classOf[String])
      ctor.get.newInstance(idName, method, url, login, password, token, auth).asInstanceOf[HttpRequestDriver]
    }
    case None => throw StatementConfigurationException("Unknown Http Request Driver :" + driver)
  }

  val query = "select ?b ?c where { <aaRosHttpDriverTest> ?b ?c . } limit 1"

  def genericTest(driver: String): Tests = Tests {

    test("get") {
      insert_data.map(_ => {
        getInstanceDriver(driver, idName = "test", method = "get", url = DataTestFactory.url_endpoint, login = "", password = "", token = "", auth = "")
          .request(query)
          .map(qr => {
            assert(qr.json("results")("bindings").arr(0)("b")("value").value == "bb")
            assert(qr.json("results")("bindings").arr(0)("c")("value").value == "cc")
          })
      }).flatten
    }

    test("get bad request") {
      insert_data.map(_ => {
        getInstanceDriver(driver, idName = "test", method = "get", url = DataTestFactory.url_endpoint, login = "", password = "", token = "", auth = "")
          .request("bad request")
          .map(qr => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }

    test("get malformed endpoint") {
      insert_data.map(_ => {
        getInstanceDriver(driver, idName = "test", method = "get", url = "bidon", login = "", password = "", token = "", auth = "")
          .request(query)
          .map(qr => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }

    test("get endpoint does not exist") {
      insert_data.map(_ => {
        getInstanceDriver(driver, idName = "test", method = "get", url = "http://bidon.com", login = "", password = "", token = "", auth = "")
          .request(query)
          .map(qr => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }

    test("post") {
      insert_data.map(_ => {
        getInstanceDriver(driver, idName = "test", method = "post", url = DataTestFactory.url_endpoint, login = "", password = "", token = "", auth = "")
          .request(query)
          .map(qr => {
            assert(qr.json("results")("bindings").arr(0)("b")("value").value == "bb")
            assert(qr.json("results")("bindings").arr(0)("c")("value").value == "cc")
          })
      }).flatten
    }

    test("post bad request") {
      insert_data.map(_ => {
        getInstanceDriver(driver, idName = "test", method = "post", url = DataTestFactory.url_endpoint, login = "", password = "", token = "", auth = "")
          .request("bad request")
          .map(qr => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }
    test("post malformed endpoint") {
      insert_data.map(_ => {
        getInstanceDriver(driver, idName = "test", method = "post", url = "bidon", login = "", password = "", token = "", auth = "")
          .request(query)
          .map(qr => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }

    test("post endpoint does not exist") {
      insert_data.map(_ => {
        getInstanceDriver(driver, idName = "test", method = "post", url = "http://bidon.com", login = "", password = "", token = "", auth = "")
          .post(query)
          .map(qr => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }
  }

  def tests = Tests {
    for (driver <- Seq("RosHttpDriver", "SHTTPRequest", "JenaRequestDriver")) {
      genericTest(driver)
    }
  }

  TestRunner.runAsync(tests).map { _ => {
    DataTestFactory.delete_virtuoso1(this.getClass.getSimpleName)
  }
  }
}
