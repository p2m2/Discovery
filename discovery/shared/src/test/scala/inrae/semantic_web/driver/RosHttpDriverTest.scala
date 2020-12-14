package inrae.semantic_web.driver

import inrae.data.DataTestFactory
import inrae.semantic_web.sparql.ConfigurationHttpRequest
import utest.{TestSuite, Tests, test}
import wvlet.log.Logger.rootLogger.error

import scala.util.{Failure, Success}
import inrae.semantic_web.driver._
import monix.execution.Scheduler.Implicits.global

object RosHttpDriverTest extends TestSuite {
  DataTestFactory.insert_virtuoso1(
    """
      <aa> <bb> <cc> .
      """.stripMargin, this.getClass.getSimpleName)

  override def utestAfterAll(): Unit = {
    DataTestFactory.delete_virtuoso1(this.getClass.getSimpleName)
  }

  def tests = Tests {


    test("get") {
      RosHTTPDriver().get("select ?b ?c where { <aa> ?b ?c . } limit 1", ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
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
      //NOSONAR
      RosHTTPDriver().get("bad request", ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
        .onComplete {
          case Success(qr) => {
            assert(false)
          }
          case Failure(e) => {
            assert(true)
          }
        }
    }
    test("get malformed endpoint") {
      //NOSONAR
      RosHTTPDriver().get("select ?b ?c where { <aa> ?b ?c . } limit 1", ConfigurationHttpRequest(url = "bidon"))
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
      RosHTTPDriver().get("select ?b ?c where { <aa> ?b ?c . } limit 1", ConfigurationHttpRequest(url = "http://bidon.com"))
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
      RosHTTPDriver().post("select ?b ?c where { <aa> ?b ?c. } limit 1", ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
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
      RosHTTPDriver().post("bad request", ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
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
      RosHTTPDriver().post("select ?b ?c where { <aa> ?b ?c . } limit 1", ConfigurationHttpRequest(url = "bidon"))
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
      RosHTTPDriver().post("select ?b ?c where { <aa> ?b ?c . } limit 1", ConfigurationHttpRequest(url = "http://bidon.com"))
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