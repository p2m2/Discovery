package inrae.semantic_web.sparql
import inrae.semantic_web.internal.RdfNode

class SparqlGroupNode {
  def whois() : String = {
    this match {
      case _ : BGP => "BGP"
      case _ : AND => "AND"
      case _ : OR => "OR"
      case _ => "UNKNOWN"
    }
  }

  def +(that: SparqlGroupNode) : SparqlGroupNode = {
    this match {
      case BGP(lnode_this) => that match {
        case BGP(lnode_that) => BGP(lnode_this ++ lnode_that)
        case OR(lbgp) => OR( lbgp.map( this + _ ))
        case AND(lbgp) => AND( List(this) ++ lbgp)
      }
      case OR(lbgp_this) => that match {
        case OR(lbgp_that) => OR(lbgp_this ++ lbgp_that)
        case _ => OR(lbgp_this ++ List(that))
      }
      case AND(lbgp_this) => that match {
        case BGP(lnode_that) => AND( lbgp_this ++ List(that) )
        case AND(lbgp) => AND( lbgp_this ++ lbgp)
        case OR(lbgp) => OR( lbgp.map( this + _ ))
      }
    }
  }

  def display(logiceNode: SparqlGroupNode, marge : Int ) : String = {
    val stepMarge = 1

    val start = "  "*marge + logiceNode.getClass.getSimpleName +  " ["
    val item = "\n"+" "*(start.length+marge)

    start +
      {
        logiceNode match {
          case BGP(l) => l.map( _.toString ).mkString(",")
          case OR(l)  =>  item +l.map( display(_, marge+stepMarge) ).mkString(item)
          case AND(l) =>  item +l.map( display(_, marge+stepMarge) ).mkString(item)
        }
      } + "]"
  }

  override def toString = "\n"+display(this,1)
}

case class BGP(lnodes : Seq[RdfNode]) extends SparqlGroupNode
case class OR(lbgp   : Seq[SparqlGroupNode]) extends SparqlGroupNode
case class AND(lbgp : Seq[SparqlGroupNode]) extends SparqlGroupNode

