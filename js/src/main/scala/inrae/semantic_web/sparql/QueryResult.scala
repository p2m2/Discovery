package inrae.semantic_web.sparql

import inrae.semantic_web.rdf.{Anonymous, Literal, RdfType, URI}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel(name="QueryResult")
case class QueryResult(results : String, mimetype : String) {
  def print(): Unit = {
    None: Option[String]
  }

  def json() : ResultsFormat = {
    println("=========== JSON ===================");
    println(results);
    val data = ujson.read(results)

    val rf = ResultsFormat()
    val vars = data("head")("vars")

    data("results")("bindings").arr
       .map( KeyAndValue =>
         KeyAndValue.obj.map( x  => {
           val rdfobj : RdfType = x._2("type").str match {
             case "uri" => URI(x._2("value").str, "")
             case "literal" => Literal(x._2("value").str, x._2("datatype").str, x._2("lang").str)
             case "bnode" => Anonymous(x._2("value").str)
             case _ => /* probleme with json format */ Literal(x._2.toString(), "unknown")
           }
            (x._1 -> rdfobj)
         })
       ).foreach( a => rf.rows = rf.rows :+ rf.ResultsRow(a.toMap))

    return rf
    //rf.rows = rf.rows :+ rf.ResultsRow(values)
  }

  @JSExport
  def get(): Option[ResultsFormat] = {
      mimetype match {
        case "json" => Some(json())
        case _ => None
      }
  }

  def getString() : String = {
    "---"
  }
}
