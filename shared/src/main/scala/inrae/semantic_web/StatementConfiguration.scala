package inrae.semantic_web

import upickle.default.{macroRW, ReadWriter => RW}

/**
 * using doc to validate JSON config:
 * see https://www.playframework.com/documentation/2.8.x/ScalaJson
 *
 * @param json_conf
 */

class StatementConfiguration() {

  case class Source(
                     id:String, /* identify the source endpoint */
                     url: String, /* url access */
                     typ: String,  /* ldfragment, csv, tps */
                     method: String = "POST", /* POST, GET*/
                     auth : String = "none", /* basic, digest, none */
                     login : String = "none" ,
                     password : String = "none"
                   )

  case class StatementConfigurationJson(
                                         sources : Seq[Source]  /* sources configuration */
                                       )

  object Source{
    implicit val rw: RW[Source] = macroRW
  }

  object StatementConfigurationJson{
    implicit val rw: RW[StatementConfigurationJson] = macroRW
  }
  /*
  implicit val StatementConfigurationJsonReadWrite: ReadWriter[StatementConfigurationJson] =
    readwriter[Seq[Source]].bimap[StatementConfigurationJson](_.sources, StatementConfigurationJson(_))

  implicit val SourceReadWrite: ReadWriter[Source] =
    ReadWriter.merge(
      macroRW[Source]
    )
*/
  var conf: StatementConfigurationJson = new StatementConfigurationJson(Seq[Source]())

  /**
   * Set a config using class definition
   * @param conf
   */
  def setConfig(conf_ext : StatementConfigurationJson) : Unit = {
      conf = conf_ext
  }

  /**
   * set a config using string configuration
   * @param json_conf
   */
  def setConfigString(json_conf: String) : Unit = {
    try {
      conf = upickle.default.read[StatementConfigurationJson](json_conf)
    } catch {
      case e1: upickle.core.AbortException => System.err.println(e1)
    }
  }

  def source(idname : String) : Source = {
    //(json \ "sources").as[Seq[Source]].find( source => source.id == idname )

    conf.sources.find(source => source.id == idname ) match {
      case Some(v : Source) => v
      //case Some(lv : Seq[Source]) => lv[0]
      case None => throw new Exception("Unknown source id:"+idname )
    }
  }
}
