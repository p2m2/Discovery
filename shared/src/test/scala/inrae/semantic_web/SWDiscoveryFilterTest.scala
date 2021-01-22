package inrae.semantic_web

import inrae.data.DataTestFactory
import inrae.semantic_web.rdf._
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object SWDiscoveryFilterTest extends TestSuite {


  val insert_data = DataTestFactory.insert_virtuoso1(
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
      insert_data.map(_ => {
        SWDiscovery(config)
          .something()
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .isSubjectOf(QueryVariable("prop"))
          .filter.isLiteral
          .select(List("prop"))
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
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "propUri")
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
          .select(List("prop"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 1)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "propBlank")
          })
      }).flatten
    }

    test("SW Filter contains") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .something("x")
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .isSubjectOf(URI("propContains"))
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
          .isSubjectOf(URI("propContains"))
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
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "propLiteral")
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
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "propLiteral")
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
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("prop")).localName == "propLiteral")
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
          .select(List("value"))
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
          .isSubjectOf(URI("propNum"), "value")
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
          .isSubjectOf(URI("propDate"), "value")
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
          .isSubjectOf(URI("propNum"), "value")
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
          .isSubjectOf(URI("propNum"), "value")
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
          .isSubjectOf(URI("propNum"), "value")
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

  TestRunner.runAsync(tests).map { _ => {
    DataTestFactory.delete_virtuoso1(this.getClass.getSimpleName)
  }
  }

}
