package inrae.semantic_web

import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent, Publisher, Subscriber}
import inrae.semantic_web.internal.{DatatypeNode, pm}
import inrae.semantic_web.rdf.{SparqlDefinition, URI}
import inrae.semantic_web.sparql.QueryResult
import inrae.semantic_web.strategy._
import wvlet.log.Logger.rootLogger.{debug, trace}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}


case class SWTransaction(sw : SWDiscovery, lRef: Seq[String] = List(), limit : Int = 0, offset : Int = 0)
    extends Subscriber[DiscoveryRequestEvent,StrategyRequest]
{
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def notify(pub: StrategyRequest, event: DiscoveryRequestEvent) = {
    notify(event)
  }

  val _prom_raw: Promise[ujson.Value] = Promise[ujson.Value]()
  val raw: Future[ujson.Value] = _prom_raw.future
  var currentRequestEvent: String = DiscoveryStateRequestEvent.START.toString()

  val mapId2Var: Map[String, String] =  pm.SparqlGenerator.correspondenceVariablesIdentifier(sw.rootNode)._1

  val lDatatype: Seq[DatatypeNode] = sw.rootNode.lDatatypeNode.filter(ld => lRef.contains(ld.property.reference()))
  trace("list datatype : "+lDatatype.toString)

  val lSelectVariables: Seq[String] = {
    /* select uri type ask with decoration/datatype */
    lDatatype.map(ld => {
      mapId2Var(ld.refNode)
    }) ++ {
      /* select user ask variable */
      lRef match {
        case v if v.length > 0 => v.flatMap(ref => variable(ref))
        case _ => sw.rootNode.referencesChildren().flatMap(ref => variable(ref))
      }
    }
  }.distinct

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

  private def variable(reference: String) : Option[String] = {
    debug(" -- variable -- ")
    val variableNameList = pm.SelectNode.getNodeWithRef(reference, sw.rootNode)
      .map( v => {
        pm.SparqlGenerator.correspondenceVariablesIdentifier(sw.rootNode)._1.getOrElse(reference,"")
      })

    if (variableNameList.filter(_ != "").length==0) {
      None
    } else {
      Some(variableNameList(0))
    }
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
                      qr.getValues(mapId2Var(datatypeNode.refNode))
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
                qr.v2Ident(mapId2Var)
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
