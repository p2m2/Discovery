package inrae.semantic_web.internal

import inrae.semantic_web.rdf._
import wvlet.log.Logger.rootLogger.debug

trait Node {

  var children: Seq[Node] = Seq[Node]()

  def addChildren(n: Node): Node = {
    children = children :+ n
    this
  }

  def getRdfNode(ref : String,sep : String ="") : Option[RdfNode] = {
    debug(" -- getRdfNode -- ")
    this match {
      case  n : RdfNode if (ref == n.reference()) => Some(n)
      case _ if children.nonEmpty => children.flatMap(c => c.getRdfNode(ref, sep + "*")).headOption
      case _ =>  None
    }
  }

  override def toString() : String = {
    "NODE "+ { children.length match {
      case l if l>0 => " ["+children.toString()+"]"
      case _ => ""
    } }
  }

  /* everything by default*/
  def accept(n: Node): Boolean = {
    true
  }
}

object Node {
  def references(n : Node ) : List[String] = n match {
    case rdf : RdfNode => {
      List(rdf.reference()) ++ n.children.flatMap(c => c match {
        case rdf: RdfNode => references(rdf)
        case _ => List()
      })
    }
    case _ => List()
  }

}

/* Filter node */


/* Node case */
case class Root() extends Node {
  /* prefix management */
  var prefixes : Map[String,IRI] = Map[String,IRI]()
  var defaultGraph : Seq[IRI]    = List[IRI]()
  var namedGraph : Seq[IRI]      = List[IRI]()

  var lDatatypeNode : Seq[DatatypeNode] = List[DatatypeNode]()
  var lSourcesNodes : Seq[SourcesNode] = List[SourcesNode]()
  var lOperatorNode : Seq[OperatorNode] = List[OperatorNode]()

  def sourcesNode(n : RdfNode) : Option[SourcesNode] = {
    lSourcesNodes.find( p => p.refNode == n.reference() )
  }

  override def toString() : String = {
    "=======================================================\n"+"ROOT "+ { children.length match {
      case l if l>0 => " ["+children.toString()+"]"
      case _ => ""
    } } + "\n=======================================================\n"
  }
}

/* triplets */
abstract class RdfNode(uniqRef : String) extends Node {

  def reference(): String = uniqRef

  override def toString() : String = {

    this.getClass.getSimpleName+ "@"+uniqRef.toString+ { children.length match {
      case l if l>0 => " ["+children.toString()+"]"
      case _ => ""
    } }
  }

  def duplicateWithoutChildren() : RdfNode
}


abstract class URIRdfNode(concretUniqRef : String,val term : SparqlDefinition) extends RdfNode(concretUniqRef)

case class Something(concretUniqRef: String) extends RdfNode(concretUniqRef) {
  override def duplicateWithoutChildren() = Something(concretUniqRef)
  /* everything by default*/
  override def accept(n: Node): Boolean = n match {
    case _ : Something  => false
    case _ : URIRdfNode => true
    case _ : FilterNode => true
    case _ : Value      => true
    case _ : ListValues => true
    case _              => false
  }
}

case class SubjectOf(concretUniqRef : String, override val term : SparqlDefinition) extends URIRdfNode(concretUniqRef,term) {
  override def duplicateWithoutChildren() = SubjectOf(concretUniqRef,term)
}

case class ObjectOf(concretUniqRef : String,override val term : SparqlDefinition) extends URIRdfNode(concretUniqRef,term) {
  override def duplicateWithoutChildren() = ObjectOf(concretUniqRef,term)
}

case class LinkTo(concretUniqRef : String,override val term : SparqlDefinition) extends URIRdfNode(concretUniqRef,term) {
  override def duplicateWithoutChildren() = LinkTo(concretUniqRef,term)
}

case class LinkFrom(concretUniqRef : String,override val term : SparqlDefinition) extends URIRdfNode(concretUniqRef,term) {
  override def duplicateWithoutChildren() = LinkFrom(concretUniqRef,term)
}

case class Value(var term : SparqlDefinition) extends Node {

  override def toString() : String = "VALUE("+term.toString+")"

  override def accept(n: Node): Boolean = n match {
    case _ : Something  => false
    case _ : URIRdfNode => true
    case _              => false
  }
}

case class ListValues(var terms : Seq[SparqlDefinition]) extends Node {

  override def toString() : String = "VALUES("+terms.toString+")"

  override def accept(n: Node): Boolean = n match {
    case _ : Something  => false
    case _ : URIRdfNode => true
    case _              => false
  }
}

/* Logic */
class LogicNode(val sire : Node) extends Node
case class UnionBlock(s : Node) extends LogicNode(s)
case class NotBlock(s : Node) extends LogicNode(s)

/* filter */
class FilterNode(val negation: Boolean) extends Node {
  override def accept(n: Node): Boolean = n match {
    case a : FilterNode => true
    case _ => false
  }
}
case class isBlank(override val negation: Boolean) extends FilterNode(negation) {
  override def toString() : String = negation.toString() + " isBlank"
}

case class isLiteral(override val negation: Boolean) extends FilterNode(negation) {
  override def toString() : String = negation.toString() + " isLiteral"
}

case class isURI(override val negation: Boolean) extends FilterNode(negation) {
  override def toString() : String = negation.toString() + " isURI"
}

case class Contains(value :String,override val negation: Boolean)  extends FilterNode(negation) {
  override def toString() : String =  negation.toString() + " Contains ("+value+")"
}

case class Equal(value :String,override val negation: Boolean)  extends FilterNode(negation) {
  override def toString() : String = negation.toString() + " Equal ("+value+")"
}

/* Datatype Node */
case class DatatypeNode(refNode : String, property : SubjectOf) extends Node

/* SourcesNode */
case class SourcesNode(refNode : String, sources : Seq[String]) extends Node

/* BIND / Operator */
case class OperatorNode(var operator : String) extends Node
