//package inrae.semantic_web.sparql
/*
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


case class QueryRunner(source: ConfigurationObject.Source,settings : GeneralSetting)
  extends Subscriber[DiscoveryRequestEvent,HttpRequestDriver]
    with Publisher[DiscoveryRequestEvent] {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def notify (pub: HttpRequestDriver, event: DiscoveryRequestEvent) : Unit = {
    publish(event)
  }


  def query(queryStr: String): Future[QueryResult] = {
    publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.RESULTS_BUILD))

    /* memorize */
    QueryRunner.getQrm(source.url).get(queryStr) match {
      case Some(resultsString) => Future {
        publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.RESULTS_DONE))
        QueryResult(resultsString)
      }

      case None => {
        /* request */
        val httpDriver = settings.getRequestDriver
        /* subscribe */
     //   httpDriver.subscribe(this.asInstanceOf[Subscriber[DiscoveryRequestEvent,Publisher[DiscoveryRequestEvent]]])
        /* request */
       /* httpDriver.request(source.method, queryStr,
          ConfigurationHttpRequest(
            url = source.url,
            login = source.login,
            password = source.password,
            token = source.token,
            auth = source.auth)).map(resultsQR => {
          if (settings.cache)
            QueryRunner.getQrm(source.url).set(queryStr, resultsQR.results)
          publish(DiscoveryRequestEvent(DiscoveryStateRequestEvent.RESULTS_DONE))
          resultsQR
        })*/
        Future{ QueryResult("") }
      }
    }
  }

}
*/
