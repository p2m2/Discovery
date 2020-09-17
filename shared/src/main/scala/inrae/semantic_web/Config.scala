package inrae.semantic_web

import play.api.libs.json._

/**
 * using doc to validate JSON config:
 * see https://www.playframework.com/documentation/2.8.x/ScalaJson
 *
 * @param json_conf
 */
class Config(var json_conf: String) {
  var conf: JsValue = Json.parse(json_conf)
  def get(key : String) : String = {
    val name = (conf \ "endpoints").asOpt[String] match {
      case None => return ""//Or handle the lack of a value another way: throw an error, etc.
      case Some(s: String) => return s //return the string to set your value
    }

    println("results")
    println(name)
    //println(Json.prettyPrint(conf \\ key))
    // val r = (conf \\ key) //.map(_.as [String])
    // val readableString: String = Json.prettyPrint(r)
    // println(readableString)
    ""
  }
  def getIndex(key : String, idx : Integer = 0) : String = {
    val name = (conf \ "endpoints").asOpt[Array[String]] match {
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
