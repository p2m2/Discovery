package inrae.semantic_web.sparql

import inrae.semantic_web.ConfigurationObject
import inrae.semantic_web.ConfigurationObject.GeneralSetting
import inrae.semantic_web.sparql.{QueryResult, QueryResultManager}

import scala.concurrent.Future

final case class QueryRunnerException(private val message: String = "",
                                            private val cause: Throwable = None.orNull) extends Exception(message,cause)

object QueryRunner {
  var qrmBySource = Map[String,QueryResultManager]()

  def getQrm(url: String): QueryResultManager = {
    qrmBySource.get(url) match {
      case Some(qrm) => qrm
      case None => {
        qrmBySource += url -> QueryResultManager()
        qrmBySource(url)
      }
    }
  }
}


case class QueryRunner(source: ConfigurationObject.Source,settings : GeneralSetting)  {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def query(queryStr: String): Future[QueryResult] = {

    /* memorize */
    QueryRunner.getQrm(source.url).get(queryStr) match {
      case Some(resultsString) => Future {
        QueryResult(resultsString)
      }

      case None => {
        /* request */
        settings.getHttpDriver().request(source.method, queryStr,
          ConfigurationHttpRequest(
            url = source.url,
            login = source.login,
            password = source.password,
            token = source.token,
            auth = source.auth)).map(resultsQR => {
          if (settings.cache)
            QueryRunner.getQrm(source.url).set(queryStr, resultsQR.results)
          resultsQR
        })
      }
    }
  }

}
