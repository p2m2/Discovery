package inrae.semantic_web.rdf

import utest._

object SparqlDefinitionTest extends TestSuite {

  def tests = Tests {
    test("URI object with localname/namespace") {
      val value: URI = URI("local","namespace")

      assert( value.toString() == "namespace:local")
      assert( value.sparql == "namespace:local")
      assert( value.naiveLabel == "local")
    }

    test("URI object with uri form") {
      val value: URI = URI("http://test.org/namespace")
      assert( value.toString() == "<http://test.org/namespace>")
      assert( value.sparql == "<http://test.org/namespace>")
      assert( value.naiveLabel == "namespace")
    }

    test("URI namespace:object") {
      val value: URI = URI("namespace:obj")
      assert( value.toString() == "namespace:obj")
      assert( value.sparql == "namespace:obj")
      assert( value.naiveLabel == "obj")
    }

    test("IRI") {
      val value : IRI = IRI("http://test.org/namespace")
      assert( value.toString() == "<http://test.org/namespace>")
      assert( value.sparql == "<http://test.org/namespace>")
      assert( value.naiveLabel == "namespace")
    }

    test("IRI implicit") {
      val value : IRI = "http://test.org/namespace"
    }

    test("Anonymous") {
      val value : Anonymous = Anonymous("something")
      assert( value.toString() == "something")
      assert( value.sparql == "something")
      assert( value.naiveLabel == "Anonymous[something]")
    }

    test("PropertyPath") {
      val value : PropertyPath = PropertyPath("something*/something2+")
      assert( value.toString() == "something*/something2+")
      assert( value.sparql == "something*/something2+")
      assert( value.naiveLabel == "PropertyPath[something*/something2+]")
    }

    test("PropertyPath implicit") {
      val value : PropertyPath = "http://test.org/namespace"
    }

    test("Literal default") {
      val value : Literal = Literal("test")
      assert( value.toString() == "\"test\"")
      assert( value.sparql == "\"test\"")
      assert( value.naiveLabel == "test")
    }

    test("Literal xsd:string") {
      val value : Literal = Literal("test","xsd:string")
      assert( value.toString() == "\"test\"^^xsd:string")
      assert( value.sparql == "\"test\"^^xsd:string")
      assert( value.naiveLabel == "test")
    }

    test("Literal <something> type") {
      val value : Literal = Literal("test","<something>")
      assert( value.toString() == "\"test\"^^<something>")
      assert( value.sparql == "\"test\"^^<something>")
      assert( value.naiveLabel == "test")
    }

    test("Literal xsd:string and tag") {
      val value : Literal = Literal("test","xsd:string", "fr")
      assert( value.toString() == "\"test\"@fr")
      assert( value.sparql == "\"test\"@fr")
      assert( value.naiveLabel == "test")
    }

    test("Literal implicit") {
      val value : Literal = "0.5"
      assert( value.toString() == "\"0.5\"")
      assert( value.sparql == "\"0.5\"")
      assert( value.naiveLabel == "0.5")
    }

    test("createUri") {
      val value = SparqlBuilder.createUri(ujson.Value("{ \"value\": \"test\"}"))
      assert( value.toString() == "<test>")
      assert( value.sparql == "<test>")
      assert( value.naiveLabel == "test")
    }

    test("createLiteral") {
      val value = SparqlBuilder.createLiteral(ujson.Value("{ \"value\": \"test\" }"))
      assert( value.toString() == "\"test\"")
      assert( value.sparql == "\"test\"")
      assert( value.naiveLabel == "test")
    }

    test("createLiteral") {
      val value = SparqlBuilder.createLiteral(ujson.Value("{ \"value\": \"test\" , \"datatype\" : \"\" }"))
      assert( value.toString() == "\"test\"")
      assert( value.sparql == "\"test\"")
      assert( value.naiveLabel == "test")
    }

    test("createLiteral tag") {
      val ujsonv = ujson.Value("{ \"value\": \"test\" , \"datatype\" : \"\"  , \"tag\":\"fr\"}")
      val value = SparqlBuilder.createLiteral(ujsonv)
      assert( value.toString() == "\"test\"@fr")
      assert( value.sparql == "\"test\"@fr")
      assert( value.naiveLabel == "test")
    }

    test("create uri") {
      val ujsonv = ujson.Value("{  \"type\" : \"uri\" ,\"value\": \"test\"}")
      val value = SparqlBuilder.create(ujsonv)
      assert( value.toString() == "<test>")
      assert( value.sparql == "<test>")
      assert( value.naiveLabel == "test")
    }

    test("create literal") {
      val ujsonv = ujson.Value("{ \"type\" : \"literal\" , \"value\": \"test\" , \"datatype\" : \"\"  , \"tag\":\"fr\"}")
      val value = SparqlBuilder.create(ujsonv)
      assert( value.toString() == "\"test\"@fr")
      assert( value.sparql == "\"test\"@fr")
      assert( value.naiveLabel == "test")
    }

    test("create literal") {
      val ujsonv = ujson.Value("{ \"type\" : \"typed-literal\" , \"value\": \"test\" , \"datatype\" : \"\"  , \"tag\":\"fr\"}")
      val value = SparqlBuilder.create(ujsonv)
      assert( value.toString() == "\"test\"@fr")
      assert( value.sparql == "\"test\"@fr")
      assert( value.naiveLabel == "test")
    }

  }
}
