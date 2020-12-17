package inrae.semantic_web.sparql

import utest._

import scala.io.Source
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


    test("Compression long string") {
      val json_f = Source.fromResource("./json_results/dbpedia_a_isa_concept_10000.json").mkString

      val qrm = QueryResultManager()
      qrm.set("",json_f)

      qrm.get("") match {
        case Some(r) if r == json_f => assert(true)
        case _ => assert(false)
      }
    }
  }
}
