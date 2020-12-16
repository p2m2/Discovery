package inrae.semantic_web

import inrae.data.DataTestFactory
import inrae.semantic_web.rdf._
import utest._
import wvlet.log.Logger.rootLogger.error

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.matching.Regex
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object SWNodeAddTest extends TestSuite {

  val config: StatementConfiguration = DataTestFactory.getConfigVirtuoso1()

  def tests = Tests {

    test("something") {
      val s = SW(config).something("h1")
    }

    test("isSubjectOf on the root") {
      Try(SW(config).isSubjectOf(URI("bb"), "var")) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("isSubjectOf") {
      val s = SW(config)
               .something("h1")
               .isSubjectOf(URI("bb"), "var")

      val triplet: Regex = "\\?something[0-9]+ <bb> \\?object[0-9]+".r

      triplet.findFirstMatchIn(s.sparql()) match {
        case Some(_) => assert(true)
        case None => assert(false)
      }
    }

    test("isObjectOf on the root") {
      Try(SW(config).isObjectOf(URI("bb"), "var")) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("isObjectOf") {
      val s = SW(config)
        .something("h1")
        .isObjectOf(URI("bb"), "var")

      val triplet: Regex = "\\?subject[0-9]+ <bb> \\?something[0-9]+".r

      triplet.findFirstMatchIn(s.sparql()) match {
        case Some(_) => assert(true)
        case None => assert(false)
      }
    }

    test("isLinkTo on the root") {

      Try(SW(config).isLinkTo(URI("bb"), "var")) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("isLinkTo") {
      val s = SW(config)
        .something("h1")
        .isLinkTo(URI("bb"), "var")

      val triplet: Regex = "\\?something[0-9]+ \\?linkto[0-9]+ <bb>".r

      triplet.findFirstMatchIn(s.sparql()) match {
        case Some(_) => assert(true)
        case None => assert(false)
      }
    }

    test("isLinkFrom on the root") {
      Try(SW(config).isLinkFrom(URI("bb"), "var")) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("isLinkFrom") {
      val s = SW(config)
        .something("h1")
        .isLinkFrom(URI("bb"), "var")

      val triplet: Regex = "<bb> \\?linkfrom[0-9]+ \\?something[0-9]+".r

      triplet.findFirstMatchIn(s.sparql()) match {
        case Some(_) => assert(true)
        case None => assert(false)
      }
    }

    test("isA on the root") {
      Try(SW(config).isA(URI("class"))) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("isLinkFrom") {
      val s = SW(config)
        .something("h1")
        .isA(URI("class"))

      val triplet: Regex = "\\?something[0-9]+ a \\?object[0-9]+".r

      triplet.findFirstMatchIn(s.sparql()) match {
        case Some(_) => assert(true)
        case None => assert(false)
      }
    }

  }
}
