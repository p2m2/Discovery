package inrae.semantic_web
import scala.scalajs.js.annotation._
import java.util.UUID.randomUUID

import inrae.semantic_web.rdf._
import inrae.semantic_web.internal._
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
  def getUniqueRef() : String = "_internal_"+randomUUID.toString

  /* set the current focus on the select node */
  def focus(ref : String) : SW = {
    focusNode = pm.SelectNode.setFocus(ref, rootNode)(0)
    return this
  }

  def focusManagement(n : Node) : SW = {
    focusNode.addChildren(n)
    /* current node is the focusNode */
    focusNode = n
    return this
  }

  /* start a request */
  def something( ref : String = getUniqueRef() ) : SW = {
    val lastNode = new Something(ref)
    /* special case when "somthing" is used. become the focus */
    focusManagement(lastNode)
  }

  /* create node which focus is the subject : ?focusId <uri> ?target */
  def isSubjectOf( uri : URI , ref : String = getUniqueRef() ) : SW = {
    val lastNode = new SubjectOf(ref,uri)
    focusManagement(lastNode)
  }


  /* create node which focus is the subject : ?focusId <uri> ?target */
  def isObjectOf( uri : URI , ref : String = getUniqueRef() ) : SW = {
    val lastNode = new ObjectOf(ref,uri)
    focusManagement(lastNode)
  }

  /* set */
  def set( uri : URI ) : SW = {
    val lastNode = new Value(uri)
    focusManagement(lastNode)
  }

  def debug() : SW = {
    println("USER REQUEST\n")
    println(pm.SimpleConsole.get(rootNode))
    println("FOCUS NODE\n")
    println(pm.SimpleConsole.get(focusNode))
    println("QUERY PLANNER\n")
    println("todo....")
    return this
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

  def findClassOf(motherClass: URI = URI("") ) : Future[Seq[URI]] = {
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
          case Some(uri : URI) => uri
        })
      )
  }
}
