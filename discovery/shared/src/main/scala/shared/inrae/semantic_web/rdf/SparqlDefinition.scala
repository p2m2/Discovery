package inrae.semantic_web.rdf

import scala.scalajs.js.annotation.JSExportTopLevel

case class Graph(triples : Set[Triple])

case class Triple(s: SparqlDefinition, p: SparqlDefinition, o: SparqlDefinition)

trait SparqlDefinition {
  def cleanString(str : String) = {
    str.replaceAll("^\"","")
      .replaceAll("\"$","")
      .replaceAll("^<","")
      .replaceAll(">$","")
  }

  def sparql() : String

  def naiveLabel() : String
}

@JSExportTopLevel(name="IRI")
case class IRI (var iri : String) extends SparqlDefinition {
  iri = cleanString(iri)
  override def toString() : String = {
      "<"+iri+">"
  }
  def sparql() : String = toString

  def naiveLabel() : String = iri.split("[/#]").last

}

@JSExportTopLevel(name="URI")
case class URI (var localName : String,var nameSpace : String = "") extends SparqlDefinition {
  localName = cleanString(localName)
  nameSpace = cleanString(nameSpace)

  override def toString() : String = {
    (localName,nameSpace) match {
      case ("a",_) => "a"
      case (_,"") => "<"+localName+">"
      case _ => nameSpace + ":" + localName
    }
  }

  def sparql() : String = toString

  def naiveLabel() : String = localName.split("[/#]").last
}

@JSExportTopLevel(name="Anonymous")
case class Anonymous(var value : String) extends SparqlDefinition {
  value = cleanString(value)

  override def toString() : String = {
    return value
  }
  def sparql() : String = toString

  def naiveLabel() : String = s"Anonymous[$value]"
}

@JSExportTopLevel(name="PropertyPath")
case class PropertyPath(var value : String) extends SparqlDefinition {
  value = cleanString(value)

  override def toString() : String = value

  def sparql() : String = toString

  def naiveLabel() : String = s"PropertyPath[$value]"
}

@JSExportTopLevel(name="Literal")
case class Literal(var value : String, var datatype : String = "xsd:string", var tag : Option[String]=None) extends SparqlDefinition {
  value = cleanString(value)
  datatype = cleanString(datatype)

  override def toString() : String = value+"^^"+datatype

  def toInt() : Int = value.toInt
  def toBoolean() : Boolean = value.toBoolean

  def sparql() : String = toString

  def naiveLabel() : String = s"PropertyPath[$value]"
}

@JSExportTopLevel(name="QueryVariable")
case class QueryVariable (var name : String) extends SparqlDefinition {
  name = cleanString(name)
  override def toString() : String = {
    "?"+name
  }
  def sparql() : String = toString

  def naiveLabel() : String = s"Variable[$name]"
}

object SparqlBuilder {

  def create(value : ujson.Value) : SparqlDefinition = {
    value("type").value match {
      case "uri" => createUri(value)
      case "literal" => createLiteral(value)
      case _ => throw new Error("unknown type !")
    }
  }

  def createUri(value : ujson.Value) : URI = {
    try {
      URI(value("value").value.toString)
    } catch {
      case _ : Throwable => URI("")
    }
  }

  def createLiteral(value : ujson.Value) : Literal = {
    try {
      Literal(value("value").toString,value("datatype").toString)
    } catch {
      case _ : Throwable => Literal("","")
    }
  }
}