package inrae.semantic_web.internal.pm

import inrae.semantic_web.internal._
import inrae.semantic_web.sparql._
import inrae.semantic_web.rdf._
import utest.{TestSuite, Tests, assert, test}

object SparqlGeneratorTest extends TestSuite {
  def tests = Tests {
    test("Sparql Prolog - Variable list empty") {
      assert(SparqlGenerator.prolog(Seq[String]()) == "SELECT * WHERE {")
    }

    test("Sparql Prolog - One Variable ") {
      assert(SparqlGenerator.prolog(Seq[String]("test")) == "SELECT ?test WHERE {")
    }

    test("Sparql Prolog - Two Variables ") {
      assert(SparqlGenerator.prolog(Seq[String]("test","test2")) == "SELECT ?test ?test2 WHERE {")
    }

    test("solutionModifier") {
      assert(SparqlGenerator.solutionModifier() == "}" )
    }

    test("solutionModifierSourcesSelection") {
      assert(SparqlGenerator.solutionModifierSourcesSelection() == "} LIMIT 1" )
    }

    test("sparqlNode - SubjectOf") {
      val v = SparqlGenerator.sparqlNode(new SubjectOf("id",URI("a")),"varSire","varId")
      assert(v == "?varSire <a> ?varId")
    }
  }
}
