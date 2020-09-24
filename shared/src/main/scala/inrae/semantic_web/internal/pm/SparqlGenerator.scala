package inrae.semantic_web.pm

import inrae.semantic_web.StatementConfiguration
import inrae.semantic_web.internal._

/**
 * 
 */
class SparqlGenerator  {

    def prolog(sw: StatementConfiguration, n: Root ) : String = {
        "SELECT * { "
    }

    def solutionModifier (sw: StatementConfiguration, n: Root ) : String = {
        "}"
    } 

    def body(sw: StatementConfiguration, n: Node, varIdSire : String = "", ms : Map[String,Int] = Map[String,Int]())  : String = {
        
        val k : String = n match {      
            case node : Something          => "something"  
            case node : SubjectOf          => "object"   
            case node : ObjectOf           => "subject"
            case node : Attribute          => "attribute"
            case _                         => ""
        }

        val v : Int = ms getOrElse(k,0)  

        val variableName : String = if (k != "" ) k+v.toString() else varIdSire

        val triplet : String = n match {
            case node : SubjectOf          => "?" + varIdSire + " " + node.uri.toString() + " " + " ?"+ variableName 
            case node : ObjectOf           => "?" + variableName + " " + node.uri.toString() + " " + "?"+ varIdSire
            case node : Attribute          => "?" + variableName + " " + node.uri.toString() + " " + "?"+ varIdSire 
            case node : Value              => "VALUES ?" +varIdSire+ " { " + node.uri.toString() + " }"
            case _                         => ""
        }
       
        triplet + "\n" + n.children.map( child => body( sw, child, variableName ,ms + (k -> (v+1) ))).mkString(" .")
    } 
}

