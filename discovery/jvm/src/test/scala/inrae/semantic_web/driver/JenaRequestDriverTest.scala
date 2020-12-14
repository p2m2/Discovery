package inrae.semantic_web.driver

import inrae.data.DataTestFactory
import inrae.semantic_web.sparql.ConfigurationHttpRequest
import utest.{TestSuite, Tests, test}
import wvlet.log.Logger.rootLogger.error

import scala.util.{Failure, Success}


object JenaRequestDriverTest extends TestSuite {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  DataTestFactory.insert_virtuoso1(
    """
      <aa> <bb> <cc> .
      """.stripMargin, this.getClass.getSimpleName)

  override def utestAfterAll(): Unit = {
    DataTestFactory.delete_virtuoso1(this.getClass.getSimpleName)
  }

  def tests = Tests {

    val query = "select ?b ?c where { <aa> ?b ?c . } limit 1"

    test("get") {
      JenaRequestDriver().get(query, ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
        .onComplete {
          case Success(qr) => {
            assert(qr.json("results")("bindings").arr(0)("b")("value").value=="bb")
            assert(qr.json("results")("bindings").arr(0)("c")("value").value=="cc")
          }
          case Failure(e) => {
            error(e.getMessage())
            assert(false)
          }
        }
    }

    test("get bad request") {
      JenaRequestDriver().get("bad request", ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
        .onComplete {
          case Success(_) => {
            assert(false)
          }
          case Failure(_) => {
            assert(true)
          }
        }
    }

    test("get malformed endpoint") {
      JenaRequestDriver().get(query, ConfigurationHttpRequest(url = "bidon"))
        .onComplete {
          case Success(_) => {
            assert(false)
          }
          case Failure(_) => {
            assert(true)
          }
        }
    }

    test("get endpoint does not exist") {
      JenaRequestDriver().get(query, ConfigurationHttpRequest(url = "http://bidon.com"))
        .onComplete {
          case Success(_) => {
            assert(false)
          }
          case Failure(_) => {
            assert(true)
          }
        }
    }

    test("post") {
      JenaRequestDriver().post(query, ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
        .onComplete {
          case Success(qr) => {
            assert(qr.json("results")("bindings").arr(0)("b")("value").value=="bb")
            assert(qr.json("results")("bindings").arr(0)("c")("value").value=="cc")
          }
          case Failure(e) => {
            error(e.getMessage())
            assert(false)
          }
        }
    }

    test("post bad request") {
      //NOSONAR
      JenaRequestDriver().post("bad request", ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
        .onComplete {
          case Success(_) => {
            assert(false)
          }
          case Failure(_) => {
            assert(true)
          }
        }
    }
    test("post malformed endpoint") {
      //NOSONAR
      JenaRequestDriver().post(query, ConfigurationHttpRequest(url = "bidon"))
        .onComplete {
          case Success(_) => {
            assert(false)
          }
          case Failure(_) => {
            assert(true)
          }
        }
    }

    test("post endpoint does not exist") {
      JenaRequestDriver().post(query, ConfigurationHttpRequest(url = "http://bidon.com"))
        .onComplete {
          case Success(_) => {
            assert(false)
          }
          case Failure(_) => {
            assert(true)
          }
        }
    }
  }
}