package inrae.semantic_web.pm

import inrae.semantic_web.internal._

object SelectNode  {
    def setFocus( ref : String, n: Node ) : Array[Node] = n match {
            case node : ReferenceNode  if (node.uniqRef == ref) =>  Array[Node](node) 
            case _                                              => n.children.flatMap( child => setFocus( ref, child ))
        }
}
