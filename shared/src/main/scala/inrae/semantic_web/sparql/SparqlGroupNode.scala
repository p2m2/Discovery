package inrae.semantic_web.sparql
import inrae.semantic_web.internal._

class SparqlGroupNode {
  def whois() : String = {
    this match {
      case _ : BgpGroupe => "BGP"
      case _ : AndGroupe => "AND"
      case _ : OrGroupe => "OR"
      case _ => "UNKNOWN"
    }
  }

  def +(that: SparqlGroupNode) : SparqlGroupNode = {
    this match {
      case BgpGroupe(lnode_this) => that match {
        case BgpGroupe(lnode_that) => BgpGroupe(lnode_this ++ lnode_that)
        case OrGroupe(lbgp) => OrGroupe( lbgp.map( this + _ ))
        case AndGroupe(lbgp) => AndGroupe( List(this) ++ lbgp)
      }
      case OrGroupe(lbgp_this) => that match {
        case OrGroupe(lbgp_that) => OrGroupe(lbgp_this ++ lbgp_that)
        case _ => OrGroupe(lbgp_this ++ List(that))
      }
      case AndGroupe(lbgp_this) => that match {
        case BgpGroupe(lnode_that) => AndGroupe( lbgp_this ++ List(that) )
        case AndGroupe(lbgp) => AndGroupe( lbgp_this ++ lbgp)
        case OrGroupe(lbgp) => OrGroupe( lbgp.map( this + _ ))
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
          case BgpGroupe(l) => l.map( _.toString ).mkString(",")
          case OrGroupe(l)  =>  item +l.map( display(_, marge+stepMarge) ).mkString(item)
          case AndGroupe(l) =>  item +l.map( display(_, marge+stepMarge) ).mkString(item)
        }
      } + "]"
  }

  override def toString = "\n"+display(this,1)
}

case class BgpGroupe(lnodes : Seq[RdfNode]) extends SparqlGroupNode
case class OrGroupe(lbgp   : Seq[SparqlGroupNode]) extends SparqlGroupNode
case class AndGroupe(lbgp : Seq[SparqlGroupNode]) extends SparqlGroupNode
