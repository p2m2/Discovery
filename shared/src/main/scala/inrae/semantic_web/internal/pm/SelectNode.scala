package inrae.semantic_web.internal.pm

import inrae.semantic_web.internal._

object SelectNode  {

  def getNodeWithRef(ref : String, n: Node ) : Array[RdfNode] = n match {
            case node : RdfNode  if (node.reference() == ref) => Array[RdfNode](node)
            case _   => println(n.toString()+" compare With *"+ref+"*") ; n.children.toArray.flatMap( child => getNodeWithRef( ref, child ))
  }

  /* Get the reference node if exist otherwise recursively the reference sire */
  def getNodeRef(base : Node, f: Node) : String = base match {
    case node : RdfNode  if (node == f) => node.reference()
    case _   => {
      val l = base.children.toArray.map( child => getNodeRef( child, f )).filter( _ != "")
      if ( l.length>0) l(0)
      else
        ""
    }
  }
}
