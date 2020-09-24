package inrae.semantic_web

import play.api.libs.json.Json
import play.api.libs.json._

/**
 * using doc to validate JSON config:
 * see https://www.playframework.com/documentation/2.8.x/ScalaJson
 *
 * @param json_conf
 */


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
                                       sources : Seq[Source] /* sources configuration */
                                     )


class StatementConfiguration() {

  implicit val SourceWrites = Json.writes[Source]
  implicit val SourceReads = Json.reads[Source]
  implicit val SourceFormat = Json.format[Source]
  implicit val StatementConfigurationJsonWrites = Json.writes[StatementConfigurationJson]
  implicit val StatementConfigurationJsonReads = Json.reads[StatementConfigurationJson]

  var json: JsValue = JsNull

  /**
   * Set a config using class definition
   * @param conf
   */
  def setConfig(conf : StatementConfigurationJson) : Unit = {
    json = Json.toJson(conf)
  }

  /**
   * set a config using string configuration
   * @param json_conf
   */
  def setConfigString(json_conf: String) : Unit = {
    json = try { Json.parse(json_conf) } finally { Json.toJson("bad configuration.") }
  }

  def source(idname : String) : Source = {
    //(json \ "sources").as[Seq[Source]].find( source => source.id == idname )

    (json \ "sources").as[Seq[Source]].find(source => source.id == idname ) match {
      case Some(v : Source) => v
      //case Some(lv : Seq[Source]) => lv[0]
      case None => throw new Exception("Unknown source id:"+idname )
    }
  }

  def getDeprecatedSource(idx : Integer = 0, key : String) : String = {
    val name = (json \ "sources").asOpt[Array[String]] match {
      case None => return ""//Or handle the lack of a value another way: throw an error, etc.
      case Some(s: Array[String]) => return s(idx) //return the string to set your value
    }

    println("results")
    println(name)
    //println(Json.prettyPrint(conf \\ key))
    // val r = (conf \\ key) //.map(_.as [String])
    // val readableString: String = Json.prettyPrint(r)
    // println(readableString)
    ""
  }
}
