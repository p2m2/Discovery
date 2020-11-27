package inrae.semantic_web

import upickle.default.{macroRW, ReadWriter => RW}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

/**
 * using doc to validate JSON config:
 * see https://www.playframework.com/documentation/2.8.x/ScalaJson
 *
 * @param json_conf
 */


object ConfigurationObject {
  /* sources configuration */
  case class StatementConfigurationJson(
                                         sources : Seq[Source]
                                       )

  case class Source(
                     id:String, /* identify the source endpoint */
                     url: String, /* url access */
                     typ: String,  /* ldfragment, csv, tps */
                     method: String = "POST", /* POST, POST_ENCODED, GET */
                     auth : String = "none", /* basic, digest, none */
                     login : String = "none" ,
                     password : String = "none",
                     mimetype : String = "json",
                   )

  case class Prefixes(list : Map[String,String])

  object Prefixes{
    implicit val rw: RW[Prefixes] = macroRW
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
      Seq[ConfigurationObject.Source]())

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
}
