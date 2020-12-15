package inrae.semantic_web.sparql

import utest._

import scala.util.{Failure, Success, Try}

object QueryResultManagerTest extends TestSuite {

  val q1 = "Select ?a { ?a ?b ?c . } "
  val r1 = """{ "head": { "vars": [ "book" , "title" ] } ,"results": { "bindings": []} }"""

  def tests = Tests {
    test("QueryResultManager bad def") {
      val qrm = QueryResultManager()
      qrm.set(q1,r1)
      qrm.get(q1) match {
        case Some(r) if r == r1 => assert(true)
        case _ => assert(false)
      }

      qrm.remove("something bad")
      qrm.remove(q1)

      qrm.get(q1) match {
        case None => assert(true)
        case _ => assert(false)
      }
    }
  }
}
