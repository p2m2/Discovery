package inrae.semantic_web.internal

import inrae.semantic_web.{SW, StatementConfiguration}
import inrae.semantic_web.rdf._
import utest._

object SWTest extends TestSuite {

  def tests = Tests {
    test("Create a simple query") {
      val config: StatementConfiguration = new StatementConfiguration()
      config.setConfigString(""" { "sources" : [] } """)
      val query = new SW(config)
      val r = query.something("h1")
    }

    test("Create a query finding a subject") {
      val config: StatementConfiguration = new StatementConfiguration()
      config.setConfigString(
        """
          |{
          | "sources" : [{
          |   "id"  : "dbpedia",
          |   "url" : "https://dbpedia.org/sparql",
          |   "typ" : "tps",
          |   "method" : "POST"
          | }]}
          |""".stripMargin)
      val query = new SW(config)
      val r = query.something("h1")
        .set(URI("http://dbpedia.org/resource/%C3%84lvdalen"))
        .isSubjectOf(URI("http://www.w3.org/2002/07/owl#sameAs"))
        .select
        .get
      pprint.pprintln(r)
    }
  }
}