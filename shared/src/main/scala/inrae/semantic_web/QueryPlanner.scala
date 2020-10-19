package inrae.semantic_web
import inrae.semantic_web.QueryPlanner.{LOGIC, Planning}

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

  case class BGP(lnodes : Seq[Node]) extends LOGIC
  case class OR(lbgp   : Seq[LOGIC]) extends LOGIC
  case class AND(lbgp : Seq[LOGIC]) extends LOGIC

  case class Planning(var steps : LOGIC)

  sealed trait RESULTS_SET
  case class INTERSECTION_RESULTS_SET( lns : Map[String,Seq[Node]]) extends RESULTS_SET
  case class OR_RESULTS_SET(lbgp   : Seq[RESULTS_SET]) extends RESULTS_SET
  case class AND_RESULTS_SET(lbgp : Seq[RESULTS_SET]) extends RESULTS_SET


  def buildPlanning(root: Root) : Planning = {
    val plan = OR(root.children.map(c => buildIndependantBGP(c)))
    Planning(cleanPlan(plan))
  }

  /*
      Split BGP( [...] ) according their SOURCES
       example:
       BGP([ N1 ->S1, N2 ->S1 ]) => INTERSECTION_RESULTS_SET ( S1 -> [BGP(N1,N2)] )
       BGP([ N1 ->S1, N2 ->S2 ]) => INTERSECTION_RESULTS_SET ( S1 -> [BGP(N1)], S2 -> [BGP(N2)] )
       BGP([ N1 ->S1,S2, N2 ->S2 ]) => INTERSECTION_RESULTS_SET ( S1 -> [BGP(N1)], S2 -> [BGP(N1,N2)] )
   */
//, config : StatementConfiguration
  def ordonnanceBySource(l : LOGIC, r : Root ) : RESULTS_SET = {
    l match {
      case BGP(lnodes) => {
        val lSourcesNodes = lnodes.map( n => r.sourcesNode(n) ).flatten
        val lbgpBySource  = lSourcesNodes.map( sn => sn.sources )
                       .flatMap( x=> x)
                       .distinct
                       .map( source => (source -> lSourcesNodes.filter( _.sources contains source ).map( sn => sn.n )))

        INTERSECTION_RESULTS_SET(lbgpBySource.toMap)
      }
      case OR(lbgp) => OR_RESULTS_SET(lbgp.map( ordonnanceBySource(_,r)))
      case AND(lbgp) => AND_RESULTS_SET(lbgp.map( ordonnanceBySource(_,r)))
    }

  }

  /*
  def checkVariable(plan : Planning, listVariables : Seq[String]) : Planning = {
    Planning(filterBgpWithVariables(plan.steps,listVariables))
  }

  def filterBgpWithVariables(l : LOGIC, listVariables : Seq[String]) : LOGIC = {
    l match {
      case BGP(lnode) => BGP(lnode.filter( n => n.reference match {
        case Some(v) => listVariables contains v
        case None => false
      }))
      case OR(lbgp) => OR(lbgp.map(filterBgpWithVariables(_,listVariables)))
      case AND(lbgp) => AND(lbgp.map(filterBgpWithVariables(_,listVariables)))
    }
  }
*/

  def cleanPlan(plan : LOGIC) : LOGIC = {
    normalize(factorize(plan)) match {
      case a : LOGIC if normalize(factorize(a)) == a => a
      case b : LOGIC => cleanPlan(b)
    }
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