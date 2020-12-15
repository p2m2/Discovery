package inrae.semantic_web

import utest._

object StatementConfigurationTest extends TestSuite {

  def tests = Tests {
    test("Create a simple source with string configuration") {
      StatementConfiguration()
        .setConfigString(
        """
            {
             "sources" : [{
               "id"  : "dbpedia",
               "url" : "https://dbpedia.org/sparql",
               "type" : "tps",
               "method" : "POST"
             }],
             "settings" : {
               "logLevels" : "info"
             }
            }
            """.stripMargin)
    }

    test("Create a simple source") {

      val configDbpediaBasic: StatementConfiguration = StatementConfiguration()
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

      try {
        StatementConfiguration()
          .setConfigString(
          """
          {
           "hello" : [{
           }]}
          """.stripMargin)
        assert(false)
      } catch {
        case _ : Throwable => assert(true)
      }
    }

    test("Create a request config without source ") {
      try {
        StatementConfiguration()
          .setConfigString(
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