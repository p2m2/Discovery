package inrae.semantic_web.internal

import inrae.semantic_web.rdf._

import scala.concurrent.Future

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

case class Node(var uniqRef : Option[String]) {

  var children: Seq[Node] = Seq[Node]()

  def addChildren(n: Node): Node = {
    children = children :+ n
    return n
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

/* Filter node */
sealed trait FilterNode{}

/* Node case */
class Root() extends Node(None) {
  var lSourcesNodes : Seq[SourcesNode] = List[SourcesNode]()
  var lOperatorsNode : Seq[OperatorNode] = List[OperatorNode]()
}

/* triplets */
class Something(uniqRef : String) extends Node(Some(uniqRef))
class SubjectOf(uniqRef : String, var uri : URI) extends Node(Some(uniqRef))
class ObjectOf(uniqRef : String, var uri : URI) extends Node(Some(uniqRef))
class LinkTo(uniqRef : String, var term : RdfType) extends Node(Some(uniqRef))
class LinkFrom(uniqRef : String, var uri : URI) extends Node(Some(uniqRef))
class Attribute(uniqRef : String, var uri : URI) extends Node(Some(uniqRef))
class Value(var rdfterm : RdfType) extends Node(None)

/* filter */
class isLiteral() extends FilterNode
class isURI() extends FilterNode


/* SourcesNode */
class SourcesNode(var n : Node, var sources : Seq[String]) extends Node(n.reference())

/* Operator */
class OperatorNode(var operator : String ) extends Node(None)