package inrae.semantic_web.internal.pm
import inrae.semantic_web.internal._
import inrae.semantic_web.rdf.{IRI, QueryVariable}
import wvlet.log.Logger.rootLogger._


final case class SparqlGeneratorException(private val message: String = "",
                                    private val cause: Throwable = None.orNull) extends Exception(message,cause)
/**
 *
 */
object SparqlGenerator  {

    def prefixes(prefixes : Map[String,IRI]) : String = {
        prefixes.map {
            case (k,v) => "PREFIX "+k+": "+v.sparql()
        }.mkString("\n")
    }

    def from(graphs : Seq[IRI]): String = graphs.map( g => "FROM "+g.sparql()).mkString("\n")

    def fromNamed(graphs : Seq[IRI]): String = graphs.map( g => "FROM NAMED "+g.sparql()).mkString("\n")

    def solutionSequenceModifierStart(root : Root) : String = {
        println("---------------------------------------------------aaaa")
        root.lSolutionSequenceModifierNode.map( r => println(r.children) )

        "SELECT " + {
            root.lSolutionSequenceModifierNode.filter {
                case _ : Distinct => true
                case _ => false
            }.lastOption.map(sparqlNode(_,"","")).getOrElse("")
        } + {
            root.lSolutionSequenceModifierNode.filter {
                case _ : Reduced => true
                case _ => false
            }.lastOption.map(sparqlNode(_,"","")).getOrElse("")
        } + {
            root.lSolutionSequenceModifierNode.filter {
                case _ : Projection => true
                case _ => false
            }.lastOption.map( proj => {
                (sparqlNode(proj,"","")
                  + proj.children.map( child => body( child, "")).mkString(""))
            }
            ).getOrElse("")
        } + "\n" +
          from(root.defaultGraph) +"\n"+
          fromNamed(root.namedGraph) +"\n"+
          "WHERE {"
    }

    def solutionSequenceModifierEnd(root : Root) : String = {

        "} " + {
            root.lSolutionSequenceModifierNode.filter {
                case l : Limit if l.value>0 => true
                case _ => false
            }.lastOption.map(sparqlNode(_,"","")).getOrElse("")
        } + {
            root.lSolutionSequenceModifierNode.filter {
                case o : Offset if o.value > 0 => true
                case _ => false
            }.lastOption.map(sparqlNode(_,"","")).getOrElse("")
        } + {
            root.lSolutionSequenceModifierNode.filter {
                case o : OrderByAsc if o.list.length>0 => true
                case _ => false
            }.lastOption.map(sparqlNode(_,"","")).getOrElse("")
        } + {
            root.lSolutionSequenceModifierNode.filter {
                case d : OrderByDesc if d.list.length>0 =>  true
                case _ => false
            }.lastOption.map(sparqlNode(_,"","")).getOrElse("")
        }
    }

    def prologCountSelection(varCount : String) : String = {
        "SELECT ( COUNT(*) as ?"+varCount+" )"
    }


    def sparqlNode(n: Node,
                   varIdSire : String,
                   variableName : String) : String = {
        trace(varIdSire+" - "+variableName)
         n match {
            case node : SubjectOf          => "\t?" + varIdSire + " " + node.term.toString + " " + "?"+ variableName + " .\n"
            case node : ObjectOf           => "\t?" + variableName + " " + node.term.toString + " " + "?"+ varIdSire + " .\n"
            case node : LinkTo           => "\t?"+ varIdSire + " " + "?" + variableName + " " + node.term.toString + " .\n"
            case node : LinkFrom           => node.term.toString + " " + "?" + variableName + " " + "?"+ varIdSire + " .\n"
            case node : Value              => node.term match {
                case _ : QueryVariable => "BIND ( ?" + varIdSire +  " AS " + node.term.toString + ")"
                case _  =>  "VALUES ?" +varIdSire+ " { " + node.term.toString + " } .\n" }
            case node : ListValues         => "VALUES ?" +varIdSire+ " { " + node.terms.map(t => t.sparql()).mkString(" ") + " } .\n"
            case _ : Distinct              => "DISTINCT "
            case _ : Reduced               => "REDUCED "
            case node : Projection         => node.list.mkString(" ")
            case node : Limit              => "LIMIT " + node.value + " "
            case node : Offset             => "OFFSET " + node.value + " "
            case node : OrderByAsc         => "ORDER BY (" + node.list.mkString(" ") + ")" + " "
            case node : OrderByDesc        => "ORDER BY DESC (" + node.list.mkString(" ") + ")" + " "
            case node : FilterNode         => "FILTER ( " + {
                if (node.negation) {
                    "!"
                } else {
                    ""
                }
            } + {
                node match {
                    case n : Contains           => "contains(str(" + "?" +varIdSire + "),\""+ n.value + "\")"
                    case n : StrStarts          => "strStarts(str(" + "?" +varIdSire + "),\""+ n.value + "\")"
                    case n : StrEnds            => "strEnds(str(" + "?" +varIdSire + "),\""+ n.value + "\")"
                    case n : Equal              => "(?" +varIdSire + "="+ n.value + ")"
                    case n : NotEqual           => "(?" +varIdSire + "!="+ n.value + ")"
                    case n : Inf                => "(?" +varIdSire + "<" + n.value + ")"
                    case n : InfEqual           => "(?" +varIdSire + "<=" + n.value + ")"
                    case n : Sup                => "(?" +varIdSire + ">" + n.value + ")"
                    case n : SupEqual           => "(?" +varIdSire + ">=" + n.value + ")"
                    case _ : isBlank            => "isBlank(" + "?" +varIdSire + ")"
                    case _ : isURI              => "isURI(" + "?" +varIdSire + ")"
                    case _ : isLiteral          => "isLiteral(" + "?" +varIdSire + ")"
                    case _ => throw new Exception("SparqlGenerator::sparqlNode . [Devel error] Node undefined ["+n.toString+"]")
                }
            } + " )\n"
            case _ : Root| _ : Something         => ""
            case _                               => throw new Error("Not implemented yet :"+n.getClass.getName)
        }
    }

    def body(n: Node, /* current node to browse with children */
             varIdSire : String = "" /* sire variable */
            )  : String = {
        val variableName : String = n match {
            case rdf : RdfNode => rdf.reference()
            case _ => varIdSire
        }

        trace(n.toString)
        trace("varIdSire:"+varIdSire)
        trace("variableName:"+variableName)
        val triplet : String = sparqlNode(n,varIdSire,variableName)
        triplet + n.children.map( child => body( child, variableName)).mkString("")
    }
}
