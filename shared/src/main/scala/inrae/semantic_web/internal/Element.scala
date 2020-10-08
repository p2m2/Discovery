package inrae.semantic_web.internal

import inrae.semantic_web.rdf._
/*
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
}*/

class Node(var uniqRef : Option[String]) {

  var children: Seq[Node] = Seq[Node]()
  var sources: Seq[String] = Seq[String]()

  def addChildren(n: Node): Node = {
    children = children :+ n
    return n
  }

  def addSource(s: String): Node = {
    sources = sources :+ s
    this
  }

  def references(): Seq[String] = {

    val l: Seq[String] = uniqRef match {
      case Some(v) => Seq[String](v)
      case None => Seq[String]()
    }

    l ++: children.flatMap(c => c.references())
  }

  def reference(): Option[String] = uniqRef
}

/* Node case */
class Root() extends Node(None)


class Something(uniqRef : String) extends Node(Some(uniqRef))
class SubjectOf(uniqRef : String, var uri : URI) extends Node(Some(uniqRef))
class ObjectOf(uniqRef : String, var uri : URI) extends Node(Some(uniqRef))
class Attribute(uniqRef : String, var uri : URI) extends Node(Some(uniqRef))
class Value(var uri : RdfType) extends Node(None)
