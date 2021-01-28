package inrae.semantic_web.driver

import inrae.semantic_web.SparqlQueryBuilder
import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent, Publisher}
import inrae.semantic_web.internal.{Root, pm}
import inrae.semantic_web.rdf.SparqlBuilder
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
        requestOnSWDB(query).map(resultsQR => {
          RequestDriver.getQrm(this).set(query, resultsQR.results)
          publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.RESULTS_DONE))
          resultsQR
        })
      }

    }
  }

  def countNbSolutions(root : Root) : Future[Int] = {
      val (refToIdentifier, _) = pm.SparqlGenerator.correspondenceVariablesIdentifier(root)
      val varCount = "count"
      publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.QUERY_BUILD))
      val query = SparqlQueryBuilder.countQueryString(root,refToIdentifier,varCount)
      val res: Future[QueryResult] = request(query)
      res.map(v => {
        publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.RESULTS_BUILD))
        SparqlBuilder.createLiteral(v.json("results")("bindings")(0)(varCount)).toInt()
      })
    }

  protected def requestOnSWDB(query: String): Future[QueryResult]
}
