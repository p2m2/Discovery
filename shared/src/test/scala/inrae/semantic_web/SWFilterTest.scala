package inrae.semantic_web

import inrae.data.DataTestFactory
import inrae.semantic_web.rdf._
import utest._
import wvlet.log.Logger.rootLogger.error

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success}

object SWFilterTest extends TestSuite {
  DataTestFactory.delete_virtuoso1(this.getClass.getSimpleName)

  DataTestFactory.insert_virtuoso1(
    """
      <aaSWFilterTest> <propUri> <cc> .
      <aaSWFilterTest> <propLiteral> "test" .
      <aaSWFilterTest> <propBlank> _:something .

      <aaSWFilterTest> <propContains> "something regex_expected somethingElse" .
      <aaSWFilterTest> <propNotContains> "something other test ... somethingElse" .

      <aaSWFilterTest> <propNum> 1 .
      <aaSWFilterTest> <propNum> 1.2 .
      <aaSWFilterTest> <propNum> "2"^^xsd:integer .
      <aaSWFilterTest> <propNum> "2.3"^^xsd:double .

      <aaSWFilterTest> <propNum> 5 .
      <aaSWFilterTest> <propNum> 5.1 .
      <aaSWFilterTest> <propNum> "6"^^xsd:integer .
      <aaSWFilterTest> <propNum> "5.2"^^xsd:double .

      <aaSWFilterTest> <propNum> 10 .
      <aaSWFilterTest> <propNum> 10.2 .
      <aaSWFilterTest> <propNum> "11"^^xsd:integer .
      <aaSWFilterTest> <propNum> "11.4"^^xsd:double .

      <aaSWFilterTest> <propDate> "1790-01-01"^^xsd:date .
      <aaSWFilterTest> <propDate> "1990-01-01"^^xsd:date .

      """.stripMargin, this.getClass.getSimpleName)

  val config: StatementConfiguration = DataTestFactory.getConfigVirtuoso1()

  def tests = Tests {

    test("SW Filter isLiteral") {
      SW(config)
        .something()
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .isSubjectOf(QueryVariable("prop"))
        .filter.isLiteral
        .select(List("prop"))
        .raw
        .map(result => {
          assert(result("results")("bindings").arr.length == 5)
        })
    }

    test("SW Filter isUri") {
      SW(config)
        .something()
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .isSubjectOf(QueryVariable("prop"))
        .filter.isUri
        .select(List("prop"))
        .raw
        .map(result => {
            assert(result("results")("bindings").arr.length == 1)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "propUri")
        })
    }

    test("SW Filter isBlank") {
      SW(config)
        .something()
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .isSubjectOf(QueryVariable("prop"))
        .filter.isBlank
        .select(List("prop"))
        .raw
        .map(result => {
            assert(result("results")("bindings").arr.length == 1)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "propBlank")
          })
    }

    test("SW Filter contains") {
      SW(config)
        .something()
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .isSubjectOf(QueryVariable("prop"))
        .filter.contains("regex_expected")
        .select(List("prop"))
        .raw
        .map(result => {
          assert(result("results")("bindings").arr.length == 1)
          assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "propContains")
        })
    }

    test("SW Filter strStarts") {
      SW(config)
        .something()
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .isSubjectOf(QueryVariable("prop"))
        .filter.strStarts("tes")
        .select(List("prop"))
        .raw
        .map(result => {
          assert(result("results")("bindings").arr.length == 1)
          assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "propLiteral")
        })
    }

    test("SW Filter strEnds") {
      SW(config)
        .something()
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .isSubjectOf(QueryVariable("prop"))
        .filter.strEnds("est")
        .select(List("prop"))
        .raw
        .map(result => {
          assert(result("results")("bindings").arr.length == 1)
          assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "propLiteral")
        })
    }

    test("SW Filter equal") {
      SW(config)
        .something()
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .isSubjectOf(QueryVariable("prop"),"value")
        .filter.equal("test")
        .select(List("prop"))
        .raw
        .map(result => {
          assert(result("results")("bindings").arr.length == 1)
          assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "propLiteral")
        })
    }

    test("SW Filter notEqual") {
      SW(config)
        .something()
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .isSubjectOf(QueryVariable("prop"),"v")
        .filter.notEqual("test")
        .select(List("value"))
        .raw
        .map(result => {
          assert(result("results")("bindings").arr.map( v => v("v")("value").value ).filter( _ == "test").length == 0)
        })
    }

    test("SW Filter inf") {
      SW(config)
        .something()
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .isSubjectOf(URI("propNum"),"value")
        .filter.inf(5)
        .select(List("value"))
        .raw
        .map(result => {
          assert(result("results")("bindings").arr.length == 4)
        })
    }

    test("SW Filter inf 2") {
      SW(config)
        .something()
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .isSubjectOf(URI("propDate"),"value")
        .filter.inf(Literal("1900-01-01",URI("date","xsd")))
        .select(List("value"))
        .raw
        .map(result => {
          assert(result("results")("bindings").arr.length == 1)
        })
    }

    test("SW Filter infEqual") {
      SW(config)
        .something()
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .isSubjectOf(URI("propNum"),"value")
        .filter.infEqual(5)
        .select(List("value"))
        .raw
        .map(result => {
          assert(result("results")("bindings").arr.length == 5)
        })
    }

    test("SW Filter Sup") {
      SW(config)
        .something()
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .isSubjectOf(URI("propNum"),"value")
        .filter.sup(5)
        .select(List("value"))
        .raw
        .map(result => {
          assert(result("results")("bindings").arr.length == 7)
        })
    }

    test("SW Filter SupEqual") {
      SW(config)
        .something()
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .isSubjectOf(URI("propNum"),"value")
        .filter.supEqual(5)
        .select(List("value"))
        .raw
        .map(result => {
          assert(result("results")("bindings").arr.length == 8)
        })
    }
  }
}
