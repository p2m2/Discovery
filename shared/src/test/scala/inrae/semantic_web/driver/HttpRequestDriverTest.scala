package inrae.semantic_web.driver

import inrae.data.DataTestFactory
import inrae.semantic_web.StatementConfigurationException
import monix.execution.Scheduler.Implicits.global
import org.portablescala.reflect.{InvokableConstructor, Reflect}
import utest.{TestRunner, TestSuite, Tests, test}
import wvlet.log.{LogLevel, Logger}

object HttpRequestDriverTest extends TestSuite {

  val insert_data = DataTestFactory.insert_virtuoso1(
    """
      <aaRosHttpDriverTest> <bb> <cc> .
      """.stripMargin, this.getClass.getSimpleName)


  Logger.setDefaultLogLevel(LogLevel.OFF)

  def getInstanceDriver(driver : String,
                        idName: String,
                        `type` : String,
                        url : String,
                        login: String ,
                        password: String,
                        token: String,
                        auth: String,
                        mimetype : String
                       ) : HttpRequestDriver = Reflect.lookupInstantiatableClass(driver) match {
    case Some( cls ) => {
      val ctor : Option[InvokableConstructor] =
        cls.getConstructor(classOf[String], classOf[String],classOf[String], classOf[String],classOf[String], classOf[String],classOf[String], classOf[String])
      ctor.get.newInstance(idName,`type`,url,login,password,token,auth,mimetype).asInstanceOf[HttpRequestDriver]
    }
    case None => throw StatementConfigurationException("Unknown Http Request Driver :"+driver)
  }

  val query = "select ?b ?c where { <aaRosHttpDriverTest> ?b ?c . } limit 1"

  def genericTest(driver : String) : Tests = Tests {

    test("get") {
      insert_data.map(_ => {
        getInstanceDriver(driver, idName = "test", `type` = "get", url = DataTestFactory.url_endpoint, login = "", password = "", token = "", auth = "", mimetype = "")
          .request(query)
          .map(qr => {
            assert(qr.json("results")("bindings").arr(0)("b")("value").value == "bb")
            assert(qr.json("results")("bindings").arr(0)("c")("value").value == "cc")
          })
      }).flatten
    }

    test("get bad request") {
      insert_data.map(_ => {
        getInstanceDriver(driver, idName = "test", `type` = "get", url = DataTestFactory.url_endpoint, login = "", password = "", token = "", auth = "", mimetype = "")
          .request("bad request")
          .map(qr => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }

    test("get malformed endpoint") {
      insert_data.map(_ => {
        getInstanceDriver(driver, idName = "test", `type` = "get", url = "bidon", login = "", password = "", token = "", auth = "", mimetype = "")
          .request(query)
          .map(qr => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }

    test("get endpoint does not exist") {
      insert_data.map(_ => {
        getInstanceDriver(driver, idName = "test", `type` = "get", url = "http://bidon.com", login = "", password = "", token = "", auth = "", mimetype = "")
          .request(query)
          .map(qr => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }

    test("post") {
      insert_data.map(_ => {
        getInstanceDriver(driver, idName = "test", `type` = "post", url = DataTestFactory.url_endpoint, login = "", password = "", token = "", auth = "", mimetype = "")
          .request(query)
          .map(qr => {
            assert(qr.json("results")("bindings").arr(0)("b")("value").value == "bb")
            assert(qr.json("results")("bindings").arr(0)("c")("value").value == "cc")
          })
      }).flatten
    }

    test("post bad request") {
      insert_data.map(_ => {
        getInstanceDriver(driver, idName = "test", `type` = "post", url = DataTestFactory.url_endpoint, login = "", password = "", token = "", auth = "", mimetype = "")
          .request("bad request")
          .map(qr => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }
    test("post malformed endpoint") {
      insert_data.map(_ => {
        getInstanceDriver(driver, idName = "test", `type` = "post", url = "bidon", login = "", password = "", token = "", auth = "", mimetype = "")
          .request(query)
          .map(qr => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }

    test("post endpoint does not exist") {
      insert_data.map(_ => {
        getInstanceDriver(driver, idName = "test", `type` = "post", url = "http://bidon.com", login = "", password = "", token = "", auth = "", mimetype = "")
          .post(query)
          .map(qr => assert(false))
          .recover(_ => assert(true))
      }).flatten
    }
  }

  def tests = Tests {
    for(driver <- Seq("RosHttpDriver","SHTTPRequest","JenaRequestDriver")) {
      genericTest(driver)
    }
  }

  TestRunner.runAsync(tests).map { _ => {
      DataTestFactory.delete_virtuoso1(this.getClass.getSimpleName)
    }
  }
}
