package inrae.semantic_web

import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent, Publisher, Subscriber}
import inrae.semantic_web.internal.Node.references
import inrae.semantic_web.internal.pm
import inrae.semantic_web.rdf.SparqlDefinition
import inrae.semantic_web.sparql.QueryResult
import wvlet.log.Logger.rootLogger.{debug, trace}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

case class SWTransaction(sw : SW,lRef: Seq[String] = List(), limit : Int = 0, offset : Int = 0)
    extends Subscriber[DiscoveryRequestEvent,QueryManager]
{
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def notify (pub: QueryManager, event: DiscoveryRequestEvent) : Unit = {
    notify(event)
  }

  val _prom_raw: Promise[ujson.Value] = Promise[ujson.Value]()
  val raw: Future[ujson.Value] = _prom_raw.future
  var currentRequestEvent: String = DiscoveryStateRequestEvent.START.toString()

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

    _prom_raw failure(DiscoveryException("aborted by the user."))
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

  def commit() : SWTransaction = {
    notify(DiscoveryRequestEvent(DiscoveryStateRequestEvent.START))

    val mapId2Var =  pm.SparqlGenerator.correspondenceVariablesIdentifier(sw.rootNode)._1

    trace("Mapping variable <-> references :\n" + mapId2Var.toString().split(",").mkString("\n"))

    val lDatatype = sw.rootNode.lDatatypeNode.filter(ld => lRef.contains(ld.property.reference()))
    trace("list datatype : "+lDatatype.toString)

    val lSelectVariables = {
      /* select uri type ask with decoration/datatype */
      lDatatype.map(ld => {
        mapId2Var(ld.refNode)
      }) ++ {
        /* select user ask variable */
        lRef match {
          case v if v.length > 0 => v.flatMap(ref => variable(ref))
          case _ => references(sw.focusNode).flatMap(ref => variable(ref))
        }
      }
    }.distinct

    trace("lSelectVariables :::" + lSelectVariables.toString())

    /* manage variable name */
    val qm = QueryManager(sw.config)
    qm.subscribe(this.asInstanceOf[Subscriber[DiscoveryRequestEvent,Publisher[DiscoveryRequestEvent]]])
    qm.queryVariables(sw.rootNode,lSelectVariables,limit,offset)
      /* manage datatype decoration */
      .map( (qr : QueryResult) => {
        notify(DiscoveryRequestEvent(DiscoveryStateRequestEvent.DATATYPE_BUILD))
        /* create an empty set of datatypes */
        qr.json("results").update("datatypes",ujson.Obj())
        trace(qr.json)
        /* manage datatype */
        trace("  lDatatype ====> " + lDatatype.toString())

        Future.sequence(lDatatype.map(datatypeNode => {
          trace("datatype node:"+datatypeNode)

          sw.rootNode.getRdfNode(datatypeNode.refNode) match {
            case Some(_) => {

              /* find uris value inside results to decorate */
              val lUris : Seq[SparqlDefinition] =
                try {
                  qr.getValues(mapId2Var(datatypeNode.refNode))
                } catch {
                  case _ : Throwable => {
                    List()
                  }
                }
              Future.sequence(QueryManager(sw.config).process_datatypes(qr,datatypeNode,lUris))
            }
            case None => {
              Future { }
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
            _prom_raw failure(e)
          }
        }
      }).recover( exception => {
      _prom_raw failure(exception)
    })
    this
  }

}
