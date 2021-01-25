package inrae.semantic_web.internal.pm

import inrae.semantic_web.internal._

object SelectNode  {

  def getNodeWithRef(ref : String, n: Node ) : Array[RdfNode] = n match {
            case node : RdfNode  if (node.reference() == ref) => Array[RdfNode](node)
            case _   => n.children.toArray.flatMap( child => getNodeWithRef( ref, child ))
  }

}
