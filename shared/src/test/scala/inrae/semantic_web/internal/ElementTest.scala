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

    test("Attribute creation") {
      val v : Attribute = new Attribute("1234",new URI("test"))
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
    /*
    object TutorialApp {
  def main(args: Array[String]): Unit = {
    println("Hello world!")
    val query = new SW()
    val r = query.something("h1")
               .set(URI("http://dbpedia.org/resource/%C3%84lvdalen"))
               .isSubjectOf(URI("http://www.w3.org/2002/07/owl#sameAs"))
               .select()
  }
}
     */
  }
}
/*

class Something(uniqRef : String) extends ReferenceNode(uniqRef)
class SubjectOf(uniqRef : String, var uri : URI) extends ReferenceNode(uniqRef)
class ObjectOf(uniqRef : String, var uri : URI) extends ReferenceNode(uniqRef)
class Attribute(uniqRef : String, var uri : URI) extends ReferenceNode(uniqRef)
class Value
 */