package inrae.semantic_web

import inrae.semantic_web.internal.{LinkFrom, LinkTo, Node, ObjectOf, Root, Something, SubjectOf, UnionBlock, Value}
import annotation.tailrec

object QueryPlanner {

  sealed trait LOGIC {

    def whois() : String = {
      this match {
        case _ : BGP => "BGP"
        case _ : AND => "AND"
        case _ : OR => "OR"
        case _ => "UNKNOWN"
      }
    }

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

    def display( logiceNode: LOGIC , marge : Int ) : String = {
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

  case class BGP(lnodes : Seq[Node]) extends LOGIC
  case class OR(lbgp   : Seq[LOGIC]) extends LOGIC
  case class AND(lbgp : Seq[LOGIC]) extends LOGIC


  sealed trait ORDONNANCEMENT_RESULTS_SET
  case class INTERSECTION_RESULTS_SET( lns : Map[String,Seq[Node]]) extends ORDONNANCEMENT_RESULTS_SET
  case class OR_RESULTS_SET(lbgp   : Seq[ORDONNANCEMENT_RESULTS_SET]) extends ORDONNANCEMENT_RESULTS_SET
  case class AND_RESULTS_SET(lbgp : Seq[ORDONNANCEMENT_RESULTS_SET]) extends ORDONNANCEMENT_RESULTS_SET


  def buildPlanning(root: Root) : LOGIC = {
    scribe.debug("buildPlanning")
    val plan = OR(root.children.map(c => buildIndependantBGP(c)))
    cleanPlan(plan)
  }

  /*
      Split BGP( [...] ) according their SOURCES
       example:
       BGP([ N1 ->S1, N2 ->S1 ]) => INTERSECTION_RESULTS_SET ( S1 -> [BGP(N1,N2)] )
       BGP([ N1 ->S1, N2 ->S2 ]) => INTERSECTION_RESULTS_SET ( S1 -> [BGP(N1)], S2 -> [BGP(N2)] )
       BGP([ N1 ->S1,S2, N2 ->S2 ]) => INTERSECTION_RESULTS_SET ( S1 -> [BGP(N1)], S2 -> [BGP(N1,N2)] )
   */
//, config : StatementConfiguration
  def ordonnanceBySource(l : LOGIC, r : Root ) : ORDONNANCEMENT_RESULTS_SET = {
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

  @tailrec
  def cleanPlan(plan : LOGIC) : LOGIC = {
    scribe.debug("clean plan :"+plan.toString)
    factorize(plan) match {
      case a : LOGIC if factorize(a) == a => a
      case b : LOGIC => cleanPlan(b)
    }
  }

  def factorize(l : LOGIC) : LOGIC = {
    scribe.debug("factorize :"+l.toString)

    val newn = l match {
      case and : AND => AND(and.lbgp.reverse.map(factorize(_)).reverse)
      case or  : OR => OR(or.lbgp.reverse.map(factorize(_)).reverse)
      case _ => l
    }

    val m = newn match {
      case a : AND  => {
        /* Aims : We get three children => 1) BGP List, 2) AND List, 3) OR List*/

        val bgpSet = BGP(a.lbgp.filter( elt => elt.whois() == "BGP").flatMap( _ match{ case BGP(l) => l case _ => List()}))
        val andSet = AND(a.lbgp.filter( elt => elt.whois() == "AND").flatMap( _ match{ case AND(l) => l case _ => List()}))
        val orSet = OR(a.lbgp.filter( elt => elt.whois() == "OR").flatMap( _ match{ case OR(l) => l case _ => List()}))

        if ( bgpSet.lnodes.length>0 && andSet.lbgp.length>0 && orSet.lbgp.length>0) {
          AND(List(AND(andSet.lbgp ++ List(bgpSet)),orSet))   //OR(orSet.lbgp.map( _ + bgpSet )))
        } else if( bgpSet.lnodes.length<=0 && andSet.lbgp.length>0 && orSet.lbgp.length>0) {
          AND(List(andSet,orSet))
        } else if( bgpSet.lnodes.length<=0 && andSet.lbgp.length>0 && orSet.lbgp.length<=0) {
          andSet
        } else if( bgpSet.lnodes.length<=0 && andSet.lbgp.length<=0 && orSet.lbgp.length>0) {
          orSet
        } else if( bgpSet.lnodes.length>0 && andSet.lbgp.length<=0 && orSet.lbgp.length>0) {
          OR(orSet.lbgp.map( bgpSet + _  ))
        } else if( bgpSet.lnodes.length>0 && andSet.lbgp.length<=0 && orSet.lbgp.length<=0) {
          bgpSet
        } else if( bgpSet.lnodes.length>0 && andSet.lbgp.length>0 && orSet.lbgp.length<=0) {
          AND(List(AND(andSet.lbgp ++ List(bgpSet))))
        } else {
          scribe.error("non traité.....")
          scribe.error("bgpSet.lnodes.length:"+bgpSet.lnodes.length.toString)
          scribe.error("andSet.lbgp.length:"+andSet.lbgp.length.toString)
          scribe.error("orSet.lbgp.length:"+orSet.lbgp.length.toString)
          BGP(List())
        }
      }

      case a : OR => {
        /* Aims : We get three children => 1) BGP List, 2) AND List, 3) OR List*/

        val bgpSetList = a.lbgp.filter( elt => elt.whois() == "BGP")
        val andSetList = a.lbgp.filter( elt => elt.whois() == "AND")
        val orSet = OR(a.lbgp.filter( elt => elt.whois() == "OR").flatMap( _ match{ case OR(l) => l case _ => List()}))

        if ( bgpSetList.length <= 0 && andSetList.length <= 0 ) {
          orSet
        } else if (bgpSetList.length == 1 && andSetList.length <= 0 && orSet.lbgp.length <= 0) {
          bgpSetList(0)
        } else if (bgpSetList.length <=0 && andSetList.length == 1 && orSet.lbgp.length <= 0) {
          andSetList(0)
        } else {
          OR( bgpSetList ++ andSetList ++ orSet.lbgp)
        }
      }
      case _ : BGP => l
    }
    /*
    println("============================================= END ============================================")
    println(m)
    println("-----------------------------------------------------------------------------------------------")*/
    m
  }

  //  @tailrec
  def buildIndependantBGP( n : Node , cpt : Int = 0) : LOGIC = {

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