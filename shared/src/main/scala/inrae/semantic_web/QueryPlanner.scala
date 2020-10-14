package inrae.semantic_web

import inrae.semantic_web.internal.{LinkFrom, LinkTo, Node, ObjectOf, Root, SubjectOf,Value}

object QueryPlanner {

  case class IndependentBGP(var lBgp : Seq[Node])
  
  case class Planning(var steps : Seq[IndependentBGP])

  def buildPlanning(root: Root, listVariables : Seq[String], config : StatementConfiguration) : Planning = {
    Planning(root.children.map(c => buildIndependantBGP(c,List())).flatten)
  }

  def buildIndependantBGP( n : Node, lbgp : Seq[Node]) : Seq[IndependentBGP] = {
    n match {
      case _ : ObjectOf | _: SubjectOf | _ : LinkTo | _: LinkFrom | _: Value => {
        n.children.map(c => buildIndependantBGP(c,lbgp :+ n)).flatten
      }
      case _ => List(IndependentBGP(lbgp))
    }
  }
}
