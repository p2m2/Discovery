package inrae.semantic_web.rdf

import utest._

object SparqlDefinitionTest$ extends TestSuite {

  def tests = Tests {
    test("Create URI object with localname/namespace") {
      val value: URI = URI("local","namespace")

      assert( value.toString() == "namespace:local")
    }

    test("Create URI object with uri form") {
      val value: URI = URI("http://test.org/namespace")
      assert( value.toString() == "<http://test.org/namespace>")
    }
  }
}