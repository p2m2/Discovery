package inrae.semantic_web.sparql
import inrae.semantic_web.StatementConfiguration
import dispatch._, Defaults._

case class QueryRunner(service: String, config: StatementConfiguration) {

  def setServiceUrlRequest() : Unit = {
    println("")
  }

  def query(queryStr: String): QueryResult = {
    //val r: String = "nothing"

    val svc = url("http://api.hostip.info/country.php")
    val country = Http.default(svc OK as.String)

    for (c <- country)
      println(c)


    QueryResult("","")
  }
}
