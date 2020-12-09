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
            {
             "sources" : [{
               "id"  : "dbpedia",
               "url" : "https://dbpedia.org/sparql",
               "type" : "tps",
               "method" : "POST"
             }]}
            """.stripMargin)
      }

    test("Create a simple source") {
      val configDbpediaBasic: StatementConfiguration = new StatementConfiguration()
      val dbname = "dbpedia"
      val url= "http://test"
      val `type` = "tps"
      configDbpediaBasic.setConfig(ConfigurationObject.StatementConfigurationJson(
        Seq(ConfigurationObject.Source(dbname,url,`type`))))
      val source = configDbpediaBasic.source("dbpedia")

      assert( source.id == dbname )
      assert( source.url == url )
      assert( source.`type` == `type` )
    }

    test("Create a config with a bad tag ") {

      val configDbpediaBasic: StatementConfiguration = new StatementConfiguration()
      configDbpediaBasic.setConfigString(
        """
          {
           "hello" : [{
           }]}
          """.stripMargin)
    }

    test("Create a request config without source ") {

      val configDbpediaBasic: StatementConfiguration = new StatementConfiguration()
      try {
        configDbpediaBasic.setConfigString(
          """
            {
             "settings" : {
                "driver" : "hello.world"
             }
            """.stripMargin)
        assert(false)
      } catch {
        case _ : Throwable => assert(true)
      }
    }
  }
}