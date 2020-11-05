package inrae.semantic_web.internal.pm

import inrae.semantic_web.internal._

object SelectNode  {
    def getNodeWithRef(ref : String, n: Node ) : Array[Node] = n match {
            case node : RdfNode  if (node.reference() == ref) => Array[Node](node)
            case _   => n.children.toArray.flatMap( child => getNodeWithRef( ref, child ))
    }
}
