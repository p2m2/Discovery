package inrae.semantic_web
import scala.scalajs.js.annotation._
import java.util.UUID.randomUUID

import inrae.semantic_web.rdf._
import inrae.semantic_web.internal.{Node, _}
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

  /* manage the creation of an unique ref */
  def getUniqueRef() : String = "_internal_" + randomUUID.toString

  /* set the current focus on the select node */
  def focus(ref : String) : SW = {
    focusNode = pm.SelectNode.setFocus(ref, rootNode)(0)
    return this
  }

  def setupnode( n : Node ) : SW = {
    focusManagement(n)

    QueryManager.setUpSourcesNode(n,config).onComplete {
      case Success(sourceNode : SourcesNode) => {
        rootNode.lSourcesNodes = rootNode.lSourcesNodes :+ sourceNode
        debug()
      }
      case Success( _ : Node) => None
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

  /* set */
  def set( uri : URI ) : SW = {
    setupnode(new Value(uri))
  }

  def debug() : SW = {
      println("USER REQUEST\n" +
        pm.SimpleConsole.get(rootNode) +
        pm.SimpleConsole.get(focusNode) +
        "QUERY PLANNER\n"+
        "todo....")
    this
  }

  def sparql() : String = {
    ""
  }

  def select() : Future[QueryResult] = {
    QueryManager.queryNode(rootNode,focusNode,config)
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
      .select
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
