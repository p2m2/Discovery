package inrae.semantic_web.sparql

import inrae.semantic_web.rdf.{Anonymous, Literal, SparqlBuilder, SparqlDefinition, URI}

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
    //scribe.debug(v2k.toString)
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

    val records = json("results")("bindings").arr.map(kv => kv match {
      case o: ujson.Obj => o.obj.map(
        kv2 => {
            v2k.find( _._2 == kv2._1).map( _._1 ) match {
              case Some(s) => (s,kv2._2)
              case _ => (kv2._1,kv2._2)
            }
        })
      case _ => Nil
    })
    json("results")("bindings").arr.clear()
    records.map( r => json("results")("bindings").arr.append(r) )
  }

  /* get column results */
  def getValues( key : String ): Seq[SparqlDefinition] = {
    json("results")("bindings").arr.map(kv => kv match {
      case o: ujson.Obj => {
        SparqlBuilder.create(o(key))
      }
    }).toSeq
  }

  def setDatatype( key : String , uri_values : Map[String,ujson.Value] ): Unit = {
    val datatype = json("results").obj.getOrElse("datatypes",ujson.Obj())
    val keyObjet = datatype.obj.getOrElse(key,ujson.Obj())

    uri_values.foreach( {
      case (subkey, value) => {
        val subkeyObjet = keyObjet.obj.getOrElse(subkey,ujson.Arr())
        subkeyObjet.arr.append(value)
        keyObjet.obj.update(subkey,subkeyObjet)
      }
    })

    datatype.obj.update(key,keyObjet)
    json("results").update("datatypes",datatype)
  }
}
