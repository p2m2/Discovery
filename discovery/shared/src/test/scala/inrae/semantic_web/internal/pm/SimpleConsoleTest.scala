package inrae.semantic_web.internal.pm
import inrae.semantic_web.internal._
import inrae.semantic_web.rdf.{Literal, URI}
import utest.{TestSuite, Tests, assert, test}
object SimpleConsoleTest extends TestSuite {
  def tests = Tests {
    test("get String") {
      assert(SimpleConsole.get(Something("s")) != "")
      assert(SimpleConsole.get(SubjectOf("1234",URI("test"))) != "")
      assert(SimpleConsole.get(ObjectOf("1234",URI("test"))) != "")
      assert(SimpleConsole.get(LinkTo("1234",URI("test"))) != "")
      assert(SimpleConsole.get(LinkFrom("1234",URI("test"))) != "")
      assert(SimpleConsole.get(Value(URI("test"))) != "")
      assert(SimpleConsole.get(isBlank(true)) != "")
      assert(SimpleConsole.get(isLiteral(true)) != "")
      assert(SimpleConsole.get(isURI(true)) != "")
      assert(SimpleConsole.get(Contains("h",true)) != "")
      assert(SimpleConsole.get(Equal("h",true)) != "")
      assert(SimpleConsole.get(DatatypeNode("h",SubjectOf("1234",URI("something_property")))) != "")
      assert(SimpleConsole.get(SourcesNode("h",List("source1","source2"))) != "")
      assert(SimpleConsole.get(OperatorNode("<")) != "")
    }
  }
}
