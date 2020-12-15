package inrae.semantic_web.internal

import inrae.semantic_web.internal._
import inrae.semantic_web.rdf._
import utest._

object ElementTest extends TestSuite {

  def tests = Tests {
    test("Root creation") {
        val v : Root = new Root()
        assert(true)
    }

    test("Something creation with ID") {
      val v : Something = new Something("1234")
      assert(true)
    }

    test("SubjectOf creation") {
      val v : SubjectOf = new SubjectOf("1234",new URI("test"))
      assert(true)
    }

    test("ObjectOf creation") {
      val v : ObjectOf = new ObjectOf("1234",new URI("test"))
      assert(true)
    }

    test("Value creation with uri") {
      val v : Value = new Value(new URI("test"))
      assert(true)
    }
    test("Value creation with Literal") {
      val v : Value = new Value(new Literal("test"))
      assert(true)
    }
  }
}