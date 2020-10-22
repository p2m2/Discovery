package inrae.semantic_web.internal.pm

import inrae.semantic_web._
import inrae.semantic_web.internal.Node.references
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

    def prologCountSelection(varCount : String) : String = {
      //  variable match {
        "SELECT ( COUNT(*) as ?"+varCount+" ) WHERE {"
           // case _ => "SELECT ( COUNT(?"+variable+") as ?"+varCount+" ) WHERE {"
        //}
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

    def generic_name(n:RdfNode) : String = {
        n match {
            case _: Something => "something"
            case _: SubjectOf => "object"
            case _: ObjectOf => "subject"
            case _: LinkTo => "linkto"
            case _: LinkFrom => "linkfrom"
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
                       n:RdfNode,
                       ms : Map[String,Int] = Map[String,Int](), /* map to increase id and manage new variable name */
                     ) : Option[(String,Map[String,Int])] = {

        if ( ! n.reference().startsWith("_internal_")) {
            Some(n.reference(), ms)
        } else {
            val genericName = generic_name(n)
            genericName match {
                case s: String => {
                    val v = ms.getOrElse(s, 0)
                    Some(genericName + n.reference(), ms + (s -> (v + 1)))
                }
            }
        }
    }

    def correspondanceVariablesIdentifier(n:Node, buildMap : Map[String,Int]= Map[String,Int]())
                                       : (Map[String,String],Map[String,Int]) = {
        val resLoc : (Map[String,String],Map[String,Int]) = n match {
            case rdf : RdfNode => {
                val name = generic_name(rdf)
                val newBuildMap : Map[String,Int] = {
                    buildMap.get(name) match {
                        case Some(v: Int) => buildMap + (name -> (v + 1))
                        case None => buildMap + (name -> 0)
                    }
                }
                (Map(rdf.reference() -> (name + newBuildMap(name).toString)),newBuildMap)
                }
            case _ => (Map(),buildMap)
            }

        n.children.toArray.foldLeft(  resLoc ) {
            (acc, child) => {
                val r = correspondanceVariablesIdentifier(child,acc._2)
                (acc._1 ++ r._1,r._2)
            }
         }
    }

    def body(sw: ConfigurationObject.Source, /* user configuration*/
             n: Node, /* current node to browse with children */
             referenceToIdentifier : Map[String,String],
             varIdSire : String = "" /* sire variable */
            )  : String = {

        val variableName : String = n match {
            case rdf : RdfNode => {
                referenceToIdentifier.get(rdf.reference()) match {
                    case Some(value) => value
                    case None => varIdSire
            } }
            case _ => varIdSire
        }

        val triplet : String = sparqlNode(n,varIdSire,variableName)
       
        triplet + n.children.map( child => body( sw, child,referenceToIdentifier, variableName)).mkString("")
    } 
}

