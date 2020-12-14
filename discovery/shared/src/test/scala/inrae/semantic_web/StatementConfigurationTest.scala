package inrae.semantic_web

import utest._
import wvlet.log.LogLevel

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
      try {
        c.source("something")
        assert(false)
      } catch {
        case _ : Throwable => assert(true)
      }
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

    test("Create a request config an unknown driver ") {
      try {
        StatementConfiguration()
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
            """.stripMargin)
        assert(false)
      } catch {
        case _ : Throwable => assert(true)
      }
    }

    test("Create a request config an unknown log level ") {
      try {
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
                  "logLevel" : "hello.world"
               }
             }
            """.stripMargin)
        assert(true)
      } catch {
        case e : Throwable => {
          assert(false)
        }
      }
    }

    test("Create a request config log level debug ") {
      try {
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
                  "logLevel" : "debug"
               }
             }
            """.stripMargin)
        assert(c.conf.settings.getLogLevel() == LogLevel.DEBUG)
      } catch {
        case e : Throwable => {
          assert(false)
        }
      }
    }

    test("Create a request config log level info ") {
      try {
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
      } catch {
        case e : Throwable => {
          assert(false)
        }
      }
    }
    test("Create a request config log level trace ") {
      try {
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
      } catch {
        case e : Throwable => {
          assert(false)
        }
      }
    }
    test("Create a request config log level warn ") {
      try {
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
      } catch {
        case e : Throwable => {
          assert(false)
        }
      }
    }

    test("Create a request config log level error ") {
      try {
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
      } catch {
        case e : Throwable => {
          assert(false)
        }
      }
    }

    test("Create a request config log level all ") {
      try {
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
      } catch {
        case e : Throwable => {
          assert(false)
        }
      }
    }

    test("Create a request config log level off ") {
      try {
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
      } catch {
        case e : Throwable => {
          assert(false)
        }
      }
    }

  }
}