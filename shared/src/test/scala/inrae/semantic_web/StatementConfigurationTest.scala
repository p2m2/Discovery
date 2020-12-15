package inrae.semantic_web

import utest._
import wvlet.log.LogLevel

import scala.util.{Failure, Success, Try}

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

    test("Get a unknown source") {
      val c = StatementConfiguration()
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

    test("Create a request config an unknown driver ") {
      Try(StatementConfiguration()
        .setConfigString(
          """
            {
              {
               "sources" : [{
                 "id"  : "dbpedia",
                 "url" : "https://dbpedia.org/sparql",
                 "type" : "tps",
                 "method" : "POST"
               }],
               "settings" : {
                  "driver" : "hello.world"
               }
            }
            """.stripMargin)) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("Create a request config an unknown log level ") {
      assert(StatementConfiguration()
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
                  "logLevel" : "hello.world"
               }
             }
            """.stripMargin).conf.settings.getLogLevel() == LogLevel.WARN)
    }

    test("Create a request config log level debug ") {
      Try(StatementConfiguration()
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
                  "logLevel" : "debug"
               }
             }
            """.stripMargin).conf.settings.getLogLevel() == LogLevel.DEBUG) match {
        case Success(_) => assert(true)
        case Failure(_) => assert(false)
      }
    }

    test("Create a request config log level info ") {

      val c = StatementConfiguration()
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
                  "logLevel" : "info"
               }
             }
            """.stripMargin)
      assert(c.conf.settings.getLogLevel() == LogLevel.INFO)

    }
    test("Create a request config log level trace ") {
      val c = StatementConfiguration()
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
                  "logLevel" : "trace"
               }
             }
            """.stripMargin)
      assert(c.conf.settings.getLogLevel() == LogLevel.TRACE)
    }
    test("Create a request config log level warn ") {
      val c = StatementConfiguration()
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
                  "logLevel" : "warn"
               }
             }
            """.stripMargin)
      assert(c.conf.settings.getLogLevel() == LogLevel.WARN)
    }

    test("Create a request config log level error ") {
      val c = StatementConfiguration()
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
                  "logLevel" : "error"
               }
             }
            """.stripMargin)
      assert(c.conf.settings.getLogLevel() == LogLevel.ERROR)
    }

    test("Create a request config log level all ") {
      val c = StatementConfiguration()
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
                  "logLevel" : "all"
               }
             }
            """.stripMargin)
      assert(c.conf.settings.getLogLevel() == LogLevel.ALL)
    }

    test("Create a request config log level off ") {
      val c = StatementConfiguration()
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
                  "logLevel" : "off"
               }
             }
            """.stripMargin)
      assert(c.conf.settings.getLogLevel() == LogLevel.OFF)
    }
  }
}
