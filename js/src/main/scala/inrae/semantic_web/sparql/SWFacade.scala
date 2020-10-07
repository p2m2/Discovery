package inrae.semantic_web.sparql

import inrae.semantic_web.{SW, StatementConfiguration}
import inrae.semantic_web.internal.{Node, ObjectOf, Something, SubjectOf, Value}
import inrae.semantic_web.rdf.URI

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js.JSConverters._
import scala.scalajs.js._

@JSExportTopLevel(name="EasySparqlEngine")
class SWFacade(var config: StatementConfiguration) {
  var sw = new SW(config)

  @JSExport
  def print() : Unit = sw.print()

  @JSExport
  def focus(ref : String) : SWFacade = { sw.focus(ref) ; this }

  @JSExport
  def focusManagement(n : Node) : SWFacade = { sw.focusManagement(n) ; this }

  /* start a request */
  @JSExport
  def something( ref : String = sw.getUniqueRef() ) : SWFacade = { sw.something(ref) ; this }

  /* create node which focus is the subject : ?focusId <uri> ?target */
  @JSExport
  def isSubjectOf( uri : URI , ref : String = sw.getUniqueRef() ) : SWFacade = { sw.isSubjectOf(uri,ref) ; this }

  /* create node which focus is the subject : ?focusId <uri> ?target */
  @JSExport
  def isObjectOf( uri : URI , ref : String = sw.getUniqueRef() ) : SWFacade = { sw.isObjectOf(uri,ref); this }

  /* set */
  @JSExport
  def set( uri : URI ) : SWFacade = { sw.set(uri) ; this }

  @JSExport
  def debug() : SWFacade = { sw.debug() ; this }

  @JSExport
  def sparql() : String = sw.sparql()

  @JSExport
  def select(): Promise[QueryResult] = {
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
    sw.select().toJSPromise
  }

}
