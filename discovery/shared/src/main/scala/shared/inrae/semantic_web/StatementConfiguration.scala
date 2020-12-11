package inrae.semantic_web

import inrae.semantic_web.sparql.HttpRequestDriver
import inrae.semantic_web.driver._
import upickle.default.{macroRW, ReadWriter => RW}
import wvlet.log.LogLevel
import wvlet.log.Logger.rootLogger.{error, warn}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

final case class StatementConfigurationException(private val message: String = "",
                                            private val cause: Throwable = None.orNull) extends Exception(message,cause)


/**
 * using doc to validate JSON config:
 * see https://www.playframework.com/documentation/2.8.x/ScalaJson
 *
 * @param json_conf
 */


object ConfigurationObject {
  /* sources configuration */
  case class StatementConfigurationJson(
                                         sources : Seq[Source],
                                         settings : GeneralSetting = new GeneralSetting(),
                                       )
  case class Source(
                     id:String, /* identify the source endpoint */
                     url: String, /* url access */
                     `type`: String="tps",  /* tps, ldf, csv, tps */
                     method: String = "POST", /* POST, POST_ENCODED, GET */
                     auth : String = "", /* basic, digest, bearer, proxy */
                     login : String = "" ,
                     password : String = "",
                     token : String = "",
                     mimetype: String = "application/json"
                   ) {

    val type_legal = List("tps", "ldf", "csv", "tps")

    `type` match {
      case a if ! type_legal.contains(a) => throw StatementConfigurationException(s"type source unknown :${`type`}")
      case _ =>
    }

    val method_legal = List("post","get")

    method.toLowerCase() match {
      case a if ! method_legal.contains(a) => throw StatementConfigurationException(s"method source unknown :${method}")
      case _ =>
    }

    val auth_legal = List("basic", "digest", "bearer", "proxy","")

    auth.toLowerCase() match {
      case a if ! auth_legal.contains(a) => throw StatementConfigurationException(s"auth source not managed :${auth}")
      case _ =>
    }

  }

  case class GeneralSetting(
                      driver: String = "inrae.semantic_web.driver.RosHTTPDriver",
                      cache : Boolean = true,
                      logLevel : String = "warn"          , // trace, debug, info, warn, error, all, off
                      sizeBatchProcessing : Int = 150
                    ) {

    def getHttpDriver() : HttpRequestDriver = {
      import org.portablescala.reflect._

      Reflect.lookupInstantiatableClass(driver) match {
        case Some( cls ) => cls.newInstance().asInstanceOf[HttpRequestDriver]
        case None => throw StatementConfigurationException("Unknown Http Request Driver :"+driver)
      }

    }

    def getLogLevel() : LogLevel = logLevel.toLowerCase() match {
      case "debug" | "d" => LogLevel.DEBUG
      case "info" | "i" => LogLevel.INFO
      case "warn" | "w" => LogLevel.WARN
      case "error" | "e" => LogLevel.ERROR
      case "trace" | "t" => LogLevel.TRACE
      case "all" => LogLevel.ALL
      case "off" => LogLevel.OFF
      case _ => {
        warn("[config.settings] logLevel is not defined. ")
        LogLevel.WARN
      }
    }
  }

  case class Prefixes(list : Map[String,String])

  object Prefixes{
    implicit val rw: RW[Prefixes] = macroRW
  }

  object GeneralSetting{
    implicit val rw: RW[GeneralSetting] = macroRW
  }

  object Source{
    implicit val rw: RW[Source] = macroRW
  }

  object StatementConfigurationJson{
    implicit val rw: RW[StatementConfigurationJson] = macroRW
  }
}

@JSExportTopLevel(name="EasySparqlStatementConfiguration")
case class StatementConfiguration() {

  var conf: ConfigurationObject.StatementConfigurationJson =
    new ConfigurationObject.StatementConfigurationJson(
      Seq[ConfigurationObject.Source](),ConfigurationObject.GeneralSetting())

  /**
   * Set a config using class definition
   * @param conf
   */
  @JSExport
  def setConfig(conf_ext : ConfigurationObject.StatementConfigurationJson) : StatementConfiguration = {
    conf = conf_ext
    this
  }

  /**
   * set a config using string configuration
   * @param json_conf
   */
  @JSExport
  def setConfigString(json_conf: String) : StatementConfiguration = {
    try {
      conf = upickle.default.read[ConfigurationObject.StatementConfigurationJson](json_conf)
    } catch {
      case e1: Throwable => {
        throw StatementConfigurationException(e1.getMessage())
      }
    }
    this
  }

  def source(idname : String) : ConfigurationObject.Source = {
    conf.sources.find(source => source.id == idname ) match {
      case Some(v : ConfigurationObject.Source) => v
      case None => throw new Exception("Unknown source id:"+idname )
    }
  }

  def sources() : Seq[ConfigurationObject.Source] = {
    conf.sources
  }

}
