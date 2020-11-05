package inrae.semantic_web.sparql

import inrae.semantic_web.{SW, StatementConfiguration}
import inrae.semantic_web.internal.{Node, ObjectOf, Something, SubjectOf, Value}
import inrae.semantic_web.rdf.{IRI, URI}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js.JSConverters._
import scala.scalajs.js._

@JSExportTopLevel(name="EasySparqlEngine")
class SWFacade(var config: StatementConfiguration) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  var sw = new SW(config)

  @JSExport
  def help() : SWFacade = { sw.help() ; this }

  @JSExport
  def focus(ref : String) : SWFacade = { sw.focus(ref) ; this }

  @JSExport
  def focusManagement(n : Node) : SWFacade = { sw.focusManagement(n) ; this }

  @JSExport
  def prefix(short : String, long : IRI ) : SWFacade = { sw.prefix(short,long) ; this }

  @JSExport
  def graph(graph : IRI) : SWFacade = { sw.graph(graph) ; this }

  @JSExport
  def namedGraph(graph : IRI ) : SWFacade = { sw.namedGraph(graph) ; this }
  /* start a request */
  @JSExport
  def something( ref : String = sw.getUniqueRef() ) : SWFacade = { sw.something(ref) ; this }

  /* create node which focus is the subject : ?focusId <uri> ?target */
  @JSExport
  def isSubjectOf( uri : URI , ref : String = sw.getUniqueRef() ) : SWFacade = { sw.isSubjectOf(uri,ref) ; this }

  /* create node which focus is the subject : ?focusId <uri> ?target */
  @JSExport
  def isObjectOf( uri : URI , ref : String = sw.getUniqueRef() ) : SWFacade = { sw.isObjectOf(uri,ref); this }

  @JSExport
  def isLinkTo( uri : URI , ref : String = sw.getUniqueRef() ) : SWFacade = { sw.isLinkTo(uri,ref); this }

  @JSExport
  def isLinkFrom( uri : URI , ref : String = sw.getUniqueRef() ) : SWFacade = { sw.isLinkFrom(uri,ref); this }
  /* set */
  @JSExport
  def set( uri : URI ) : SWFacade = { sw.set(uri) ; this }

  @JSExport
  def debug() : SWFacade = { sw.debug() ; this  }

  @JSExport
  def sparql_console() : SWFacade = { sw.sparql_console() ; this }

  @JSExport
  def select(lRef: String*): Promise[QueryResult] = { sw.select(lRef).toJSPromise }

  @JSExport
  def count(): Promise[Int] = { sw.count().toJSPromise }

  @JSExport
  def findClassesOf(uri:URI = URI("")): Promise[Seq[Option[URI]]] = { sw.findClassesOf().toJSPromise }

  @JSExport
  def findObjectPropertiesOf(motherClassProperties: URI = URI("") ) : Promise[Seq[URI]] = {
    sw.findObjectPropertiesOf().toJSPromise
  }

  @JSExport
  def findDatatypePropertiesOf(motherClassProperties: URI = URI("") ) : Promise[Seq[URI]] = {
    sw.findDatatypePropertiesOf().toJSPromise
  }
}
