package inrae.semantic_web
import scala.scalajs.js.annotation._
import java.util.UUID.randomUUID

import inrae.semantic_web.rdf._
import inrae.semantic_web.internal._
import inrae.semantic_web.sparql._

import scala.concurrent.{Future}
import scribe._

class SW(var config: StatementConfiguration) {

  /* root node */
  private var rootNode   : Root = new Root()
  /* focus node */
  private var focusNode  : Node = rootNode

  def print() : Unit = {
    println(" - SW -");
    println(" -- root --");
    //pprint.pprintln(rootNode.children)
    println(" -- focusNode --");
    //pprint.pprintln(focusNode.children)
  }

  /* manage the creation of an unique ref */
  def getUniqueRef() : String = randomUUID.toString

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
    //println( pprint.tokenize(rootNode).mkString )
    //pprint.pprintln(rootNode.children)
    println("--focus--")
    //pprint.pprintln(focusNode)
    //pprint.pprintln(focusNode.children)
    //rootNode.accept(sc)
    println(pm.SimpleConsole.get(rootNode))
    return this
  }

  def sparql() : String = {
    ""
  }

  def select() : Future[QueryResult] = {
    QueryManager.queryNode(rootNode,focusNode,config)
  }
}
