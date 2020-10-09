package inrae.semantic_web.rdf

import scala.scalajs.js.annotation.JSExportTopLevel

case class Graph(triples : Set[Triple])

case class Triple(s: RdfType, p: RdfType, o: RdfType)

trait RdfType

@JSExportTopLevel(name="URI")
case class URI (var localName : String,var nameSpace : String = "") extends RdfType {
  override def toString() : String = {
    (localName,nameSpace) match {
      case ("a",_) => "a"
      case (_,"") => "<"+localName+">"
      case _ => "<"+nameSpace + "/" + localName+">"
    }
  }

}

@JSExportTopLevel(name="Anonymous")
case class Anonymous(var id: String) extends RdfType {
  override def toString() : String = {
    return "anonymous:"+id
  }
}

@JSExportTopLevel(name="PropertyPath")
case class PropertyPath(var value : String) extends RdfType {
  override def toString() : String = {
    return value
  }
}

@JSExportTopLevel(name="Literal")
case class Literal(var value : String, var datatype : String = "xsd:string", var tag : Option[String]=None) extends RdfType {
  override def toString() : String = {
    return value+"^^"+datatype
  }
}