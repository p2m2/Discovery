package inrae.semantic_web.internal.pm

import inrae.semantic_web.internal._
import inrae.semantic_web.sparql._
import inrae.semantic_web.rdf._
import utest.{TestSuite, Tests, assert, test}

object SparqlGeneratorTest extends TestSuite {
  def tests = Tests {
    test("Sparql Prolog - Variable list empty") {
      val v = SparqlGenerator.prolog(Seq[String]()).toLowerCase()
      assert(v.contains("*"))//assert(SparqlGenerator.prolog(Seq[String]().contains("*")) == "SELECT * WHERE {")
      assert(v.contains("select"))
      assert(v.contains("where"))
      assert(v.contains("{"))
    }

    test("Sparql Prolog - One Variable ") {
      val v = SparqlGenerator.prolog(Seq[String]("test")).toLowerCase()
      assert(v.contains("?test"))
      assert(v.contains("select"))
      assert(v.contains("where"))
      assert(v.contains("{"))
    }

    test("Sparql Prolog - Two Variables ") {
      val v = SparqlGenerator.prolog(Seq[String]("test","test2")).toLowerCase()
      assert(v.contains("?test"))
      assert(v.contains("?test2"))
      assert(v.contains("select"))
      assert(v.contains("where"))
      assert(v.contains("{"))
    }

    test("solutionModifier") {
      val v = SparqlGenerator.solutionModifier()
      assert(v.contains("}"))
    }

    test("solutionModifierSourcesSelection") {
      assert(SparqlGenerator.solutionModifierSourcesSelection() == "} LIMIT 1" )
    }

    test("sparqlNode - SubjectOf") {
      val v = SparqlGenerator.sparqlNode(new SubjectOf("id",URI("http://test")),"varSire","varId")
      assert(v.contains("?varSire <http://test> ?varId"))
    }
  }
}
