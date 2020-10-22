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

trait Node {

  var children: Seq[Node] = Seq[Node]()

  def addChildren(n: Node): Node = {
    children = children :+ n
    this
  }
  override def toString() : String = {

    "NODE "+ { children.length match {
      case l if l>0 => " ["+children.toString()+"]"
      case _ => ""
    } }
  }
}

object Node {
  def references(n : Node ) : List[String] = n match {
    case rdf : RdfNode => {
      List(rdf.reference()) ++ n.children.flatMap(c => c match {
        case rdf: RdfNode => references(rdf)
        case _ => List("")
      })
    }
    case _ => List("")
  }

}

/* Filter node */


/* Node case */
case class Root() extends Node {
  var lSourcesNodes : Seq[SourcesNode] = List[SourcesNode]()
  var lOperatorsNode : Seq[OperatorNode] = List[OperatorNode]()

  def sourcesNode(n : RdfNode) : Option[SourcesNode] = {
    lSourcesNodes.find( p => p.n == n )
  }
}

/* triplets */
class RdfNode(uniqRef : String) extends Node {

  def reference(): String = uniqRef

  override def toString() : String = {

    this.getClass.getSimpleName+ "@"+uniqRef.toString+ { children.length match {
      case l if l>0 => " ["+children.toString()+"]"
      case _ => ""
    } }
  }

  /*
 duplicateWithoutChildren
*/
  def duplicateWithoutChildren() : RdfNode = ???
}

case class Something(concretUniqRef: String) extends RdfNode(concretUniqRef) {
  override def duplicateWithoutChildren() = Something(concretUniqRef)
}

case class SubjectOf(concretUniqRef : String,uri : URI) extends RdfNode(concretUniqRef) {
  override def duplicateWithoutChildren() = SubjectOf(concretUniqRef,uri)
}

case class ObjectOf(concretUniqRef : String,uri : URI) extends RdfNode(concretUniqRef) {
  override def duplicateWithoutChildren() = ObjectOf(concretUniqRef,uri)
}

case class LinkTo(concretUniqRef : String,term : RdfType) extends RdfNode(concretUniqRef) {
  override def duplicateWithoutChildren() = LinkTo(concretUniqRef,term)
}

case class LinkFrom(concretUniqRef : String,uri : URI) extends RdfNode(concretUniqRef) {
  override def duplicateWithoutChildren() = LinkFrom(concretUniqRef,uri)
}
case class Attribute(concretUniqRef : String,uri : URI) extends RdfNode(concretUniqRef) {
  override def duplicateWithoutChildren() = Attribute(concretUniqRef,uri)
}

case class Value(var rdfterm : RdfType) extends RdfNode(rdfterm.toString) {
  override def duplicateWithoutChildren() = Value(rdfterm)
}

/* Logic */
sealed trait LogicNode
case class UnionBlock(sire : Node) extends Node with LogicNode

case class Not(sire : Node) extends Node with LogicNode

/* filter */
sealed trait FilterNode
case class isLiteral() extends Node with FilterNode
case class isURI() extends Node with FilterNode

/* SourcesNode */
case class SourcesNode(n : RdfNode, sources : Seq[String]) extends Node

/* Operator */
case class OperatorNode(var operator : String) extends Node