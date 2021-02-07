package inrae.semantic_web

import inrae.semantic_web.internal.Node
import inrae.semantic_web.rdf.{IRI, SparqlDefinition, URI}
import inrae.semantic_web.view.HtmlView

import scala.scalajs._
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}



@JSExportTopLevel(name="SWDiscovery")
case class SWDiscoveryJs(
                          config: StatementConfiguration=StatementConfiguration(),
                          swArg: SWDiscovery = null
                        ) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val sw: SWDiscovery = swArg match {
    case null => SWDiscovery(config)
    case v =>v
  }

  def toIRI(any:Any) : IRI = any match {
    case v : IRI => v
    case v : URI => IRI(v.sparql)
    case string : String => string
    case _ => throw SWDiscoveryException(any.toString + " can not be cast into IRI.")
  }

  def toURI(any:Any): URI = any match {
    case v : URI => v
    case string : String => string
    case _ => throw SWDiscoveryException(any.toString + " can not be cast into IRI.")
  }

  @JSExport
  val filter: FilterIncrementJs = FilterIncrementJs(this)

  @JSExport
  def helper(regex : String = ""): SWDiscoveryJs = { HtmlView(sw,regex) ; SWDiscoveryJs(config,sw) }

  @JSExport
  def bind(`var` : String) : BindIncrementJs = BindIncrementJs(this,`var`)

  @JSExport
  def usage() : SWDiscoveryJs = SWDiscoveryJs(config,SWDiscovery(config).usage)

  @JSExport
  def finder :SWDiscoveryHelperJs = SWDiscoveryHelperJs(sw)

  @JSExport
  def focus(ref : String) : SWDiscoveryJs = SWDiscoveryJs(config,sw.focus(ref))

  @JSExport
  def focusManagement(n : Node) : SWDiscoveryJs = SWDiscoveryJs(config,sw.focusManagement(n))

  @JSExport
  def prefix(short : String, long : Any ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.prefix(short,toIRI(long)))

  @JSExport
  def graph(graph : Any) : SWDiscoveryJs = SWDiscoveryJs(config,sw.graph(toIRI(graph)))

  @JSExport
  def root(): SWDiscoveryJs = SWDiscoveryJs(config,sw.root)

  @JSExport
  def focus() : String = sw.focusNode

  @JSExport
  def namedGraph(graph : Any ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.namedGraph(toIRI(graph)))
  /* start a request */
  @JSExport
  def something( ref : String = sw.getUniqueRef("something") ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.something(ref))

  /* create node which focus is the subject : ?focusId <uri> ?target */
  @JSExport
  def isSubjectOf( uri : Any , ref : String = sw.getUniqueRef("object") ) : SWDiscoveryJs =
    SWDiscoveryJs(config,sw.isSubjectOf(uri,ref))

  /* create node which focus is the subject : ?focusId <uri> ?target */
  @JSExport
  def isObjectOf( uri : Any , ref : String = sw.getUniqueRef("subject") ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.isObjectOf(uri,ref))

  @JSExport
  def isLinkTo( uri : Any , ref : String = sw.getUniqueRef("linkTo") ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.isLinkTo(uri,ref))

  @JSExport
  def isA( term : Any ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.isA(term))

  @JSExport
  def isLinkFrom( uri : String , ref : String = sw.getUniqueRef("linkFrom") ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.isLinkFrom(uri,ref))
  /* set */
  @JSExport
  def set( term : Any ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.set(term))

  @JSExport
  def setList( terms : Any* ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.setList(terms.map(SparqlDefinition.fromAny)))

  @JSExport
  def datatype( uri : Any, ref : String ) : SWDiscoveryJs = SWDiscoveryJs(config,sw.datatype(toURI(uri),ref))

  @JSExport
  def console() : SWDiscoveryJs = SWDiscoveryJs(config,sw.console)

  @JSExport
  def sparql() : String = sw.sparql

  @JSExport
  def getSerializedString: String = sw.getSerializedString

  @JSExport
  def setSerializedString(query : String): SWDiscoveryJs = SWDiscoveryJs(config,sw.setSerializedString(query))

  @JSExport
  def select(lRef: String*): SWTransactionJs = SWTransactionJs(sw.select(lRef))

  @JSExport
  def select(lRef: Seq[String], limit : Int = 0, offset : Int = 0): SWTransactionJs =
    SWTransactionJs(sw.select(lRef,limit,offset))


  @JSExport
  def selectByPage(lRef: String*)  : js.Promise[(Int,js.Array[SWTransactionJs])] = {
    sw.finder.count.map(
      nSolutions => {
        val nit : Int = nSolutions / config.conf.settings.pageSize
        (nit+1,(0 to nit).map( p =>{
          val limit = config.conf.settings.pageSize
          val offset = p*config.conf.settings.pageSize
          select(lRef,limit,offset)
        }).toJSArray)
      }).toJSPromise
  }
}
