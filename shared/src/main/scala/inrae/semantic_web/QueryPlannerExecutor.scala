package inrae.semantic_web
import inrae.semantic_web.QueryPlanner.ORDONNANCEMENT_RESULTS_SET
import inrae.semantic_web.sparql.{QueryResult, _}

import scala.concurrent.Future

object QueryPlannerExecutor {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def executePlanning( rs : ORDONNANCEMENT_RESULTS_SET,
                       listVariables : Seq[String],
                       config : StatementConfiguration ): Future[QueryResult] = {
    Future {
      QueryResult(null)
    }
  }
}
