package inrae.semantic_web.rdf

case class Graph(triples : Set[Triple])

case class Triple(s: RdfType, p: RdfType, o: RdfType)

trait RdfType

case class URI (var localName : String,var nameSpace : String = "") extends RdfType {
  override def toString() : String = {
    nameSpace match {
      case "" => "<"+localName+">"
      case _ => "<"+nameSpace + "/" + localName+">"
    }
  }

}

case class Anonymous(var id: String) extends RdfType {
  override def toString() : String = {
    return "anonymous:"+id
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