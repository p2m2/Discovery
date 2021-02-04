package inrae.semantic_web

import inrae.data.DataTestFactory
import inrae.semantic_web.rdf._
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object SWDiscoveryFilterTest extends TestSuite {


  val insert_data = DataTestFactory.insert_virtuoso1(
    """
      <http://aaSWFilterTest> <http://propUri> <http://cc> .
      <http://aaSWFilterTest> <http://propLiteral> "test" .
      <http://aaSWFilterTest> <http://propBlank> _:something .

      <http://aaSWFilterTest> <http://propContains> "something regex_expected somethingElse" .
      <http://aaSWFilterTest> <http://propNotContains> "something other test ... somethingElse" .

      <http://aaSWFilterTest> <http://propNum> 1 .
      <http://aaSWFilterTest> <http://propNum> 1.2 .
      <http://aaSWFilterTest> <http://propNum> "2"^^xsd:integer .
      <http://aaSWFilterTest> <http://propNum> "2.3"^^xsd:double .

      <http://aaSWFilterTest> <http://propNum> "5"^^xsd:integer .
      <http://aaSWFilterTest> <http://propNum> 5.1 .
      <http://aaSWFilterTest> <http://propNum> "6"^^xsd:integer .
      <http://aaSWFilterTest> <http://propNum> "5.2"^^xsd:double .

      <http://aaSWFilterTest> <http://propNum> 10 .
      <http://aaSWFilterTest> <http://propNum> 10.2 .
      <http://aaSWFilterTest> <http://propNum> "11"^^xsd:integer .
      <http://aaSWFilterTest> <http://propNum> "11.4"^^xsd:double .

      <http://aaSWFilterTest> <http://propDate> "1790-01-01"^^xsd:date .
      <http://aaSWFilterTest> <http://propDate> "1990-01-01"^^xsd:date .

      """.stripMargin, this.getClass.getSimpleName)

  val config: StatementConfiguration = DataTestFactory.getConfigVirtuoso1()

  def tests = Tests {

    test("SW Filter isLiteral") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .something()
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .isSubjectOf(QueryVariable("prop"))
          .filter.isLiteral
          .root
          .distinct
          .projection(List("prop"))
          .transaction
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 5)
          })
      }).flatten
    }

    test("SW Filter isUri") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .something()
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .isSubjectOf(QueryVariable("prop"))
          .filter.isUri
          .select(List("prop"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 1)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "http://propUri")
          })
      }).flatten
    }

    test("SW Filter isBlank") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .something()
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .isSubjectOf(QueryVariable("prop"))
          .filter.isBlank
          .root
          .distinct
          .projection(List("prop"))
          .transaction
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 1)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "http://propBlank")
          })
      }).flatten
    }

    test("SW Filter contains") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .something("x")
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .isSubjectOf(URI("http://propContains"))
          .filter.contains("regex_expected")
          .select(List("x"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 1)
          })
      }).flatten
    }

    test("SW Filter not contains") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .something()
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .isSubjectOf(URI("http://propContains"))
          .filter.not.contains("regex_expected")
          .select(List("prop"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 0)
          })
      }).flatten
    }
    test("SW Filter not contains 2") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .something()
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .isSubjectOf(URI("propContains"))
          .filter.contains("bidon")
          .filter.not.contains("regex_expected")
          .select(List("prop"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 0)
          })
      }).flatten
    }

    test("SW Filter strStarts") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .something()
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .isSubjectOf(QueryVariable("prop"))
          .filter.strStarts("tes")
          .select(List("prop"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 1)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "http://propLiteral")
          })
      }).flatten
    }

    test("SW Filter strEnds") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .something()
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .isSubjectOf(QueryVariable("prop"))
          .filter.strEnds("est")
          .select(List("prop"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 1)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "http://propLiteral")
          })
      }).flatten
    }

    test("SW Filter equal") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .something()
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .isSubjectOf(QueryVariable("prop"), "value")
          .filter.equal("test")
          .select(List("prop"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 1)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "http://propLiteral")
          })
      }).flatten
    }

    test("SW Filter notEqual") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .something()
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .isSubjectOf(QueryVariable("prop"), "v")
          .filter.notEqual("test")
          .select(List("v"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.map(v => v("v")("value").value).filter(_ == "test").length == 0)
          })
      }).flatten
    }

    test("SW Filter inf") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .something()
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .isSubjectOf(URI("http://propNum"), "value")
          .filter.inf(5)
          .select(List("value"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 4)
          })
      }).flatten
    }

    test("SW Filter inf 2") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .something()
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .isSubjectOf(URI("http://propDate"), "value")
          .filter.inf(Literal("1900-01-01", URI("date", "xsd")))
          .select(List("value"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 1)
          })
      }).flatten
    }

    test("SW Filter infEqual") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .something()
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .isSubjectOf(URI("http://propNum"), "value")
          .filter.infEqual(5)
          .select(List("value"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 5)
          })
      }).flatten
    }

    test("SW Filter Sup") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .something()
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .isSubjectOf(URI("http://propNum"), "value")
          .filter.sup(5)
          .select(List("value"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 7)
          })
      }).flatten
    }

    test("SW Filter SupEqual") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .something()
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .isSubjectOf(URI("http://propNum"), "value")
          .filter.supEqual(5)
          .select(List("value"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 8)
          })
      }).flatten
    }
  }

}
