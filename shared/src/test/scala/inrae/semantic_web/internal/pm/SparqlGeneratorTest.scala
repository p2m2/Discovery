package inrae.semantic_web.internal.pm

import inrae.semantic_web.internal._
import inrae.semantic_web.rdf._
import utest.{TestSuite, Tests, assert, test}

import scala.util.{Failure, Success, Try}

object SparqlGeneratorTest extends TestSuite {
  def tests = Tests {

    test("prefixes") {
      val m : Map[String,IRI] = Map("some"-> "http://something","some2"->"http://something2")
      assert(SparqlGenerator.prefixes(m).toLowerCase().contains("prefix"))
      assert(SparqlGenerator.prefixes(m).toLowerCase().contains("some:"))
      assert(SparqlGenerator.prefixes(m).toLowerCase().contains("some2:"))
      assert(SparqlGenerator.prefixes(m).toLowerCase().contains("http://something"))
      assert(SparqlGenerator.prefixes(m).toLowerCase().contains("http://something2"))
    }

    test("from") {
      val l : Seq[IRI] = List("http://something","http://something2")
      assert(SparqlGenerator.from(l).toLowerCase().contains("from"))
    }

    test("fromNamed") {
      val l : Seq[IRI] = List("http://something","http://something2")
      assert(SparqlGenerator.fromNamed(l).toLowerCase().contains("from named"))
    }

    test("Sparql Prolog - Variable list empty") {
      val v = SparqlGenerator.queryFormSelect(Seq[String]()).toLowerCase()
      assert(v.contains("*"))//assert(SparqlGenerator.prolog(Seq[String]().contains("*")) == "SELECT * WHERE {")
      assert(v.contains("select"))
    }

    test("Sparql Prolog - One Variable ") {
      val v = SparqlGenerator.queryFormSelect(Seq[String]("test")).toLowerCase()
      assert(v.contains("?test"))
      assert(v.contains("select"))
    }

    test("Sparql Prolog - Two Variables ") {
      val v = SparqlGenerator.queryFormSelect(Seq[String]("test","test2")).toLowerCase()
      assert(v.contains("?test"))
      assert(v.contains("?test2"))
      assert(v.contains("select"))
    }

    test("start_where") {
      assert(SparqlGenerator.start_where().toLowerCase().contains("where"))
    }


    test("solutionModifier") {
      assert(SparqlGenerator.solutionModifier().contains("}"))
    }

    test("prologCountSelection") {
      assert(SparqlGenerator.prologCountSelection("myvar").toLowerCase().contains("count"))
      assert(SparqlGenerator.prologCountSelection("myvar").toLowerCase().contains("myvar"))
    }

    test("solutionModifierSourcesSelection") {
      assert(SparqlGenerator.solutionModifierSourcesSelection() == "} LIMIT 1" )
    }

    test("sparqlNode - SubjectOf") {
      val map = Map( "varSire" -> "varId")
      val v = SparqlGenerator.sparqlNode(new SubjectOf("id",URI("http://test")),map,"varSire","varId")
      assert(v.contains("?varSire <http://test> ?varId"))
    }

    test("queryVariableTransform URI") {
      assert(SparqlGenerator.queryVariableTransform(URI("s"),Map()) == URI("s"))
    }

    test("queryVariableTransform Literal") {
      assert(SparqlGenerator.queryVariableTransform(Literal("s"),Map()) == Literal("s"))
    }

    test("queryVariableTransform PropertyPath") {
      assert(SparqlGenerator.queryVariableTransform(PropertyPath("s"),Map()) == PropertyPath("s"))
    }

    test("queryVariableTransform Anonymous") {
      assert(SparqlGenerator.queryVariableTransform(Anonymous("s"),Map()) == Anonymous("s"))
    }

    test("queryVariableTransform IRI") {
      assert(SparqlGenerator.queryVariableTransform(IRI("s"),Map()) == IRI("s"))
    }

    test("queryVariableTransform QueryVariable") {
      Try(assert(SparqlGenerator.queryVariableTransform(QueryVariable("s"), Map()) == QueryVariable("s"))) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("queryVariableTransform QueryVariable") {
      val m = Map("s"->"strans")
      assert(SparqlGenerator.queryVariableTransform(QueryVariable("s"), m) == QueryVariable("strans"))
    }

    test("sparqlNode Something") {
      val v = SparqlGenerator.sparqlNode(Something("1234"),Map(),"nothingSire","nothingVar")
      assert(v.toLowerCase() == "")
    }

    test("sparqlNode SubjectOf") {
      val v = SparqlGenerator.sparqlNode(SubjectOf("1234",URI("test")),Map(),"nothingSire","nothingVar")
      assert(v.trim().split(" ").toList == List("?nothingSire","<test>","?nothingVar","."))
    }

    test("sparqlNode ObjectOf") {
      val v = SparqlGenerator.sparqlNode(ObjectOf("1234",URI("test")),Map(),"nothingSire","nothingVar")
      assert(v.trim().split(" ").toList == List("?nothingVar","<test>","?nothingSire","."))
    }

    test("sparqlNode LinkTo") {
      val v = SparqlGenerator.sparqlNode(LinkTo("1234",URI("test")),Map(),"nothingSire","nothingVar")
      assert(v.trim().split(" ").toList == List("?nothingSire","?nothingVar","<test>","."))
    }

    test("sparqlNode LinkFrom") {
      val v = SparqlGenerator.sparqlNode(LinkFrom("1234",URI("test")),Map(),"nothingSire","nothingVar")
      assert(v.trim().split(" ").toList == List("<test>","?nothingVar","?nothingSire","."))
    }

    test("sparqlNode Value") {
      val v = SparqlGenerator.sparqlNode(Value(URI("test")),Map(),"nothingSire","nothingVar")
      assert(v.trim().split(" ").toList == List("VALUES","?nothingSire","{","<test>","}","."))
    }

    test("sparqlNode ListValues") {
      val v = SparqlGenerator.sparqlNode(ListValues(List(URI("test"),URI("test2"))),Map(),"nothingSire","nothingVar")
      assert(v.trim().split(" ").toList == List("VALUES","?nothingSire","{","<test>","<test2>","}","."))
    }

    test("sparqlNode isBlank neg") {
      val v = SparqlGenerator.sparqlNode(isBlank(true),Map(),"nothingSire","nothingVar")
      assert(v.trim().split(" ").toList == List("FILTER","(","!isBlank(?nothingSire)",")"))
    }

    test("sparqlNode isBlank") {
      val v = SparqlGenerator.sparqlNode(isBlank(false),Map(),"nothingSire","nothingVar")
      assert(v.trim().split(" ").toList == List("FILTER","(","isBlank(?nothingSire)",")"))
    }

    test("sparqlNode isLiteral neg") {
      val v = SparqlGenerator.sparqlNode(isLiteral(true),Map(),"nothingSire","nothingVar")
      assert(v.trim().split(" ").toList == List("FILTER","(","!isLiteral(?nothingSire)",")"))
    }

    test("sparqlNode isLiteral") {
      val v = SparqlGenerator.sparqlNode(isLiteral(false),Map(),"nothingSire","nothingVar")
      assert(v.trim().split(" ").toList == List("FILTER","(","isLiteral(?nothingSire)",")"))
    }

    test("sparqlNode isURI neg") {
      val v = SparqlGenerator.sparqlNode(isURI(true),Map(),"nothingSire","nothingVar")
      assert(v.trim().split(" ").toList == List("FILTER","(","!isURI(?nothingSire)",")"))
    }

    test("sparqlNode isURI") {
      val v = SparqlGenerator.sparqlNode(isURI(false),Map(),"nothingSire","nothingVar")
      assert(v.trim().split(" ").toList == List("FILTER","(","isURI(?nothingSire)",")"))
    }

    test("sparqlNode Contains neg") {
      val v = SparqlGenerator.sparqlNode(Contains("h",true),Map(),"nothingSire","nothingVar")
      assert(v.trim().split(" ").toList == List("FILTER","(","!contains(str(?nothingSire),\"h\")",")"))
    }
    test("sparqlNode Contains") {

      val v = SparqlGenerator.sparqlNode(Contains("h",false),Map(),"nothingSire","nothingVar")
      assert(v.trim().split(" ").toList == List("FILTER","(","contains(str(?nothingSire),\"h\")",")"))
    }

    test("sparqlNode Equal neg") {
      val v = SparqlGenerator.sparqlNode(Equal("h",true),Map(),"nothingSire","nothingVar")
      assert(v.trim().split(" ").toList == List("FILTER","(","!(str(?nothingSire)==\"h\")",")"))
    }

    test("sparqlNode Equal") {
      val v = SparqlGenerator.sparqlNode(Equal("h",false),Map(),"nothingSire","nothingVar")
      assert(v.trim().split(" ").toList == List("FILTER","(","(str(?nothingSire)==\"h\")",")"))
    }

    test("sparqlNode DatatypeNode") {
      Try(SparqlGenerator.sparqlNode(DatatypeNode("h", SubjectOf("1234", URI("something_property"))), Map(),
        "nothingSire", "nothingVar")) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("sparqlNode SourcesNode") {
      Try(SparqlGenerator.sparqlNode(SourcesNode("h",List("source1","source2")), Map(), "nothingSire", "nothingVar")) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("sparqlNode OperatorNode") {
      Try(SparqlGenerator.sparqlNode(OperatorNode("<"), Map(), "nothingSire", "nothingVar")) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

  }
}
