package inrae.semantic_web.rdf

import scala.scalajs.js.annotation.JSExportTopLevel

case class Graph(triples : Set[Triple])

case class Triple(s: RdfType, p: RdfType, o: RdfType)

trait RdfType {
  def sparql() : String
}

@JSExportTopLevel(name="IRI")
case class IRI (var iri : String) extends RdfType {
  override def toString() : String = {
      "<"+iri+">"
  }
  def sparql() : String = toString
}

@JSExportTopLevel(name="URI")
case class URI (var localName : String,var nameSpace : String = "") extends RdfType {
  override def toString() : String = {
    (localName,nameSpace) match {
      case ("a",_) => "a"
      case (_,"") => "<"+localName+">"
      case _ => nameSpace + ":" + localName
    }
  }
  def sparql() : String = toString
}

@JSExportTopLevel(name="Anonymous")
case class Anonymous(var value : String) extends RdfType {
  override def toString() : String = {
    return value
  }
  def sparql() : String = toString
}

@JSExportTopLevel(name="PropertyPath")
case class PropertyPath(var value : String) extends RdfType {
  override def toString() : String = value

  def sparql() : String = toString
}

@JSExportTopLevel(name="Literal")
case class Literal(var value : String, var datatype : String = "xsd:string", var tag : Option[String]=None) extends RdfType {
  override def toString() : String = value+"^^"+datatype

  def sparql() : String = toString
}