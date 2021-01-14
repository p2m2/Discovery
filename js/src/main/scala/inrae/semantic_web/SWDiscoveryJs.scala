package inrae.semantic_web

import inrae.semantic_web.internal.Node
import inrae.semantic_web.rdf.{IRI, SparqlDefinition, URI}

import scala.scalajs._
import scala.scalajs.js.JSConverters._
import scala.scalajs.js._
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}



@JSExportTopLevel(name="SWDiscovery")
class SWDiscoveryJs(var config: StatementConfiguration) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  var sw = SWDiscovery(config)

  @JSExport
  val filter = new FilterIncrementJs(this)

  @JSExport
  def help() : SWDiscoveryJs = { sw.help() ; this }

  @JSExport
  def focus(ref : String) : SWDiscoveryJs = { sw.focus(ref) ; this }

  @JSExport
  def focusManagement(n : Node) : SWDiscoveryJs = { sw.focusManagement(n) ; this }

  @JSExport
  def prefix(short : String, long : IRI ) : SWDiscoveryJs = { sw.prefix(short,long) ; this }

  @JSExport
  def graph(graph : IRI) : SWDiscoveryJs = { sw.graph(graph) ; this }

  @JSExport
  def namedGraph(graph : IRI ) : SWDiscoveryJs = { sw.namedGraph(graph) ; this }
  /* start a request */
  @JSExport
  def something( ref : String = sw.getUniqueRef() ) : SWDiscoveryJs = { sw.something(ref) ; this }

  /* create node which focus is the subject : ?focusId <uri> ?target */
  @JSExport
  def isSubjectOf( uri : URI , ref : String = sw.getUniqueRef() ) : SWDiscoveryJs = { sw.isSubjectOf(uri,ref) ; this }

  /* create node which focus is the subject : ?focusId <uri> ?target */
  @JSExport
  def isObjectOf( uri : URI , ref : String = sw.getUniqueRef() ) : SWDiscoveryJs = { sw.isObjectOf(uri,ref); this }

  @JSExport
  def isLinkTo( uri : URI , ref : String = sw.getUniqueRef() ) : SWDiscoveryJs = { sw.isLinkTo(uri,ref); this }

  @JSExport
  def isA( term : SparqlDefinition ) : SWDiscoveryJs = { sw.isA(term); this }

  @JSExport
  def isLinkFrom( uri : URI , ref : String = sw.getUniqueRef() ) : SWDiscoveryJs = { sw.isLinkFrom(uri,ref); this }
  /* set */
  @JSExport
  def set( uri : URI ) : SWDiscoveryJs = { sw.set(uri) ; this }

  @JSExport
  def datatype( uri : URI, ref : String ) : SWDiscoveryJs = { sw.datatype(uri,ref) ; this }

  @JSExport
  def debug() : SWDiscoveryJs = { sw.console() ; this  }

  @JSExport
  def sparql() : String = { sw.sparql() }

  @JSExport
  def select(lRef: String*): SWTransactionJs = { SWTransactionJs(sw.select(lRef)) }

  @JSExport
  def select(lRef: Seq[String], limit : Int = 0, offset : Int = 0): SWTransactionJs = {
    SWTransactionJs(sw.select(lRef,limit,offset))
  }

  @JSExport
  def selectByPage(lRef: String*)  : Promise[(Int,js.Array[SWTransactionJs])] = {
    sw.count().map(
      nsolutions => {
        val nit : Int = nsolutions / config.conf.settings.pageSize
        (nit+1,(0 to nit).map( p =>{
          val limit = config.conf.settings.pageSize
          val offset = p*config.conf.settings.pageSize
          select(lRef,limit,offset)
        }).toJSArray)
      }).toJSPromise
  }

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
