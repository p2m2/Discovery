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


    test("RosHttpDriver get") {
      RosHTTPDriver().get("select * where { <aa> ?b ?c . } limit 1", ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
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
    test("JenaRequestDriver get bad request") {
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

    test("JenaRequestDriver get malformed endpoint") {
      RosHTTPDriver().get("select * where { <aa> ?b ?c . } limit 1", ConfigurationHttpRequest(url = "bidon"))
        .onComplete {
          case Success(qr) => {
            assert(false)
          }
          case Failure(e) => {
            assert(true)
          }
        }
    }

    test("JenaRequestDriver get endpoint does not exist") {
      RosHTTPDriver().get("select * where { <aa> ?b ?c . } limit 1", ConfigurationHttpRequest(url = "http://bidon.com"))
        .onComplete {
          case Success(qr) => {
            assert(false)
          }
          case Failure(e) => {
            assert(true)
          }
        }
    }
    test("RosHttpDriver post") {
      RosHTTPDriver().post("select * where { <aa> ?b ?c. } limit 1", ConfigurationHttpRequest(url = DataTestFactory.url_endpoint))
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
  }
}