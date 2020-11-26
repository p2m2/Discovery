package inrae.semantic_web

import inrae.semantic_web.sparql.{SparqlGroupNode,OrGroupe,AndGroupe,BgpGroupe}
import inrae.semantic_web.internal.{Node,RdfNode, Root, UnionBlock}
import annotation.tailrec

object QueryPlanner {

  sealed trait ORDONNANCEMENT_RESULTS_SET
  case class INTERSECTION_RESULTS_SET( lns : Map[String,Seq[RdfNode]]) extends ORDONNANCEMENT_RESULTS_SET
  case class OR_RESULTS_SET(lbgp   : Seq[ORDONNANCEMENT_RESULTS_SET]) extends ORDONNANCEMENT_RESULTS_SET
  case class AND_RESULTS_SET(lbgp : Seq[ORDONNANCEMENT_RESULTS_SET]) extends ORDONNANCEMENT_RESULTS_SET


  def buildPlanning(root: Root) : SparqlGroupNode = {
    scribe.debug("buildPlanning")
    val plan = OrGroupe(root.children.map(c => buildIndependantBGP(c)))
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
  def ordonnanceBySource(l : SparqlGroupNode, r : Root ) : ORDONNANCEMENT_RESULTS_SET = {
    l match {
      case BgpGroupe(lnodes) => {
        val lSourcesNodes = lnodes.map( n => r.sourcesNode(n) ).flatten
        val lbgpBySource  = lSourcesNodes.map( sn => sn.sources )
                       .flatMap( x=> x)
                       .distinct
                       .map( source => (source -> lSourcesNodes.filter( _.sources contains source ).map( sn => r.getRdfNode(sn.refNode).getOrElse(null)  )))

        INTERSECTION_RESULTS_SET(lbgpBySource.toMap)
      }
      case OrGroupe(lbgp) => OR_RESULTS_SET(lbgp.map( ordonnanceBySource(_,r)))
      case AndGroupe(lbgp) => AND_RESULTS_SET(lbgp.map( ordonnanceBySource(_,r)))
    }

  }

  @tailrec
  def cleanPlan(plan : SparqlGroupNode) : SparqlGroupNode = {
    scribe.debug("clean plan :"+plan.toString)
    factorize(plan) match {
      case a : SparqlGroupNode if factorize(a) == a => a
      case b : SparqlGroupNode => cleanPlan(b)
    }
  }

  def factorize(l : SparqlGroupNode) : SparqlGroupNode = {
    scribe.debug("factorize :"+l.toString)

    {
      l match {
        case and: AndGroupe => AndGroupe(and.lbgp.reverse.map(factorize(_)).reverse)
        case or: OrGroupe => OrGroupe(or.lbgp.reverse.map(factorize(_)).reverse)
        case _ => l
      }
    } match {
      case a : AndGroupe  => {
        /* Aims : We get three children => 1) BGP List, 2) AND List, 3) OR List*/

        val bgpSet = BgpGroupe(a.lbgp.filter(elt => elt.whois() == "BGP").flatMap( _ match{ case BgpGroupe(l) => l case _ => List()}))
        val andSet = AndGroupe(a.lbgp.filter(elt => elt.whois() == "AND").flatMap( _ match{ case AndGroupe(l) => l case _ => List()}))
        val orSet = OrGroupe(a.lbgp.filter(elt => elt.whois() == "OR").flatMap( _ match{ case OrGroupe(l) => l case _ => List()}))

        if ( bgpSet.lnodes.length>0 && andSet.lbgp.length>0 && orSet.lbgp.length>0) {
          AndGroupe(List(AndGroupe(andSet.lbgp ++ List(bgpSet)),orSet))   //OR(orSet.lbgp.map( _ + bgpSet )))
        } else if( bgpSet.lnodes.length<=0 && andSet.lbgp.length>0 && orSet.lbgp.length>0) {
          AndGroupe(List(andSet,orSet))
        } else if( bgpSet.lnodes.length<=0 && andSet.lbgp.length>0 && orSet.lbgp.length<=0) {
          andSet
        } else if( bgpSet.lnodes.length<=0 && andSet.lbgp.length<=0 && orSet.lbgp.length>0) {
          orSet
        } else if( bgpSet.lnodes.length>0 && andSet.lbgp.length<=0 && orSet.lbgp.length>0) {
          OrGroupe(orSet.lbgp.map( bgpSet + _  ))
        } else if( bgpSet.lnodes.length>0 && andSet.lbgp.length<=0 && orSet.lbgp.length<=0) {
          bgpSet
        } else if( bgpSet.lnodes.length>0 && andSet.lbgp.length>0 && orSet.lbgp.length<=0) {
          AndGroupe(List(AndGroupe(andSet.lbgp ++ List(bgpSet))))
        } else {
          scribe.error("non traité.....")
          scribe.error("bgpSet.lnodes.length:"+bgpSet.lnodes.length.toString)
          scribe.error("andSet.lbgp.length:"+andSet.lbgp.length.toString)
          scribe.error("orSet.lbgp.length:"+orSet.lbgp.length.toString)
          BgpGroupe(List())
        }
      }

      case a : OrGroupe => {
        /* Aims : We get three children => 1) BGP List, 2) AND List, 3) OR List*/

        val bgpSetList = a.lbgp.filter( elt => elt.whois() == "BGP")
        val andSetList = a.lbgp.filter( elt => elt.whois() == "AND")
        val orSet = OrGroupe(a.lbgp.filter(elt => elt.whois() == "OR").flatMap( _ match{ case OrGroupe(l) => l case _ => List()}))

        if ( bgpSetList.length <= 0 && andSetList.length <= 0 ) {
          orSet
        } else if (bgpSetList.length == 1 && andSetList.length <= 0 && orSet.lbgp.length <= 0) {
          bgpSetList(0)
        } else if (bgpSetList.length <=0 && andSetList.length == 1 && orSet.lbgp.length <= 0) {
          andSetList(0)
        } else {
          OrGroupe( bgpSetList ++ andSetList ++ orSet.lbgp)
        }
      }
      case _ : BgpGroupe => l
    }
  }

  //  @tailrec
  def buildIndependantBGP( n : Node , cpt : Int = 0) : SparqlGroupNode = {

    n match {
      case r : RdfNode => {
        AndGroupe(BgpGroupe(List(r.duplicateWithoutChildren())) +: n.children.map(c => buildIndependantBGP(c)))
      }
      case _ : UnionBlock  =>
        OrGroupe(n.children.map(c => buildIndependantBGP(c)))

      case _ => {
        scribe.error("not managed " + n.toString)
        throw new Exception("Not manager")
      }
    }
  }
}