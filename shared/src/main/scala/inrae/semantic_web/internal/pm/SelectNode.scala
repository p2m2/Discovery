package inrae.semantic_web.internal.pm

import inrae.semantic_web.internal._

object SelectNode  {
    def setFocus( ref : String, n: Node ) : Array[Node] = n match {
            case node : Node  if (node.uniqRef == ref) =>  Array[Node](node)
            case _                                              => n.children.toArray.flatMap( child => setFocus( ref, child ))
        }
}
