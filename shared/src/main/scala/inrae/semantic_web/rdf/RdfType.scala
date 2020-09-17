package inrae.semantic_web.rdf

case class Graph(triples : Set[Triple])

case class Triple(s: RdfType, p: RdfType, o: RdfType)

trait RdfType

case class URI (var value : String) extends RdfType {
  override def toString() : String = {
    return "<"+value+">"
  }
}
case class PropertyPath(var value : String) extends RdfType {
  override def toString() : String = {
    return value
  }
}
case class Literal(var value : String, var datatype : String = "xsd:string", var tag : String = null) extends RdfType {
  override def toString() : String = {
    return value+"^^"+datatype
  }
}