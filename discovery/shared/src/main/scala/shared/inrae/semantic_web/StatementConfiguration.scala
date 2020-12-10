package inrae.semantic_web

import inrae.semantic_web.sparql.HttpRequestDriver
import inrae.semantic_web.driver._
import upickle.default.{macroRW, ReadWriter => RW}
import wvlet.log.LogLevel

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
                     `type`: String="tps",  /* ldfragment, csv, tps */
                     method: String = "POST", /* POST, POST_ENCODED, GET */
                     auth : String = "none", /* basic, digest, bearer, proxy, none */
                     login : String = "none" ,
                     password : String = "none",
                     token : String = "",
                     mimetype: String = "json"
                   ) {
  }

  case class GeneralSetting(
                      driver: String = "inrae.semantic_web.driver.RosHTTPDriver",
                      logLevel : String = "warn"          , // debug, info, warn, error
                      sizeBatchProcessing : Int = 150
                    )

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
class StatementConfiguration {

  var conf: ConfigurationObject.StatementConfigurationJson =
    new ConfigurationObject.StatementConfigurationJson(
      Seq[ConfigurationObject.Source](),ConfigurationObject.GeneralSetting())

  /**
   * Set a config using class definition
   * @param conf
   */
  @JSExport
  def setConfig(conf_ext : ConfigurationObject.StatementConfigurationJson) : Unit = {
      conf = conf_ext
  }

  /**
   * set a config using string configuration
   * @param json_conf
   */
  @JSExport
  def setConfigString(json_conf: String) : Unit = {
    try {
      conf = upickle.default.read[ConfigurationObject.StatementConfigurationJson](json_conf)
    } catch {
      case e1: upickle.core.AbortException => System.err.println(e1)
    }
  }

  def source(idname : String) : ConfigurationObject.Source = {
    //(json \ "sources").as[Seq[Source]].find( source => source.id == idname )

    conf.sources.find(source => source.id == idname ) match {
      case Some(v : ConfigurationObject.Source) => v
      //case Some(lv : Seq[Source]) => lv[0]
      case None => throw new Exception("Unknown source id:"+idname )
    }
  }

  def sources() : Seq[ConfigurationObject.Source] = {
    conf.sources
  }

  def getHttpDriver() : HttpRequestDriver = {
    import org.portablescala.reflect._
        /*try {

        //ScalaJs not compatible

          import scala.reflect.runtime.{universe=>ru,_}
          val mirror = ru.runtimeMirror(getClass.getClassLoader)
          val classSymbol = mirror.classSymbol(Class.forName(conf.settings.driver))
          val clsMirror = mirror.reflectClass(classSymbol.asClass)
          val ctor = classSymbol.toType.decl(ru.termNames.CONSTRUCTOR).asMethod

          clsMirror.reflectConstructor(ctor)().asInstanceOf[HttpRequestDriver]
     */

      Reflect.lookupInstantiatableClass(conf.settings.driver) match {
        case Some( cls ) => cls.newInstance().asInstanceOf[HttpRequestDriver]
        case None => throw StatementConfigurationException("Unknown Http Request Driver :"+conf.settings.driver)
      }

/*
    } catch {
      case _ : ClassNotFoundException => {
        throw StatementConfigurationException("Unknown Http Request Driver :"+conf.settings.driver)
      }
      case e : Throwable => {
        println(e)
        throw StatementConfigurationException("Devel error with :"+conf.settings.driver)
      }
    } */
  }

  def logLevel() : LogLevel = conf.settings.logLevel.toLowerCase() match {
      case "debug" | "d" => LogLevel.INFO
      case "info" | "i" => LogLevel.INFO
      case "warn" | "w" => LogLevel.WARN
      case "error" | "e" => LogLevel.ERROR
      case "trace" | "t" => LogLevel.TRACE
      case "all" => LogLevel.ALL
      case "off" => LogLevel.OFF
      case _ => LogLevel.WARN
    }
}
