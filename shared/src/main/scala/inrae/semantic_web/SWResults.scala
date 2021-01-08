package inrae.semantic_web

import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent, Subscriber}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

case class SWResults(_prom_raw: Promise[ujson.Value]) extends Subscriber[DiscoveryRequestEvent, SW] {

  val raw: Future[ujson.Value] = _prom_raw.future
  var requestEvent: String = DiscoveryStateRequestEvent.START.toString()
  var progression: Double = 0.0

  var countEvent: Int = 1

  def notify(pub: SW, event: DiscoveryRequestEvent): Unit = {
    requestEvent = event.state.toString()
    countEvent = countEvent + 1
    progression = countEvent.toDouble / DiscoveryStateRequestEvent.nValidStep.toDouble
  }

  def abort(): Unit = {

    /* http request should be cancelled */

    requestEvent = DiscoveryStateRequestEvent.ABORTED_BY_THE_USER.toString()

    _prom_raw failure(DiscoveryException("aborted by the user."))
  }

}
