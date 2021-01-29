package inrae.semantic_web.driver

import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent, Publisher}
import inrae.semantic_web.sparql.{QueryResult, QueryResultManager}

import scala.concurrent.Future

object RequestDriver {
  var qrmBySource = Map[RequestDriver,QueryResultManager]()

  def getQrm(rd: RequestDriver): QueryResultManager = {
    qrmBySource.get(rd) match {
      case Some(qrm) => qrm
      case None => {
        qrmBySource += rd -> QueryResultManager()
        qrmBySource(rd)
      }
    }
  }
}

trait RequestDriver extends Publisher[DiscoveryRequestEvent] {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def request(query: String): Future[QueryResult] = {
    /* memorize */
    RequestDriver.getQrm(this).get(query) match {
      case Some(resultsString) => Future {
        publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.RESULTS_DONE))
        QueryResult(resultsString)
      }
      case None => {
        publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.START_HTTP_REQUEST))
        requestOnSWDB(query).map(resultsQR => {

          publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.RESULTS_BUILD))
          RequestDriver.getQrm(this).set(query, resultsQR.results)
          publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.RESULTS_DONE))
          resultsQR
        })
      }

    }
  }

  protected def requestOnSWDB(query: String): Future[QueryResult]
}
