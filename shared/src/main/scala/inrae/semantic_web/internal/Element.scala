package inrae.semantic_web.internal

import inrae.semantic_web.rdf._

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


class URIRdfNode(concretUniqRef : String,val term : SparqlDefinition) extends RdfNode(concretUniqRef)

case class Something(concretUniqRef: String) extends RdfNode(concretUniqRef) {
  override def duplicateWithoutChildren() = Something(concretUniqRef)
  /* everything by default*/
  override def accept(n: Node): Boolean = n match {
    case _ : Something  => false
    case _ : URIRdfNode => true
    case _ : FilterNode => true
    case _ : Value      => true
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

/* SourcesNode */
case class SourcesNode(n : RdfNode, sources : Seq[String]) extends Node

/* BIND / Operator */
case class OperatorNode(var operator : String) extends Node