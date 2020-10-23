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
  /* prefix management */
  var prefixes : Map[String,IRI] = Map[String,IRI]()
  var defaultGraph : Seq[IRI]    = List[IRI]()
  var namedGraph : Seq[IRI]      = List[IRI]()
  var lSourcesNodes : Seq[SourcesNode] = List[SourcesNode]()
  var lOperatorsNode : Seq[OperatorNode] = List[OperatorNode]()

  def sourcesNode(n : RdfNode) : Option[SourcesNode] = {
    lSourcesNodes.find( p => p.n == n )
  }

  override def toString() : String = {
    "=======================================================\n"+"ROOT "+ { children.length match {
      case l if l>0 => " ["+children.toString()+"]"
      case _ => ""
    } } + "\n=======================================================\n"
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


class URIRdfNode(concretUniqRef : String,val uri : URI) extends RdfNode(concretUniqRef)

case class Something(concretUniqRef: String) extends RdfNode(concretUniqRef) {
  override def duplicateWithoutChildren() = Something(concretUniqRef)
}

case class SubjectOf(concretUniqRef : String,uri2 : URI) extends URIRdfNode(concretUniqRef,uri2) {
  override def duplicateWithoutChildren() = SubjectOf(concretUniqRef,uri)
}

case class ObjectOf(concretUniqRef : String,uri2 : URI) extends URIRdfNode(concretUniqRef,uri2) {
  override def duplicateWithoutChildren() = ObjectOf(concretUniqRef,uri)
}

case class LinkTo(concretUniqRef : String,term : RdfType) extends RdfNode(concretUniqRef) {
  override def duplicateWithoutChildren() = LinkTo(concretUniqRef,term)
}

case class LinkFrom(concretUniqRef : String,uri2 : URI) extends URIRdfNode(concretUniqRef,uri2) {
  override def duplicateWithoutChildren() = LinkFrom(concretUniqRef,uri)
}

case class Value(var term : RdfType) extends Node

/* Logic */
class LogicNode(val sire : Node) extends Node
case class UnionBlock(s : Node) extends LogicNode(s)
case class Not(s : Node) extends LogicNode(s)

/* filter */
sealed trait FilterNode
case class isLiteral() extends FilterNode
case class isURI() extends FilterNode
case class contains(value :String)  extends FilterNode

/* SourcesNode */
case class SourcesNode(n : RdfNode, sources : Seq[String]) extends Node

/* BIND / Operator */
case class OperatorNode(var operator : String) extends Node