package inrae.semantic_web.rdf

import scala.scalajs.js.annotation.JSExportTopLevel

case class Graph(triples : Set[Triple])

case class Triple(s: SparqlDefinition, p: SparqlDefinition, o: SparqlDefinition)

import scala.language.implicitConversions

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

object IRI {
  implicit def fromString(s: String): IRI = IRI(s)
}

@JSExportTopLevel(name="URI")
case class URI (localNameUser : String,nameSpaceUser : String = "") extends SparqlDefinition {
  val localName = nameSpaceUser match {
    case "" if (!localNameUser.contains("://")) => {
      cleanString(localNameUser.split(":").last)
    }
    case _ => cleanString(localNameUser)
  }

  val nameSpace = nameSpaceUser match {
    case "" if (!localNameUser.contains("://")) => {
      localNameUser.split(":") match {
        case arr if (arr.length==2) => arr(0)
        case _ => "" /* something wrong if arity if different that 2 */
      }
    }
    case _ => nameSpaceUser
  }

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

object URI {
  implicit def fromString(s: String): URI = URI(s)
  val empty = new URI("")
}


@JSExportTopLevel(name="Anonymous")
case class Anonymous(var value : String) extends SparqlDefinition {
  value = cleanString(value)

  override def toString() : String = value

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

object PropertyPath {
  implicit def fromString(s: String): PropertyPath = PropertyPath(s)
}

@JSExportTopLevel(name="Literal")
case class Literal(var value : String,var datatype : URI = URI.empty,var tag : String="") extends SparqlDefinition {
  value = cleanString(value)
  tag = cleanString(tag)

  override def toString() : String = "\""+ value + "\""+ (datatype match {
    case URI.empty => ""
    case _ if (tag == "") => "^^"+datatype.toString()
    case _ => ""

  }) + ( tag match {
    case "" => ""
    case _ => "@"+tag
  })

  def toInt() : Int = value.toInt

  def toBoolean() : Boolean = value.toBoolean

  def sparql() : String = toString

  def naiveLabel() : String = value
}

object Literal {
  implicit def fromString(s: String): Literal = Literal(s)
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
      Literal(value("value").toString,URI(value("datatype").toString))
    } catch {
      case _ : Throwable => Literal("")
    }
  }
}