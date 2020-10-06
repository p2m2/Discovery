package inrae.semantic_web.internal

import inrae.semantic_web.rdf._

sealed trait Node {
  var children : Seq[ReferenceNode] = Seq[ReferenceNode]()
  var sources : Seq[String] = Seq[String]()

  def addChildren(n : Node) : Node = {
    children = children :+ n
    return n
  }

  def addSource(s : String) : Node = {
    sources = sources :+ s
    this
  }
}

class ReferenceNode(var uniqRef : Option[String]) extends None {
  def references(): Seq[String] = {
    val l = uniqRef match {
      case Some(v) => v.toList
      case None => Seq[String]()
    }
    l ::: children.flatMap( c => c.references())
  }
  def reference(): String = {
    uniqRef
  }
}

/* Node case */
class Root() extends ReferenceNode(None)


class Something(uniqRef : String) extends ReferenceNode(Some(uniqRef))
class SubjectOf(uniqRef : String, var uri : URI) extends ReferenceNode(Some(uniqRef))
class ObjectOf(uniqRef : String, var uri : URI) extends ReferenceNode(Some(uniqRef))
class Attribute(uniqRef : String, var uri : URI) extends ReferenceNode(Some(uniqRef))
class Value(var uri : RdfType) extends ReferenceNode(None)
