package inrae.semantic_web
import scala.scalajs.js.annotation._
import java.util.UUID.randomUUID
//import scala.scalajs.js.JSConverters._
import scala.scalajs.js._

import inrae.semantic_web.rdf._
import inrae.semantic_web.internal._
import inrae.semantic_web.sparql._

import scala.concurrent.{Future}

@JSExportTopLevel(name="EasySparqlEngine")
class SW(var config: StatementConfiguration) {

  /* root node */
  private var rootNode   : Root = new Root()
  /* focus node */
  private var focusNode  : Node = rootNode

  @JSExport
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
    var sn = new pm.SelectNode();
    focusNode = sn.setFocus(ref, rootNode)(0)

    return this
  }

  @JSExport
  def focusManagement(n : Node) : SW = {
    focusNode.addChildren(n)
    /* current node is the focusNode */
    focusNode = n
    return this
  }

  /* start a request */
  @JSExport
  def something( ref : String = getUniqueRef() ) : SW = {
    val lastNode = new Something(ref)
    /* special case when "somthing" is used. become the focus */
    focusManagement(lastNode)
  }

  /* create node which focus is the subject : ?focusId <uri> ?target */
  @JSExport
  def isSubjectOf( uri : URI , ref : String = getUniqueRef() ) : SW = {
    val lastNode = new SubjectOf(ref,uri)
    focusManagement(lastNode)
  }


  /* create node which focus is the subject : ?focusId <uri> ?target */
  @JSExport
  def isObjectOf( uri : URI , ref : String = getUniqueRef() ) : SW = {
    val lastNode = new ObjectOf(ref,uri)
    focusManagement(lastNode)
  }

  /* set */
  @JSExport
  def set( uri : URI ) : SW = {
    val lastNode = new Value(uri)
    focusManagement(lastNode)
  }
  @JSExport
  def debug() : SW = {
    var sc = new pm.SimpleConsole();
    //println( pprint.tokenize(rootNode).mkString )
    //pprint.pprintln(rootNode.children)
    println("--focus--")
    //pprint.pprintln(focusNode)
    //pprint.pprintln(focusNode.children)
    //rootNode.accept(sc)
    println(sc.get(rootNode))
    return this
  }
  @JSExport
  def sparql() : String = {
    var sg = new pm.SparqlGenerator();
    return sg.body(config, rootNode)
  }

  def select() : Future[QueryResult] = {

    //val sg = new pm.SparqlGenerator()
    //val query = sg.prolog(config, rootNode ) + "\n" + sg.body(config, rootNode ) + sg.solutionModifier(config, rootNode)
    //println(" ------------------------------- SPARQL ----------------------------- ")
    //println(query)
    //println(" ------------------------------- RESULT ----------------------------- ")

    //val futuresResults = config.conf.sources
    //    .map(source => QueryRunner(source))
    //    .map(runner => runner.query(query))

    //Future.reduceLeft(futuresResults)((a, b) => a)
    //} catch {

  }
/*
  @JSExport("select")
  def selectJS(): Promise[QueryResult] = {
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
    select().toJSPromise
  }

 */
}