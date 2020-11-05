package inrae.semantic_web
import scala.scalajs.js.annotation._
import java.util.UUID.randomUUID

import inrae.semantic_web.rdf._
import inrae.semantic_web.internal.{Node, RdfNode, _}
import inrae.semantic_web.sparql._

import scala.concurrent.Future
import scribe._

import scala.util.{Failure, Success}

case class SW(var config: StatementConfiguration) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  /* root node */
  private var rootNode   : Root = new Root()
  /* focus node */
  private var focusNode  : Node = rootNode

  private val version : String = "0.0.1"

  scribe.Logger.root.clearHandlers().clearModifiers().withHandler(minimumLevel = Some(Level.Info)).replace()

  def help() : SW = {
    println(" ---------------- SW "+version+" ---------------------------")
    println("   ")
    println("    -------------  Query Control ----------")
    println(" something:")
    println(" focus    :")
    println("   ")
    println("    -------------  Add Sparql snippet ----------")
    println(" isSubjectOf(URI(\"http://relation\")):  ?currentFocus URI(\"http://relation\") ?newFocus")
    println(" isObjectOf(URI(\"http://relation\")):  ?newFocus URI(\"http://relation\") ?currentFocus")
    println(" isLinkTo(URI(\"http://object\")):  ?currentFocus ?newFocus URI(\"http://object\")")
    println(" isLinkTo(XSD(\"type\",\"value\")):  ?currentFocus ?newFocus XSD(\"type\",\"value\")")
    println(" isLinkFrom(URI(\"http://object\")):  URI(\"http://object\") ?newFocus ?currentFocus")
    println("   ")
    println("    -------------  Print information ----------")
    println(" debug:")
    println(" sparql_console:")
    println("   ")
    println("    -------------  Request ----------")
    println(" select:")
    println(" count:")
    println("   ")
    println("    -------------  Explore according the focus ----------")
    println(" findClassesOf:")
    println(" findObjectPropertiesOf:")
    println(" findDatatypePropertiesOf:")
    println("   ")
    println("  --------------------------------------------------------------" )
    this
  }

  /* manage the creation of an unique ref */
  def getUniqueRef() : String = "_internal_" + randomUUID.toString

  /* set the current focus on the select node */
  def focus(ref : String) : SW = {
    val arrNode = pm.SelectNode.getNodeWithRef(ref, rootNode)

    if ( arrNode.length > 0 ) {
      focusNode = arrNode(0)
    } else {
      System.err.println("ref unknown :"+ref)
      scribe.error("ref unknown :"+ref)
    }
    return this
  }

  def prefix(short : String, long : IRI ) : SW = {
    rootNode.prefixes = rootNode.prefixes + ( short -> long )
    this
  }

  def graph(graph : IRI) : SW = {
    rootNode.defaultGraph = rootNode.defaultGraph :+ graph
    this
  }

  def namedGraph(graph : IRI ) : SW = {
    rootNode.namedGraph = rootNode.namedGraph :+ graph
    this
  }

  def setupnode(n : Node, upsource : Boolean = false ) : SW = {

    focusManagement(n)

    if ( upsource ) {
      QueryManager.setUpSourcesNode(n,config,rootNode.prefixes).onComplete {
        case Success(Some(sn)) => {
          rootNode.lSourcesNodes = rootNode.lSourcesNodes :+ sn
        }
        case _  => None
      }
    }

    this
  }

  def focusManagement(n : Node) : SW = {
    focusNode.addChildren(n)
    /* current node is the focusNode */
    focusNode = n
    return this
  }

  /* start a request with a variable */
  def something( ref : String = getUniqueRef() ) : SW = {
    setupnode(new Something(ref))
  }

  /* create node which focus is the subject : ?focusId <uri> ?target */
  def isSubjectOf( uri : URI , ref : String = getUniqueRef() ) : SW = {
    setupnode(new SubjectOf(ref,uri))
  }


  /* create node which focus is the subject : ?target <uri> ?focusId */
  def isObjectOf( uri : URI , ref : String = getUniqueRef() ) : SW = {
    setupnode(new ObjectOf(ref,uri))
  }

  /* create node which focus is the properties :
  ?focusId ?target <uri>|literal
  */
  def isLinkTo( term : RdfType , ref : String = getUniqueRef() ) : SW = {
    setupnode(new LinkTo(ref,term))
  }

  /* create node which focus is the properties :
     <uri> ?target ?focusId
  */
  def isLinkFrom( uri : URI , ref : String = getUniqueRef() ) : SW = {
    setupnode(new LinkFrom(ref,uri))
  }

  /*
    Specific treatment : add value possibilities for a specific node
    We get the
  */
  def set( uri : URI ) : SW = {
    setupnode(Value(uri))
  }

  def debug() : SW = {
      println("USER REQUEST\n" +
        pm.SimpleConsole.get(rootNode) +
        pm.SimpleConsole.get(focusNode) +
        "QUERY PLANNER\n"+
        "todo....")
    this
  }

  def sparql_console() : SW = {
    println(QueryManager.sparql_string(rootNode,focusNode))
    this
  }

  def select(lRef: Seq[String] = List()) : Future[QueryResult] = {
    val lSelectVariables = lRef match {
      case v if v.length>0 => v.flatMap( ref => {
        val variableNameList = pm.SelectNode.getNodeWithRef(ref, rootNode)
          .map( pm.SparqlGenerator.correspondanceVariablesIdentifier(_)._1.getOrElse(ref,""))
        if (variableNameList.filter(_ != "").length==0) {
          scribe.error("Unknown reference:"+ref)
        }
        variableNameList
      })
      case _ => pm.SparqlGenerator.correspondanceVariablesIdentifier(focusNode)._1.values.toSeq
    }
    QueryManager.queryVariables(rootNode,lSelectVariables,config)
  }

  def count() : Future[Int] = {
    QueryManager.countNbSolutions(rootNode,config)
      .map (v => v match {
        case Some(literal : Literal) => literal.value.toInt
        case _ => 0
      })
  }

  def findClassesOf(motherClass: URI = URI("") ) : Future[Seq[Option[URI]]] = {
    (motherClass match {
      case uri : URI if uri == URI("")  => isSubjectOf(URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),"_esp___type")
      case _ : URI =>  isSubjectOf(URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),"_esp___type")
                  .isSubjectOf(URI("a"))
                  .set(motherClass)
    })
      .focus("_esp___type")
      .select()
      .map( resultsformat =>
        resultsformat.get.rows.map(
          resulstrow => {
            resulstrow.key("_esp___type")
          }
        ).map(option => option match {
          case Some(uri : URI) => Some(uri)
          case _ => None
        })
      )
  }

  def findObjectPropertiesOf(motherClassProperties: URI = URI("") ) : Future[Seq[URI]] = {
    //QueryManager.queryPropertyNode(rootNode,focusNode,config)
    Future {
      Seq[URI]()
    }
  }
  def findDatatypePropertiesOf(motherClassProperties: URI = URI("") ) : Future[Seq[URI]] = {
    //check motherClassProperties => xsd type
    //isLiteral
    Future {
      Seq[URI]()
    }
  }
}
