package inrae.semantic_web

import inrae.semantic_web.sparql.HttpRequestDriver
import upickle.default.{macroRW, ReadWriter => RW}
import wvlet.log.LogLevel
import wvlet.log.Logger.rootLogger.warn

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.util.{Failure, Success}

final case class StatementConfigurationException(private val message: String = "",
                                            private val cause: Throwable = None.orNull) extends Exception(message,cause)


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
      case a if ! method_legal.contains(a) => throw StatementConfigurationException("method source unknown :" + method)
      case _ =>
    }

    val auth_legal = List("basic", "digest", "bearer", "proxy","")

    auth.toLowerCase() match {
      case a if ! auth_legal.contains(a) => throw StatementConfigurationException(s"auth source not managed :$auth")
      case _ =>
    }

  }

  case class GeneralSetting(
                      driver: String = "inrae.semantic_web.driver.RosHTTPDriver",
                      cache : Boolean = true,
                      logLevel : String = "warn"          , // trace, debug, info, warn, error, all, off
                      sizeBatchProcessing : Int = 150,
                      pageSize : Int = 20
                    ) {

    def getHttpDriver: HttpRequestDriver = {
      import org.portablescala.reflect._
      Reflect.lookupInstantiatableClass(driver) match {
        case Some( cls ) => cls.newInstance().asInstanceOf[HttpRequestDriver]
        case None => throw StatementConfigurationException("Unknown Http Request Driver :"+driver)
      }
    }
    /* check if driver exist when config is loaded . */
    getHttpDriver

    def getLogLevel: LogLevel = logLevel.toLowerCase() match {
      case "debug" | "d" => LogLevel.DEBUG
      case "info" | "i" => LogLevel.INFO
      case "warn" | "w" => LogLevel.WARN
      case "error" | "e" => LogLevel.ERROR
      case "trace" | "t" => LogLevel.TRACE
      case "all" => LogLevel.ALL
      case "off" => LogLevel.OFF
      case _ =>
        warn("[config.settings.logLevel] possible value : trace, debug, info, warn, error, all, off . find ["+logLevel+"]")
        LogLevel.WARN
    }

    if ( pageSize<=0 ) {
      throw StatementConfigurationException("pageSize can not be equal to zero or negative !")
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

@JSExportTopLevel(name="SWDiscoveryConfiguration")
object StatementConfiguration {
  implicit val rw: RW[StatementConfiguration] = macroRW
  /**
   * Set a config using class definition
   * @param conf_ext : configuration
   */
  @JSExport
  def setConfig(conf_ext : ConfigurationObject.StatementConfigurationJson) : StatementConfiguration = StatementConfiguration(conf_ext)

  /**
   * set a config using string configuration
   * @param json_conf : configuration in json format
   */
  @JSExport
  def setConfigString(json_conf: String) : StatementConfiguration = {
    util.Try(upickle.default.read[ConfigurationObject.StatementConfigurationJson](json_conf))
    match {
      case Success(v) => StatementConfiguration(v)
      case Failure(e) => throw StatementConfigurationException(e.getMessage)
    }
  }

}

@JSExportTopLevel(name="StatementConfiguration")
case class StatementConfiguration(
                                   conf : ConfigurationObject.StatementConfigurationJson =
                                   new ConfigurationObject.StatementConfigurationJson(
                                     Seq[ConfigurationObject.Source](),ConfigurationObject.GeneralSetting())
                                 ) {
  override def toString: String = "StatementConfiguration => conf:"+conf.toString



  def source(idName : String) : ConfigurationObject.Source = {
    conf.sources.find(source => source.id == idName ) match {
      case Some(v : ConfigurationObject.Source) => v
      case None => throw StatementConfigurationException("Unknown source id:"+idName )
    }
  }

  def sources() : Seq[ConfigurationObject.Source] = conf.sources

}
