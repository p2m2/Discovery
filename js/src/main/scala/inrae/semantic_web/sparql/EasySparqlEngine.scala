package inrae.semantic_web

import inrae.semantic_web.{SW, StatementConfiguration}
import inrae.semantic_web.internal.{Node, ObjectOf, Something, SubjectOf, Value}
import inrae.semantic_web.rdf.{IRI, SparqlDefinition, URI}
import ujson.IndexedValue.False

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js.JSConverters._
import scala.scalajs.js._
import scala.scalajs._

@JSExportTopLevel("FilterIncrement")
class FilterIncrement(swf: EasySparqlEngine)  {
  var negation = false

  @JSExport
  def isLiteral: EasySparqlEngine = { swf.sw.filter.isLiteral ; swf }

  @JSExport
  def isUri: EasySparqlEngine = { swf.sw.filter.isUri ; swf }

  @JSExport
  def isBlank: EasySparqlEngine = { swf.sw.filter.isBlank ; swf }

  @JSExport
  def contains(l:String): EasySparqlEngine = { swf.sw.filter.contains(l) ; swf }

  @JSExport
  def not : FilterIncrement = { swf.sw.filter.not ; this }
}

@JSExportTopLevel(name="EasySparqlEngine")
class EasySparqlEngine(var config: StatementConfiguration) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  implicit def string2Uri(uri: String) = URI(uri)
  implicit def string2Iri(uri: String) = IRI(uri)

  var sw = SW(config)

  @JSExport
  val filter = new FilterIncrement(this)

  @JSExport
  def help() : EasySparqlEngine = { sw.help() ; this }

  @JSExport
  def focus(ref : String) : EasySparqlEngine = { sw.focus(ref) ; this }

  @JSExport
  def focusManagement(n : Node) : EasySparqlEngine = { sw.focusManagement(n) ; this }

  @JSExport
  def prefix(short : String, long : IRI ) : EasySparqlEngine = { sw.prefix(short,long) ; this }

  @JSExport
  def graph(graph : IRI) : EasySparqlEngine = { sw.graph(graph) ; this }

  @JSExport
  def namedGraph(graph : IRI ) : EasySparqlEngine = { sw.namedGraph(graph) ; this }
  /* start a request */
  @JSExport
  def something( ref : String = sw.getUniqueRef() ) : EasySparqlEngine = { sw.something(ref) ; this }

  /* create node which focus is the subject : ?focusId <uri> ?target */
  @JSExport
  def isSubjectOf( uri : URI , ref : String = sw.getUniqueRef() ) : EasySparqlEngine = { sw.isSubjectOf(uri,ref) ; this }

  /* create node which focus is the subject : ?focusId <uri> ?target */
  @JSExport
  def isObjectOf( uri : URI , ref : String = sw.getUniqueRef() ) : EasySparqlEngine = { sw.isObjectOf(uri,ref); this }

  @JSExport
  def isLinkTo( uri : URI , ref : String = sw.getUniqueRef() ) : EasySparqlEngine = { sw.isLinkTo(uri,ref); this }

  @JSExport
  def isA( term : SparqlDefinition ) : EasySparqlEngine = { sw.isA(term); this }

  @JSExport
  def isLinkFrom( uri : URI , ref : String = sw.getUniqueRef() ) : EasySparqlEngine = { sw.isLinkFrom(uri,ref); this }
  /* set */
  @JSExport
  def set( uri : URI ) : EasySparqlEngine = { sw.set(uri) ; this }

  @JSExport
  def datatype( uri : URI, ref : String ) : EasySparqlEngine = { sw.datatype(uri,ref) ; this }

  @JSExport
  def debug() : EasySparqlEngine = { sw.console() ; this  }

  @JSExport
  def sparql_console() : EasySparqlEngine = { sw.sparql_console() ; this }

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
