package inrae.semantic_web

import utest._
import wvlet.log.LogLevel

import scala.util.{Failure, Success, Try}

object StatementConfigurationTest extends TestSuite {
  val config_base = """
            {
             "sources" : [{
               "id"  : "dbpedia",
               "url" : "https://dbpedia.org/sparql",
               "type" : "tps",
               "method" : "POST"
             }],
             "settings" : {
               "driver" : "inrae.semantic_web.driver.RosHTTPDriver",
               "cache" : true,
               "logLevel" : "info",
               "sizeBatchProcessing" : 10,
               "pageSize" : 10
             }
            }
            """.stripMargin

  def tests = Tests {
    test("Create a simple source with string configuration") {
      StatementConfiguration().setConfigString(config_base)
    }

    test("Get a unknown source") {
      val c = StatementConfiguration().setConfigString(config_base)

      Try(c.source("something")) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("Create a simple source") {

      val configDbpediaBasic: StatementConfiguration = StatementConfiguration()
      val dbname = "dbpedia"
      val url = "http://test"
      val `type` = "tps"

      configDbpediaBasic.setConfig(ConfigurationObject.StatementConfigurationJson(
        Seq(ConfigurationObject.Source(dbname, url, `type`))))
      val source = configDbpediaBasic.source("dbpedia")

      assert(source.id == dbname)
      assert(source.url == url)
      assert(source.`type` == `type`)
    }

    test("Create a config with a bad tag ") {
      Try(StatementConfiguration()
        .setConfigString(
          """
          {
           "hello" : [{
           }]}
          """.stripMargin)) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("Create a request config without source ") {
      Try(StatementConfiguration()
        .setConfigString(
          """
            {
             "settings" : {
                "driver" : "hello.world"
             }
            """.stripMargin)) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("Create a request config with an unknown driver ") {
      Try(StatementConfiguration()
        .setConfigString(config_base.replace("inrae.semantic_web.driver.RosHTTPDriver",
          "hello.world"))) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("Create a request config with an unknown log level ") {
      assert(StatementConfiguration()
        .setConfigString(config_base.replace("\"info\"",
          "\"hello.world\"")).conf.settings.getLogLevel() == LogLevel.WARN)
    }

    test("Create a request config log level debug ") {
      Try(StatementConfiguration()
        .setConfigString(config_base.replace("\"info\"",
          "\"debug\"")).conf.settings.getLogLevel() == LogLevel.DEBUG) match {
        case Success(_) => assert(true)
        case Failure(_) => assert(false)
      }
    }

    test("Create a request config log level info ") {

      val c = StatementConfiguration()
        .setConfigString(config_base)
      assert(c.conf.settings.getLogLevel() == LogLevel.INFO)

    }
    test("Create a request config log level trace ") {
      val c = StatementConfiguration()
        .setConfigString(config_base.replace("\"info\"",
          "\"trace\""))
      assert(c.conf.settings.getLogLevel() == LogLevel.TRACE)
    }
    test("Create a request config log level warn ") {
      val c = StatementConfiguration()
        .setConfigString(config_base.replace("\"info\"",
          "\"warn\""))
      assert(c.conf.settings.getLogLevel() == LogLevel.WARN)
    }

    test("Create a request config log level error ") {
      val c = StatementConfiguration()
        .setConfigString(config_base.replace("\"info\"",
          "\"error\""))
      assert(c.conf.settings.getLogLevel() == LogLevel.ERROR)
    }

    test("Create a request config log level all ") {
      val c = StatementConfiguration()
        .setConfigString(config_base.replace("\"info\"",
          "\"all\""))
      assert(c.conf.settings.getLogLevel() == LogLevel.ALL)
    }

    test("Create a request config log level off ") {
      val c = StatementConfiguration()
        .setConfigString(config_base.replace("\"info\"",
          "\"off\""))
      assert(c.conf.settings.getLogLevel() == LogLevel.OFF)
    }
  }
}
