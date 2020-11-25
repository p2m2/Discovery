package inrae.semantic_web.sparql

import inrae.semantic_web.{SW, StatementConfiguration}
import inrae.semantic_web.internal.{Node, ObjectOf, Something, SubjectOf, Value}
import inrae.semantic_web.rdf.{IRI, URI}
import ujson.IndexedValue.False

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js.JSConverters._
import scala.scalajs.js._
import scala.scalajs._

@JSExportTopLevel("FilterIncrement")
class FilterIncrement(swf: SWFacade)  {
  var negation = false

  @JSExport
  def isLiteral: SWFacade = { swf.sw.filter.isLiteral ; swf }

  @JSExport
  def isUri: SWFacade = { swf.sw.filter.isUri ; swf }

  @JSExport
  def isBlank: SWFacade = { swf.sw.filter.isBlank ; swf }

  @JSExport
  def contains(l:String): SWFacade = { swf.sw.filter.contains(l) ; swf }

  @JSExport
  def not : FilterIncrement = { swf.sw.filter.not ; this }
}

@JSExportTopLevel(name="EasySparqlEngine")
class SWFacade(var config: StatementConfiguration) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  implicit def string2Uri(uri: String) = URI(uri)
  implicit def string2Iri(uri: String) = IRI(uri)

  var sw = SW(config)

  @JSExport
  val filter = new FilterIncrement(this)

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
  def isA( uri : URI ) : SWFacade = { sw.isA(uri); this }

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
  def variable(reference: String) : String = sw.variable(reference)

  @JSExport
  def select(lRef: String*): Promise[Dynamic] = { sw.select(lRef).map(x => scala.scalajs.js.JSON.parse(x.toString())).toJSPromise }

  @JSExport
  def count(): Promise[Int] = { sw.count().toJSPromise }

  @JSExport
  def findClasses(uri:URI = URI("")): Promise[js.Array[URI]] = { sw.findClasses(uri).map(array => array.toJSArray).toJSPromise }

  @JSExport
  def findProperties(motherClassProperties: URI = URI("") ) : Promise[js.Array[URI]] = {
    sw.findProperties(motherClassProperties).map(array => array.toJSArray).toJSPromise
  }

  @JSExport
  def findObjectProperties(motherClassProperties: URI = URI("") ) : Promise[js.Array[URI]] = {
    sw.findObjectProperties(motherClassProperties).map(array => array.toJSArray).toJSPromise
  }
  @JSExport
  def findDatatypeProperties(motherClassProperties: URI = URI("") ) : Promise[js.Array[URI]] = {
    sw.findDatatypeProperties(motherClassProperties).map(array => array.toJSArray).toJSPromise
  }
}
