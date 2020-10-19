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


/* Node case */
class Root() extends Node(None) {
  var lSourcesNodes : Seq[SourcesNode] = List[SourcesNode]()
  var lOperatorsNode : Seq[OperatorNode] = List[OperatorNode]()
}

/* triplets */
sealed trait RdfNode
class Something(uniqRef : String) extends Node(Some(uniqRef)) with RdfNode
class SubjectOf(uniqRef : String, var uri : URI) extends Node(Some(uniqRef)) with RdfNode
class ObjectOf(uniqRef : String, var uri : URI) extends Node(Some(uniqRef)) with RdfNode
class LinkTo(uniqRef : String, var term : RdfType) extends Node(Some(uniqRef)) with RdfNode
class LinkFrom(uniqRef : String, var uri : URI) extends Node(Some(uniqRef)) with RdfNode
class Attribute(uniqRef : String, var uri : URI) extends Node(Some(uniqRef)) with RdfNode
class Value(var rdfterm : RdfType) extends Node(None) with RdfNode

/* Logic */
sealed trait LogicNode
class UnionBlock( var sire : Node ) extends Node(None) with LogicNode
class Not( var sire : Node ) extends Node(None) with LogicNode


/* filter */
sealed trait FilterNode
class isLiteral() extends Node(None) with FilterNode
class isURI() extends Node(None) with FilterNode

/* SourcesNode */
class SourcesNode(var n : Node, var sources : Seq[String]) extends Node(n.reference())

/* Operator */
class OperatorNode(var operator : String ) extends Node(None)