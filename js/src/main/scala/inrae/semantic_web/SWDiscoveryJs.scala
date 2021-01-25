package inrae.semantic_web

import inrae.semantic_web.internal.Node
import inrae.semantic_web.rdf.{IRI, SparqlDefinition, URI}

import scala.scalajs._
import scala.scalajs.js.JSConverters._
import scala.scalajs.js._
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}



@JSExportTopLevel(name="SWDiscovery")
case class SWDiscoveryJs(config: StatementConfiguration=StatementConfiguration(),sw: SWDiscovery = SWDiscovery()) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  //val sw: SWDiscovery = SWDiscovery(config)

  @JSExport
  val filter = new FilterIncrementJs(this)

  @JSExport
  def help() : SWDiscoveryJs = SWDiscoveryJs(config,SWDiscovery(config).help())

  @JSExport
  def focus(ref : String) : SWDiscoveryJs = SWDiscoveryJs(config,sw.focus(ref))

  @JSExport
  def focusManagement(n : Node) : SWDiscoveryJs = SWDiscoveryJs(config,sw.focusManagement(n))

  @JSExport
  def prefix(short : String, long : IRI ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.prefix(short,long))

  @JSExport
  def graph(graph : IRI) : SWDiscoveryJs = SWDiscoveryJs(config,sw.graph(graph))

  @JSExport
  def namedGraph(graph : IRI ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.namedGraph(graph))
  /* start a request */
  @JSExport
  def something( ref : String = sw.getUniqueRef ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.something(ref))

  /* create node which focus is the subject : ?focusId <uri> ?target */
  @JSExport
  def isSubjectOf( uri : URI , ref : String = sw.getUniqueRef ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.isSubjectOf(uri,ref))

  /* create node which focus is the subject : ?focusId <uri> ?target */
  @JSExport
  def isObjectOf( uri : URI , ref : String = sw.getUniqueRef ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.isObjectOf(uri,ref))

  @JSExport
  def isLinkTo( uri : URI , ref : String = sw.getUniqueRef ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.isLinkTo(uri,ref))

  @JSExport
  def isA( term : SparqlDefinition ) : SWDiscoveryJs = { sw.isA(term); this }

  @JSExport
  def isLinkFrom( uri : URI , ref : String = sw.getUniqueRef ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.isLinkFrom(uri,ref))
  /* set */
  @JSExport
  def set( uri : URI ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.set(uri))

  @JSExport
  def datatype( uri : URI, ref : String ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.datatype(uri,ref))

  @JSExport
  def debug() : SWDiscoveryJs = SWDiscoveryJs(config,sw.console())

  @JSExport
  def sparql() : String = sw.sparql()

  @JSExport
  def getSerializedQuery: String = sw.getSerializedQuery

  @JSExport
  def setSerializedQuery(query : String): SWDiscoveryJs = SWDiscoveryJs(config,sw.setSerializedQuery(query))

  @JSExport
  def select(lRef: String*): SWTransactionJs = SWTransactionJs(sw.select(lRef))

  @JSExport
  def select(lRef: Seq[String], limit : Int = 0, offset : Int = 0): SWTransactionJs =
    SWTransactionJs(sw.select(lRef,limit,offset))


  @JSExport
  def selectByPage(lRef: String*)  : Promise[(Int,js.Array[SWTransactionJs])] = {
    sw.count().map(
      nSolutions => {
        val nit : Int = nSolutions / config.conf.settings.pageSize
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
