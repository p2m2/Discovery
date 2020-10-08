package inrae.semantic_web.internal.pm

import inrae.semantic_web._
import inrae.semantic_web.internal._

/**
 * 
 */
object SparqlGenerator  {

    def prolog(listVariables : Seq[String] = Seq[String]()) : String = {
        if (listVariables.length == 0 ) {
            "SELECT * WHERE {"
        } else
            "SELECT" + listVariables.foldLeft(" ")( (acc,identifier) => acc+"?"+identifier+" ") + "WHERE {"
    }

    def solutionModifier () : String = {
        "}"
    }

    def prologSourcesSelection(variableIdentifier : String = "") : String = {
        if ( variableIdentifier == "") {
            "SELECT (COUNT(*) AS ?COUNT) WHERE {"
        } else {
            "SELECT COUNT("+variableIdentifier+") WHERE {"
        }
    }

    def solutionModifierSourcesSelection () : String = {
        "} LIMIT 1"
    }

    def sparqlNode(n: Node,varIdSire : String, variableName : String) : String = {
        n match {
            case node : SubjectOf          => "?" + varIdSire + " " +
              node.uri.toString() + " " + "?"+ variableName
            case node : ObjectOf           => "?" + variableName + " " +
              node.uri.toString() + " " + "?"+ varIdSire
            case node : Attribute          => "?" + variableName + " " +
              node.uri.toString() + " " + "?"+ varIdSire
            case node : Value              => "VALUES ?" +varIdSire+ " { " + node.uri.toString() + " }"
            case _                         => ""
        }
    }

    def setVariableIdentifier(
                       n:Node,
                       ms : Map[String,Int] = Map[String,Int](), /* map to increase id and manage new variable name */
                     ) : Option[(String,Map[String,Int])] = {

        val genericName = n match {
            case _ : Something          => "something"
            case _ : SubjectOf          => "object"
            case _ : ObjectOf           => "subject"
            case _ : Attribute          => "attribute"
            case _                      => None
        }
        genericName match {
            case s : String => {
                val v = ms.getOrElse(s,0)
                Some(genericName+v.toString(),ms + (s -> (v+1) ))
            }
        }
    }

    /* output variable : get key : referenceKeyNode, value : variable name */
    def getAllVariablesIdentifiers(n : Node,
                       referenceToIdentifier : Map[String,String] = Map[String,String](),
                       genericNametoId : Map[String,Int] = Map[String,Int]() )
    : (Map[String,String],Map[String,Int]) = {
        println(" -- assignVariable --")
        println(referenceToIdentifier)
        val (ass,ms) = n.reference() match {
            case Some(ref) => {
                setVariableIdentifier(n,genericNametoId) match {
                    case Some((v,ms2)) => (referenceToIdentifier + (ref -> v),ms2)
                    case None => (referenceToIdentifier,genericNametoId)
                }
            }
            case _ => (referenceToIdentifier,genericNametoId)
        }
        println("--reduceleft---")
        println(n.children)
        println(ass)
        /* set up assVariableNode */
        n.children.toArray.foldLeft((ass,ms)) {
            (acc,child) =>
                getAllVariablesIdentifiers(child,acc._1,acc._2)
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

        print("--***---");
        println(variableName)
        triplet + "\n"+ n.children.map( child => body( sw, child,referenceToIdentifier, variableName)).mkString(" .")
    } 
}

