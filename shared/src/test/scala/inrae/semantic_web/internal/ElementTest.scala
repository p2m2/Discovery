package inrae.semantic_web.internal

import inrae.semantic_web.rdf._
import utest._

import scala.util.{Failure, Success, Try}

object ElementTest extends TestSuite {

  def tests = Tests {
    test("Root creation") {
        val v : Root = Root()
        assert(true)
        assert(v.toString() != "")
        Try(v.addChildren(Root())) match {
          case Success(_) => assert(true)
          case Failure(_) => assert(false)
        }
    }

      test("Root getRdfNode") {
        val v : Root = Root()
        val n = SubjectOf("1234",new URI("test"))

        v.addChildren(n).getRdfNode("1234") match {
          case Some(v) => assert(v == n )
          case None => assert(false)
        }
      }

    test("Something creation with ID") {
      Something("1234")
      assert(true)
    }

    test("SubjectOf creation") {
      SubjectOf("1234",URI("test"))
      assert(true)
    }

    test("ObjectOf creation") {
      ObjectOf("1234",URI("test"))
      assert(true)
    }

    test("LinkTo creation") {
      LinkTo("1234",URI("test"))
      assert(true)
    }

    test("LinkFrom creation") {
      LinkFrom("1234",URI("test"))
      assert(true)
    }

    test("Value creation with uri") {
      Value(URI("test"))
      assert(true)
    }

    test("Value creation with Literal") {
      Value(Literal("test"))
      assert(true)
    }

    test("ListValues creation") {
      ListValues(List(URI("test"),URI("test2"),Literal("test")))
      assert(true)
    }

    test("isBlank creation") {
      isBlank(true)
      assert(true)
    }

    test("isLiteral creation") {
      isLiteral(true)
      assert(true)
    }

    test("isURI creation") {
      isURI(true)
      assert(true)
    }

    test("Contains creation") {
      Contains("h",true)
      assert(true)
    }

    test("Equal creation") {
      Equal("h",true)
      assert(true)
    }

    test("DatatypeNode creation") {
      DatatypeNode("h",SubjectOf("1234",URI("something_property")))
      assert(true)
    }

    test("SourcesNode creation") {
      SourcesNode("h",List("source1","source2"))
      assert(true)
    }

    test("OperatorNode creation") {
      OperatorNode("<")
      assert(true)
    }

  }
}
