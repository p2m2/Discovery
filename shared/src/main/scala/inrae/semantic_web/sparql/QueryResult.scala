package inrae.semantic_web.sparql

import inrae.semantic_web.rdf.{Anonymous, Literal, SparqlDefinition, URI}
import scala.util.parsing.json

case class QueryResult(results: String, mimetype : String = "json") {

  var json = try {
    ujson.read(results)
  } catch {
    case _ : Throwable => ujson.Obj(
      "head" -> ujson.Obj(
        "link" -> ujson.Arr(),
        "vars" -> ujson.Arr()
      ),
      "results" -> ujson.Obj(
        "distinct" -> "false",
        "ordered" -> "true",
        "bindings" -> ujson.Arr()
      )
    )
  }

  def v2Ident(v2k : Map[String,String]) = {

    val l = json("head")("vars").arr.map(v => {
      val v2 = v.toString().replace("\"","")
      v2k.find( v2 == _._2 ).map( x => x._1 ) match {
        case Some(s) => s
        case s => s
      }})

    json("head")("vars").arr.clear()
    l.map( {
      case a : String => json("head")("vars").arr.append(a)
      case a => Nil
    })

    json("results")("bindings").arr.foreach(kv => kv match {
      case o: ujson.Obj => o.obj.map(
        kv2 => {
          o.obj.update(v2k.find( _._2 == kv2._1).map( _._1 ) match {
            case Some(s) => s
            case _ => kv2._1
          },kv2._2)
          o.obj.remove(kv2._1)
        }
      )
      case _ => Nil
    })
  }
}
