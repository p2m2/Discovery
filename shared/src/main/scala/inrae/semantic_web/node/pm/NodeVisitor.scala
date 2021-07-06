package inrae.semantic_web.node.pm

import inrae.semantic_web.node._

object NodeVisitor  {

  def getNodeWithRef(ref : String, n: Node ) : Array[RdfNode] = n match {
            case node : RdfNode  if (node.reference() == ref) => Array[RdfNode](node)
            case _   => n.children.toArray.flatMap( child => getNodeWithRef( ref, child ))
  }

  def map[A](  n : Node, deep : Integer , visitor : (Node,Integer) => A )  : Seq[A] =
    Seq(visitor(n,deep)) ++: n.children.flatMap( nc => NodeVisitor.map(nc, deep+1, visitor) )

}
