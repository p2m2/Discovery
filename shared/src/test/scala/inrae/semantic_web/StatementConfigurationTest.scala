package inrae.semantic_web

import utest._

object StatementConfigurationTest extends TestSuite {
  /**
   * {
   * id   : ep1,
   * type : ldfragment,tps,csv
   * url : "....",
   * method : "POST|GET",
   * auth : "basic|digest|none"
   * login,
   * password,
   * }
   *
   * @return
   */

    /*
     config.source(id).url
     config.source(id).type
     config.source(id).method
     config.source(id).login
     config.source(id).password
     */

  def tests = Tests {
      test("Create a simple source with string configuration") {
        val configDbpediaBasic: StatementConfiguration = new StatementConfiguration()
        configDbpediaBasic.setConfigString(
          """
            |{
            | "sources" : [{
            |   "id"  : "dbpedia",
            |   "url" : "https://dbpedia.org/sparql",
            |   "type" : "tps",
            |   "method" : "POST"
            | }]}
            |""".stripMargin)
      }

    test("Create a simple source") {
      val configDbpediaBasic: StatementConfiguration = new StatementConfiguration()
      configDbpediaBasic.setConfig(StatementConfigurationJson(Seq(Source("dbpedia","http://test","tps"))))

      println(configDbpediaBasic.source("dbpedia"))
    }

    test("Create a query finding a subject") {

    }
  }
}