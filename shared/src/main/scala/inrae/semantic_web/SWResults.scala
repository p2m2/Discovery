package inrae.semantic_web

import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent, Subscriber}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

case class SWResults(_prom_raw: Promise[ujson.Value]) extends Subscriber[DiscoveryRequestEvent, SW] {

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

  def notify(pub: SW, event: DiscoveryRequestEvent): Unit = {

    currentRequestEvent = event.state.toString()
    countEvent = countEvent + 1

    val p = countEvent.toDouble / DiscoveryStateRequestEvent.nValidStep.toDouble
    _progressionCallBack.foreach (f => f(p))

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

}
