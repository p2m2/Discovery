package inrae.semantic_web.internal.pm

import inrae.semantic_web.internal._
import inrae.semantic_web.rdf.RdfType

/**
 * 
 */
object SimpleConsole  {
    //full block
    def fullb : String = new String(Character.toChars(0x2588))
    //left half block
    def lefthb : String = new String(Character.toChars(0x258C))
    //right half block
    def righthb : String = new String(Character.toChars(0x2590))
    //lower half block
    def lowerhb : String = new String(Character.toChars(0x2584))
    //upper half block
    def upperhb : String  = new String(Character.toChars(0x2580))

    def item : String  = new String(Character.toChars(0x251C))
    def barrevert : String  = new String(Character.toChars(0x2502))
    def barrehor : String  = new String(Character.toChars(0x2500))

    def escape : String  = new String("  ")

    def colorize(n : Node ) : String = n match {
        case _ : Root               => Console.MAGENTA
        case _ : Something          => Console.BLUE
        case _ : SubjectOf          => Console.BLUE
        case _ : ObjectOf           => Console.BLUE
        case _ : Value              => Console.CYAN
        case _                      => Console.RED
    }

    def get( n: Node, marge : Int = 0 ) : String = {

        val prefix =  (marge match {
            case 0 => fullb + upperhb * 100 + "\n"
            case _ => ""
        })+ fullb + escape

        val suffix =  (marge match {
            case 0 => fullb + lowerhb * 100 + "\n"
            case _ => ""
        })

        val libelle : String = escape + item + barrehor + " " + colorize(n) + (n match {
            case _ : Root           => "Root"
            case node : Something   => "Something ("+ node.uniqRef +")"
            case node : SubjectOf   => "SubjectOf ("+node.uri.toString +" , " + node.uniqRef +")"
            case node : ObjectOf    => "ObjectOf ("+node.uri.toString +" , " + node.uniqRef +")"
            case node : Value       => "Value ("+node.rdfterm.toString +" , " + node.uniqRef +")"
            case v                  => "--- Unkown ---"+v.toString
        }) + Console.RESET
        prefix + (escape+barrevert) * marge + libelle + "\n" + n.children.map( child => get( child, marge+1)).mkString("") + suffix
    } 
}

