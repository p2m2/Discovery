package inrae.semantic_web.sparql

import inrae.semantic_web.rdf.{Anonymous, Literal, RdfType, URI}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js
import js.JSConverters._

@JSExportTopLevel(name="QueryResult")
case class QueryResult(results : String, mimetype : String="json") {
  def print(): Unit = {
    None: Option[String]
  }

  def valToOption(v:ujson.Value, key: String) : Option[String] = {
    try {
      Some(v(key).str)
    } catch {
      case e: Exception => None
    }
  }

  def json() : ResultsFormat = {

    val data = ujson.read(results)
    val rf = ResultsFormat()
    //val vars = data("head")("vars")
    data("results")("bindings").arr
       .map( KeyAndValue =>
         KeyAndValue.obj.map( x  => {
           val rdfobj : RdfType = x._2("type").str match {
             case "uri" => URI(x._2("value").str, "")
             case "literal" | "typed-literal" => Literal(x._2("value").str, x._2("datatype").str, valToOption(x._2,"lang"))
             case "bnode" => Anonymous(x._2("value").str)
             case _ => /* probleme with json format */ Literal(x._2.toString(), "unknown")
           }
            (x._1 -> rdfobj)
         })
       ).foreach( a => rf.rows = rf.rows :+ rf.ResultsRow(a.toMap))

    return rf
  }

  @JSExport
  def get(): ResultsFormat = {
    mimetype match {
        case "json" => json()
        case _ => new ResultsFormat()
    }
  }
}
