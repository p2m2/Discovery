package inrae.semantic_web

import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent, Publisher, Subscriber}
import inrae.semantic_web.internal.{DatatypeNode, Projection, SubjectOf, pm}
import inrae.semantic_web.rdf.{QueryVariable, SparqlDefinition, URI}
import inrae.semantic_web.sparql.QueryResult
import inrae.semantic_web.strategy._
import upickle.default.{macroRW, ReadWriter => RW}
import wvlet.log.Logger.rootLogger.{debug, trace}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}


object SWTransaction {
  implicit val rw: RW[SWTransaction] = macroRW
}

case class SWTransaction(sw : SWDiscovery)
    extends Subscriber[DiscoveryRequestEvent,StrategyRequest]
{
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def notify(pub: StrategyRequest, event: DiscoveryRequestEvent) = {
    notify(event)
  }

  val _prom_raw: Promise[ujson.Value] = Promise[ujson.Value]()
  val raw: Future[ujson.Value] = _prom_raw.future
  var currentRequestEvent: String = DiscoveryStateRequestEvent.START.toString()

  val lSelectedVariable : Seq[QueryVariable] = sw.rootNode.getChild(Projection(List(),"")).lastOption match {
    case Some(proj) => proj.list.distinct
    case None => throw SWDiscoveryException("Projection node (selected variables) is not defined.")
  }

  val lDatatype: Seq[DatatypeNode] =
    sw.rootNode.getChild[DatatypeNode](DatatypeNode("",SubjectOf("",URI("")),"unk"))
      .filter(ld => lSelectedVariable.map(_.name).contains(ld.property.reference()))

  if ( lDatatype.filter( datatypeNode => lSelectedVariable.map(_.name).contains(datatypeNode.refNode) ).length != lDatatype.length )
    throw SWDiscoveryException("Select variable with his datatype ["+lDatatype.map( d=>d.idRef + "->"+d.refNode).mkString(" ,")+"]")


  private var countEvent: Int = 1

  private var _progressionCallBack = Seq[Double => Unit]()

  def progression(  callBack  : Double => Unit  ): Unit = {
    _progressionCallBack = _progressionCallBack :+ callBack
  }

  private var _requestEventCallBack = Seq[String => Unit]()

  def requestEvent(callBack  : String => Unit  ): Unit = {
    _requestEventCallBack = _requestEventCallBack :+ callBack
  }

  def notify(event: DiscoveryRequestEvent): Unit = {
    currentRequestEvent = event.state.toString()
    countEvent = countEvent + 1

    _progressionCallBack.foreach (f => f(DiscoveryStateRequestEvent.getPercentProgression(event.state)))

    _requestEventCallBack.foreach(f => f(currentRequestEvent))
  }

  def abort(): Unit = {

    /*
      http request should be cancelled
    * SWResults should be a publish[DiscoveryCancelEvent] => SW/QueryManager/HttpRequest => Subscriber[DiscoveryCancelEvent,SWResults]
    */
    currentRequestEvent = DiscoveryStateRequestEvent.ABORTED_BY_THE_USER.toString()

    _prom_raw failure(SWDiscoveryException("aborted by the user."))
  }


  def process_datatypes(qr : QueryResult,
                        datatypeNode : DatatypeNode,
                        lUris : Seq[SparqlDefinition]) = {
    debug(" -- process_datatypes --")
    val labelProperty = datatypeNode.property.reference()

    lUris.grouped(sw.config.conf.settings.sizeBatchProcessing).toList.map(
      f = lSubUris => {
        trace(" datatypes:" + lSubUris.toString)
        /* request using api */
        SWDiscovery(sw.config)
          .something("val_uri")
          .setList(lSubUris.flatMap(
            _ match {
              case uri: URI => Some(uri)
              case _ => None
            }
          ))
          .focusManagement(datatypeNode.property, false)
          .select(List("val_uri", labelProperty))
          .commit()
          .raw
          .map(json => {
            qr.setDatatype(labelProperty, json("results")("bindings").arr.map(rec => {
              rec("val_uri")("value").value.toString -> rec(labelProperty)
            }).toMap)
          })
      })
  }

  def commit() : SWTransaction = {
    notify(DiscoveryRequestEvent(DiscoveryStateRequestEvent.START))

    Try(StrategyRequestBuilder.build(sw.config)) match {
      case Failure(e) => _prom_raw failure (e)
      case Success(driver) => {
        driver.subscribe(this.asInstanceOf[Subscriber[DiscoveryRequestEvent,Publisher[DiscoveryRequestEvent]]])
          driver.execute(this)
          /* manage datatype decoration */
          .map((qr: QueryResult) => {
            notify(DiscoveryRequestEvent(DiscoveryStateRequestEvent.DATATYPE_BUILD))
            /* create an empty set of datatypes */
            qr.json("results").update("datatypes", ujson.Obj())
            trace(qr.json)
            /* manage datatype */
            trace("  lDatatype ====> " + lDatatype.toString())

            Future.sequence(lDatatype.map(datatypeNode => {
              trace("datatype node:" + datatypeNode)

              sw.rootNode.getRdfNode(datatypeNode.refNode) match {
                case Some(_) => {

                  /* find uris value inside results to decorate */
                  val lUris: Seq[SparqlDefinition] =
                    try {
                      qr.getValues(datatypeNode.refNode)
                    } catch {
                      case _: Throwable => {
                        List()
                      }
                    }
                  Future.sequence(process_datatypes(qr, datatypeNode, lUris))
                }
                case None => {
                  Future {}
                }
              }
            })) onComplete {
              case Success(_) => {
                notify(DiscoveryRequestEvent(DiscoveryStateRequestEvent.DATATYPE_DONE))
                _prom_raw success qr.json
                notify(DiscoveryRequestEvent(DiscoveryStateRequestEvent.REQUEST_DONE))
              }
              case Failure(e) => {
                _prom_raw failure (e)
              }
            }
          }).recover(exception => {
          _prom_raw failure (exception)
        })
      }
    }
    this
  }
}
