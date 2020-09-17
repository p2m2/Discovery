package inrae.semantic_web.pm

import inrae.semantic_web.internal._

/**
 * 
 */
class SimpleConsole  {

    def get( n: Node, marge : String = "" ) : String = {
        val libelle : String = n match {
            case node : Root               => "Root"
            case node : Something          => "Something ("+ node.uniqRef +")"
            case node : SubjectOf          => "SubjectOf ("+node.uri.toString() +" , " + node.uniqRef +")"
            case node : ObjectOf           => "ObjectOf ("+node.uri.toString() +" , " + node.uniqRef +")"
            case _                         => " * Unkown *"
        }
        marge + libelle + "\n" + n.children.map( child => get( child, marge+" ")).mkString("")
    } 
}

