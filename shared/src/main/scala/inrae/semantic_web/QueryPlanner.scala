package inrae.semantic_web
import scala.util.Sorting

import scala.annotation.tailrec
import inrae.semantic_web.internal.{LinkFrom, LinkTo, Node, ObjectOf, Root, Something, SubjectOf, UnionBlock, Value}

object QueryPlanner {

  sealed trait LOGIC {

    def +(that: LOGIC) : LOGIC = {
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
  }

  case class BGP(lnode : Seq[Node]) extends LOGIC
  case class OR(lbgp   : Seq[LOGIC]) extends LOGIC
  case class AND(lbgp  : Seq[LOGIC]) extends LOGIC

  case class Planning(var steps : LOGIC)

  def buildPlanning(root: Root) : Planning = {
    val plan = OR(root.children.map(c => buildIndependantBGP(c)))
    Planning(cleanPlan(plan))
  }

  def cleanPlan(plan : LOGIC) : LOGIC = {
    normalize(factorize(plan)) match {
      case a : LOGIC if normalize(factorize(a)) == a => a
      case b : LOGIC => cleanPlan(b)
    }
  }

  def buildPlanningSource( listVariables : Seq[String], config : StatementConfiguration) = {

  }

  def compareLogic(a:LOGIC, b:LOGIC) = {
    a match {
      case _ : AND => false
      case _ => b match {
        case _ : AND => true
        case _ => false
      }
    }
  }
  /*
        groupe OR and AND LOGIC at the same children level
        by convention, OR is at the end on the children array
   */
  def normalize(l : LOGIC) : LOGIC = {

    l match {
      case a: AND if a.lbgp.length <= 1 =>{
        normalize(a.lbgp(0))
      }
      case a: OR if a.lbgp.length <= 1 => {
        normalize(a.lbgp(0))
      }
      case a: AND => {
        AND((a.lbgp).sortWith(compareLogic).map(normalize(_)))
      }
      case a: OR => {
        OR((a.lbgp).sortWith(compareLogic).map(normalize(_)))
      }
      case _ => l
    }
  }


  def factorize(l : LOGIC) : LOGIC = {
    l match {
      case a : AND => a.lbgp.reduceLeft( (l1,l2) => l1 + l2 )
      case a : OR => OR(a.lbgp.map( l => factorize(l) ))
      case _ => l
    }
  }

  //  @tailrec
  def buildIndependantBGP( n : Node ) : LOGIC = {

    n match {
      case _ : ObjectOf | _: SubjectOf | _ : LinkTo | _: LinkFrom | _: Value | _: Something => {
        AND(BGP(List(n)) +: n.children.map(c => buildIndependantBGP(c)))
      }
      case _ : UnionBlock  =>
        OR(n.children.map(c => buildIndependantBGP(c)))

      case _ => {
        scribe.error("not managed " + n.toString)
        throw new Exception("Not manager")
      }
    }
  }
}