package inrae.semantic_web.internal

import inrae.semantic_web.SWDiscoveryException
import inrae.semantic_web.rdf._
import wvlet.log.Logger.rootLogger.debug

import java.util.UUID.randomUUID

abstract class Node(
                     idRef : String,
                     val children: Seq[Node] = Seq[Node]())
{

  def reference(): String = idRef
  def copy(children : Seq[Node]=children) : Node

  def addChildren(n: Node): Node =  copy(children :+ n )

  def addChildren(focusId : String, n: Node): Node =  {
    focusId match {
      case _ if focusId == idRef && accept(n) => copy(children.map( _.addChildren(focusId,n) ) :+ n )
      case _ if focusId == idRef && !accept(n) =>
        throw SWDiscoveryException(s"cannot add this child [${n.getClass.getSimpleName}] to the current node [${getClass.getSimpleName}]")
      case _ => copy(children.map( _.addChildren(focusId,n) ))
    }
  }

  def getRdfNode(ref : String,sep : String ="") : Option[RdfNode] = {
    debug(" -- getRdfNode -- ")
    this match {
      case  n : RdfNode if ref == n.reference() => Some(n)
      case _ if children.nonEmpty => children.flatMap(c => c.getRdfNode(ref, sep + "*")).headOption
      case _ =>  None
    }
  }

  override def toString : String = {
    this.getClass.getSimpleName+ "@"+idRef+ { children.length match {
      case l if l>0 => " ["+children.toString()+"]"
      case _ => ""
    } }
  }

  /* everything by default*/
  def accept(n: Node): Boolean = {
    true
  }

  def referencesChildren() : Seq[String] = idRef +: children.flatMap( a => { a.reference() +: a.referencesChildren() } )

  def duplicateWithoutChildren() : Node
}

/* Node case */
case class Root(
                 idRef : String=randomUUID.toString,
                 prefixes : Map[String,IRI] = Map(
                   "owl" -> IRI("http://www.w3.org/2002/07/owl#"),
                   "rdf" -> IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
                   "rdfs"->IRI("http://www.w3.org/2000/01/rdf-schema#"),
                   "xsd"->IRI("http://www.w3.org/2001/XMLSchema#")
                 ),
                 defaultGraph : Seq[IRI]    = List[IRI](),
                 namedGraph : Seq[IRI]      = List[IRI](),
                 lDatatypeNode : Seq[DatatypeNode] = List[DatatypeNode](),
                 lSourcesNodes : Seq[SourcesNode] = List[SourcesNode](),
                 lOperatorNode : Seq[OperatorNode] = List[OperatorNode](),
                 override val children: Seq[Node] = Seq[Node]()
               ) extends Node(idRef,children) {
  /* prefix management */

  def addPrefix(short : String,long : IRI) : Root = {
    Root(idRef,prefixes + (short -> long ),defaultGraph,namedGraph,lDatatypeNode,lSourcesNodes,lOperatorNode,children)
  }

  def addDefaultGraph(graph : IRI) : Root = {
    Root(idRef,prefixes,defaultGraph :+ graph,namedGraph,lDatatypeNode,lSourcesNodes,lOperatorNode,children)
  }

  def addNamedGraph(graph : IRI) : Root = {
    Root(idRef,prefixes,defaultGraph,namedGraph :+ graph,lDatatypeNode,lSourcesNodes,lOperatorNode,children)
  }

  def addSourceNode(s : SourcesNode) : Root = {
    Root(idRef,prefixes,defaultGraph,namedGraph,lDatatypeNode,lSourcesNodes :+ s,lOperatorNode,children)
  }

  def addDatatype(d : DatatypeNode) : Root = {
    Root(idRef,prefixes,defaultGraph,namedGraph,lDatatypeNode :+ d,lSourcesNodes,lOperatorNode,children)
  }

  def sourcesNode(n : RdfNode) : Option[SourcesNode] = {
    lSourcesNodes.find( p => p.refNode == n.reference() )
  }

  override def addChildren(n: Node): Root = super.addChildren(n).asInstanceOf[Root]

  override def addChildren(focusId : String, n: Node): Root = super.addChildren(focusId,n).asInstanceOf[Root]


  def copy(children : Seq[Node]) : Node = {
    Root(idRef,prefixes,defaultGraph,namedGraph,lDatatypeNode,lSourcesNodes,lOperatorNode,children)
  }

  /* Accept only something on the root */
  override def accept(n: Node): Boolean = n match {
    case _ : Something => true
    case _ => false
  }

  override def duplicateWithoutChildren() : Node = Root(idRef,prefixes,defaultGraph,namedGraph,lDatatypeNode,lSourcesNodes,lOperatorNode,Seq())
}

/* triplets */
abstract class RdfNode(idRef : String,override val children: Seq[Node] = Seq[Node]()) extends Node(idRef,children) {
  def duplicateWithoutChildren(): RdfNode
}


abstract class URIRdfNode(idRef : String,val term : SparqlDefinition,override val children: Seq[Node] = Seq[Node]())
  extends RdfNode(idRef,children)

case class Something(idRef: String,override val children: Seq[Node] = Seq[Node]()) extends RdfNode(idRef,children) {
  override def duplicateWithoutChildren(): Something = Something(idRef,Seq[Node]())
  /* everything by default*/
  override def accept(n: Node): Boolean = n match {
    case _ : Something  => false
    case _ : URIRdfNode => true
    case _ : FilterNode => true
    case _ : Value      => true
    case _ : ListValues => true
    case _              => false
  }

  def copy(children : Seq[Node]) : Node = {
    Something(idRef,children)
  }
}

case class SubjectOf(idRef : String, override val term : SparqlDefinition,override val children: Seq[Node] = Seq[Node]()) extends URIRdfNode(idRef,term,children) {
  override def duplicateWithoutChildren(): SubjectOf = SubjectOf(idRef,term,Seq[Node]())
  def copy(children : Seq[Node]) : Node = {
    SubjectOf(idRef,term,children)
  }
}

case class ObjectOf(idRef : String,override val term : SparqlDefinition,override val children: Seq[Node] = Seq[Node]()) extends URIRdfNode(idRef,term,children) {
  override def duplicateWithoutChildren(): ObjectOf = ObjectOf(idRef,term,Seq[Node]())
  def copy(children : Seq[Node]) : Node = {
    ObjectOf(idRef,term,children)
  }
}

case class LinkTo(idRef : String,override val term : SparqlDefinition,override val children: Seq[Node] = Seq[Node]()) extends URIRdfNode(idRef,term,children) {
  override def duplicateWithoutChildren(): LinkTo = LinkTo(idRef,term,Seq[Node]())
  def copy(children : Seq[Node]) : Node = {
    LinkTo(idRef,term,children)
  }
}

case class LinkFrom(idRef : String,override val term : SparqlDefinition,override val children: Seq[Node] = Seq[Node]()) extends URIRdfNode(idRef,term,children) {
  override def duplicateWithoutChildren() : LinkFrom = LinkFrom(idRef,term,Seq[Node]())
  def copy(children : Seq[Node]) : Node = {
    LinkFrom(idRef,term,children)
  }
}

case class Value(var term : SparqlDefinition,idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]()) extends Node(idRef,children) {

  override def toString : String = "VALUE("+term.toString+")"

  override def accept(n: Node): Boolean = n match {
    case _ : Something  => false
    case _ : URIRdfNode => true
    case _              => false
  }

  def copy(children : Seq[Node]) : Node = Value(term,idRef,children)

  override def duplicateWithoutChildren(): Value = Value(term,idRef,Seq())
}

case class ListValues(var terms : Seq[SparqlDefinition],idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]()) extends Node(idRef,children) {

  override def toString : String = "VALUES("+terms.toString+")"

  override def accept(n: Node): Boolean = n match {
    case _ : Something  => false
    case _ : URIRdfNode => true
    case _              => false
  }

  def copy(children : Seq[Node]) : ListValues = ListValues(terms,idRef,children)

  override def duplicateWithoutChildren(): ListValues = ListValues(terms,idRef,Seq())
}

/* Logic */
abstract class LogicNode(val sire : Node,idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]()) extends Node(idRef,children)

case class UnionBlock(s : Node,idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]()) extends LogicNode(s,idRef,children) {
  def copy(children : Seq[Node]) : Node = UnionBlock(s,idRef,children)
  def duplicateWithoutChildren(): UnionBlock = UnionBlock(s,idRef,Seq())
}


case class NotBlock(s : Node,idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]()) extends LogicNode(s,idRef,children) {
  def copy(children : Seq[Node]) : NotBlock = NotBlock(s,idRef,children)

  def duplicateWithoutChildren(): NotBlock = NotBlock(s,idRef,Seq())
}

/* filter */
abstract class FilterNode(val negation: Boolean,
                          idRef : String=randomUUID.toString,
                          override val children: Seq[Node] = Seq[Node]()) extends Node(idRef,children) {
  override def accept(n: Node): Boolean = n match {
    case _ : FilterNode => true
    case _ => false
  }
}

case class isBlank(
                   override val negation: Boolean,
                   idRef : String=randomUUID.toString,
                   override val children: Seq[Node] = Seq[Node]()) extends FilterNode(negation,idRef,children) {
  override def toString : String = negation.toString + " isBlank"

  def copy(children : Seq[Node]) : isBlank = isBlank(negation,idRef,children)
  def duplicateWithoutChildren(): isBlank = isBlank(negation,idRef,Seq())
}

case class isLiteral(override val negation: Boolean,idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]()) extends FilterNode(negation,idRef,children) {
  override def toString : String = negation.toString + " isLiteral"

  def copy(children : Seq[Node]) : Node = isLiteral(negation,idRef,children)
  def duplicateWithoutChildren(): isLiteral = isLiteral(negation,idRef,Seq())
}

case class isURI(override val negation: Boolean,idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]()) extends FilterNode(negation,idRef,children) {
  override def toString : String = negation.toString + " isURI"

  def copy(children : Seq[Node]) : isURI = isURI(negation,idRef,children)
  def duplicateWithoutChildren(): isURI = isURI(negation,idRef,Seq())
}

case class Contains(value :String,override val negation: Boolean,idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]())  extends FilterNode(negation,idRef,children) {
  override def toString : String =  negation.toString + " Contains ("+value+")"

  def copy(children : Seq[Node]) : Contains = Contains(value,negation,idRef,children)
  def duplicateWithoutChildren(): Contains = Contains(value,negation,idRef,Seq())
}

case class StrStarts(value :String,override val negation: Boolean,idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]())  extends FilterNode(negation,idRef,children) {
  override def toString : String =  negation.toString + " StrStarts ("+value+")"

  def copy(children : Seq[Node]) : StrStarts = StrStarts(value,negation,idRef,children)
  def duplicateWithoutChildren(): StrStarts = StrStarts(value,negation,idRef,Seq())
}

case class StrEnds(value :String,override val negation: Boolean,idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]())  extends FilterNode(negation,idRef,children) {
  override def toString : String =  negation.toString + " StrEnds ("+value+")"

  def copy(children : Seq[Node]) : StrEnds = StrEnds(value,negation,idRef,children)
  def duplicateWithoutChildren(): StrEnds = StrEnds(value,negation,idRef,Seq())
}

case class Equal(value :Literal,override val negation: Boolean,idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]())  extends FilterNode(negation,idRef,children) {
  override def toString : String = negation.toString + " == "+value

  def copy(children : Seq[Node]) : Equal = Equal(value,negation,idRef,children)
  def duplicateWithoutChildren(): Equal = Equal(value,negation,idRef,Seq())
}

case class NotEqual(value :Literal,override val negation: Boolean,idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]())  extends FilterNode(negation,idRef,children) {
  override def toString : String = negation.toString + " == "+value

  def copy(children : Seq[Node]) : NotEqual = NotEqual(value,negation,idRef,children)
  def duplicateWithoutChildren(): NotEqual = NotEqual(value,negation,idRef,Seq())
}

case class Inf(value :Literal,override val negation: Boolean,idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]())  extends FilterNode(negation,idRef,children) {
  override def toString : String = negation.toString + " < "+value

  def copy(children : Seq[Node]) : NotEqual = NotEqual(value,negation,idRef,children)
  def duplicateWithoutChildren(): NotEqual = NotEqual(value,negation,idRef,Seq())
}

case class InfEqual(value :Literal,override val negation: Boolean,idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]())  extends FilterNode(negation,idRef,children) {
  override def toString : String = negation.toString + " <= "+value

  def copy(children : Seq[Node]) : InfEqual = InfEqual(value,negation,idRef,children)
  def duplicateWithoutChildren(): InfEqual = InfEqual(value,negation,idRef,Seq())
}

case class Sup(value :Literal,override val negation: Boolean,idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]())  extends FilterNode(negation,idRef,children) {
  override def toString : String = negation.toString + " > "+value

  def copy(children : Seq[Node]) : Sup = Sup(value,negation,idRef,children)
  def duplicateWithoutChildren(): Sup = Sup(value,negation,idRef,Seq())
}

case class SupEqual(value :Literal,override val negation: Boolean,idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]())  extends FilterNode(negation,idRef,children) {
  override def toString : String = negation.toString + " >= "+value

  def copy(children : Seq[Node]) : SupEqual = SupEqual(value,negation,idRef,children)
  def duplicateWithoutChildren(): SupEqual = SupEqual(value,negation,idRef,Seq())
}

/* Datatype Node */
case class DatatypeNode(refNode : String, property : SubjectOf,idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]()) extends Node(idRef,children) {
  def copy(children : Seq[Node]) : DatatypeNode = DatatypeNode(refNode,property,idRef,children)
  def duplicateWithoutChildren(): DatatypeNode = DatatypeNode(refNode,property,idRef,Seq())
}

/* SourcesNode */
case class SourcesNode(refNode : String, sources : Seq[String],idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]()) extends Node(idRef,children) {
  def copy(children : Seq[Node]) : SourcesNode = SourcesNode(refNode,sources,idRef,children)
  def duplicateWithoutChildren(): SourcesNode = SourcesNode(refNode,sources,idRef,Seq())
}

/* BIND / Operator */
case class OperatorNode(operator : String,idRef : String=randomUUID.toString,override val children: Seq[Node] = Seq[Node]()) extends Node(idRef,children)  {
  def copy(children : Seq[Node]) : OperatorNode = OperatorNode(operator,idRef,children)
  def duplicateWithoutChildren(): OperatorNode = OperatorNode(operator,idRef,Seq())
}
