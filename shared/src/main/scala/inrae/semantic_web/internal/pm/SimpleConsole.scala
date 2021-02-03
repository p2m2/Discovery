package inrae.semantic_web.internal.pm

import inrae.semantic_web.internal._

/**
 *
 */
object SimpleConsole  {
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
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
        case _ : RdfNode            => Console.BLUE
        case _ : FilterNode         => Console.GREEN
        case _ : Value              => Console.CYAN
        case _                      => Console.RED
    }

    def Labelled(n: Node ) : String = {
        n match {
            case _ : Root           => "Root"
            case node : Something   => "Something ("+ node.reference() +")"
            case node : SubjectOf   => "SubjectOf ("+node.term.toString +" , " + node.reference() +")"
            case node : ObjectOf    => "ObjectOf ("+node.term.toString +" , " + node.reference() +")"
            case node : LinkTo      => "LinkTo ("+node.term.toString +" , " + node.reference() +")"
            case node : LinkFrom    => "LinkFrom ("+node.term.toString +" , " + node.reference() +")"
            case node : SourcesNode => "SourceNode -> " + node.refNode
            case node : Value       => "Value ("+node.term.toString +")"
            case node : FilterNode  => "FILTER "+ node.toString()
            case node : DatatypeNode => "DatatypeNode ("+ node.refNode  +" -> " + Labelled(node.property)+ ") "
            case v                  => "--- Unkown ---"+v.toString
        }
    }

    def get( n: Node, marge : Int = 0 ) : String = {

        val prefix =  (marge match {
            case 0 => fullb + upperhb * 100 + "\n"
            case _ => ""
        })+ fullb + escape

        val suffix =  (marge match {
            case 0 => fullb + lowerhb * 100 + "\n"
            case _ => ""
        }) + Console.RESET

        val label : String = escape + item + barrehor + " " + colorize(n) + (Labelled(n)) + Console.RESET

        val labelledLine = prefix + (escape + barrevert) * marge + label + "\n"
        val children = n.children.length match {
            case l if l > 0 => {
                   n.children.map (child => get (child, marge + 1) ).mkString ("") + suffix
            }
            case _ => ""
        }

        val sourcesNode = n match {
            case r : Root => {
                "\n" + prefix + (escape + barrevert) * marge + label + "\n"
                  "==== SOURCESNODE === \n" + r.lSourcesNodes.map(child => get(child, marge + 1) +
                    " * " + child.sources.mkString(",")  ).mkString("\n") + "\n"
            }
            case _ => ""
        }

        val datatypeNode = n match {
            case r : Root => {
                "\n" +  prefix + (escape + barrevert) * marge + label + "\n"
                "==== DATATYPE === \n" + r.lDatatypeNode.map(child => get(child, marge + 1)).mkString("\n")+ "\n"
            }
            case _ => ""
        }

        val solutionSequenceModifierNode = n match {
            case r : Root => {
                "\n" +  prefix + (escape + barrevert) * marge + label + "\n"
                "==== Solution Modifier === \n" + r.lSolutionSequenceModifierNode.map(child => get(child, marge + 1)).mkString("\n")+ "\n"
            }
            case _ => ""
        }

        val expressionNode = n match {
            case r : Root => {
                "\n" +  prefix + (escape + barrevert) * marge + label + "\n"
                "==== Expression === \n" + r.lBindNode.map(child => get(child, marge + 1)).mkString("\n")+ "\n"
            }
            case _ => ""
        }


        labelledLine + children + sourcesNode + datatypeNode + solutionSequenceModifierNode + expressionNode
    }
}
