package inrae.semantic_web.internal.pm

import inrae.semantic_web._
import inrae.semantic_web.internal._

/**
 * 
 */
object SparqlGenerator  {

    def prefixes(prefixes : Map[String,String]) : String = {
        prefixes.map {
            case (k,v) => "PREFIX "+k+": "+"<"+v+">"
        }.mkString("\n")
    }

    def prolog(listVariables : Seq[String] = Seq[String]()) : String = {
        if (listVariables.length == 0 ) {
            "SELECT * WHERE {"
        } else
            "SELECT" + listVariables.foldLeft(" ")( (acc,identifier) => acc+"?"+identifier+" ") + "WHERE {"
    }

    def solutionModifier () : String = {
        "}"
    }

    def prologSourcesSelection() : String = {
            "SELECT * WHERE {"
    }

    def solutionModifierSourcesSelection () : String = {
        "} LIMIT 1"
    }

    def sparqlNode(n: Node,varIdSire : String, variableName : String) : String = {
        n match {
            case node : SubjectOf          => "?" + varIdSire + " " +
              node.uri.toString() + " " + "?"+ variableName + " .\n"
            case node : ObjectOf           => "?" + variableName + " " +
              node.uri.toString() + " " + "?"+ varIdSire + " .\n"
            case node : Attribute          => "?" + variableName + " " +
              node.uri.toString() + " " + "?"+ varIdSire + " .\n"
            case node : Value              => "VALUES ?" +varIdSire+ " { " + node.rdfterm.toString() + " }\n"
            case _                         => ""
        }
    }

    def generic_name(n:Node) : String = {
        n match {
            case _: Something => "something"
            case _: SubjectOf => "object"
            case _: ObjectOf => "subject"
            case _: LinkTo => "linkto"
            case _: LinkFrom => "linkfrom"
            case sn: SourcesNode => generic_name(sn.n)
            case _: Attribute => "attribute"
            case _ => "unknown"
        }
    }

    /**
     *
     * @param n : Get variable from this Node
     * @param ms : Construction Map to build generic variable
     * @return Variable Name, Map [Generic name -> last index variable)
     */
    def getVariableIdentifier(
                       n:Node,
                       ms : Map[String,Int] = Map[String,Int](), /* map to increase id and manage new variable name */
                     ) : Option[(String,Map[String,Int])] = {

        n.reference() match {
            /* case if user defined an identifier */
            case Some(v) if !v.startsWith("_internal_") => Some(v, ms)
            case _ =>
                val genericName = generic_name(n)
                genericName match {
                    case s: String => {
                        val v = ms.getOrElse(s, 0)
                        Some(genericName + v.toString(), ms + (s -> (v + 1)))
                    }
                }
        }
    }

    /* output variable : get key : referenceKeyNode, value : variable name */
    def setAllVariablesIdentifiers(n : Node,
                                   referenceToIdentifier : Map[String,String] = Map[String,String](),
                                   genericNametoId : Map[String,Int] = Map[String,Int]() )
    : (Map[String,String],Map[String,Int]) = {
        val (ass,ms) = n.reference() match {
            case Some(ref) => {
                getVariableIdentifier(n,genericNametoId) match {
                    case Some((v,ms2)) => (referenceToIdentifier + (ref -> v),ms2)
                    case None => (referenceToIdentifier,genericNametoId)
                }
            }
            case _ => (referenceToIdentifier,genericNametoId)
        }
        /* set up assVariableNode */
        n.children.toArray.foldLeft((ass,ms)) {
            (acc,child) =>
                setAllVariablesIdentifiers(child,acc._1,acc._2)
        }
    }

    def body(sw: ConfigurationObject.Source, /* user configuration*/
             n: Node, /* current node to browse with children */
             referenceToIdentifier : Map[String,String],
             varIdSire : String = "" /* sire variable */
            )  : String = {


        val variableName : String =  n.reference()  match {
            case Some(id) => referenceToIdentifier(id)
            case None => varIdSire
        }

        val triplet : String = sparqlNode(n,varIdSire,variableName)
        triplet + n.children.map( child => body( sw, child,referenceToIdentifier, variableName)).mkString("")
    } 
}

