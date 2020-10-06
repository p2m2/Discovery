package inrae.semantic_web.internal.pm

import inrae.semantic_web._
import inrae.semantic_web.internal._

/**
 * 
 */
object SparqlGenerator  {

    def prolog() : String = {
        "SELECT * WHERE {"
    }

    def solutionModifier () : String = {
        "}"
    }

    def prologSourcesSelection() : String = {
        "SELECT COUNT(*) WHERE {"
    }

    def solutionModifierSourcesSelection () : String = {
        "} LIMIT 1"
    }

    def sparqlNode(n: Node,varIdSire : String, variableName : String) : String = {
        n match {
            case node : SubjectOf          => "?" + varIdSire + " " +
              node.uri.toString() + " " + " ?"+ variableName +"\n"
            case node : ObjectOf           => "?" + variableName + " " +
              node.uri.toString() + " " + "?"+ varIdSire+"\n"
            case node : Attribute          => "?" + variableName + " " +
              node.uri.toString() + " " + "?"+ varIdSire +"\n"
            case node : Value              => "VALUES ?" +varIdSire+ " { " + node.uri.toString() + " }\n"
            case _                         => ""
        }
    }

    def body(sw: ConfigurationObject.Source, n: Node, varIdSire : String = "", ms : Map[String,Int] = Map[String,Int]())  : String = {

        val k : String = n match {
            case node : Something          => "something"
            case node : SubjectOf          => "object"
            case node : ObjectOf           => "subject"
            case node : Attribute          => "attribute"
            case _                         => ""
        }

        val v : Int = ms getOrElse(k,0)  

        val variableName : String = if (k != "" ) k+v.toString() else varIdSire

        val triplet : String = sparqlNode(n,varIdSire,variableName)
       
        triplet + n.children.map( child => body( sw, child, variableName ,ms + (k -> (v+1) ))).mkString(" .")
    } 
}

