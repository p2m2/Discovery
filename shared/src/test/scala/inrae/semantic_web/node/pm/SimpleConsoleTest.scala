package inrae.semantic_web.node.pm
import inrae.semantic_web.node._
import inrae.semantic_web.rdf.URI
import utest.{TestSuite, Tests, assert, test}
object SimpleConsoleTest extends TestSuite {

  def all(consoleColor : Boolean = true,displayRootStyle : Boolean = true) = {
    assert(SimpleConsole(consoleColor,displayRootStyle).get(Something("s")) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(SubjectOf("1234",URI("test"))) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(ObjectOf("1234",URI("test"))) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(LinkTo("1234",URI("test"))) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(LinkFrom("1234",URI("test"))) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(Value(URI("test"))) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(isBlank(true,"")) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(isLiteral(true,"")) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(isURI(true,"")) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(Regex("h","",true,"")) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(Contains("h",true,"")) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(Equal("h",true,"")) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(NotEqual("h",true,"")) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(StrStarts("h",true,"")) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(StrEnds("h",true,"")) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(Inf("0.5",true,"")) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(InfEqual("0.5",true,"")) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(Sup("0.5",true,"")) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(SupEqual("0.5",true,"")) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(DatatypeNode("h",SubjectOf("1234",URI("something_property")),"")) != "")
    assert(SimpleConsole(consoleColor,displayRootStyle).get(SourcesNode("h",List("source1","source2"),"")) != "")
  }

  def tests = Tests {
    test("console 1") {
      all(false,false)
    }
    test("console 2") {
      all(true,false)
    }
    test("console 3") {
      all(false,true)
    }
    test("console 4") {
      all(true,true)
    }
  }
}
