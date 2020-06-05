package EasySparql

case class QueryRunner(service: String) {
  def query(queryStr: String): QueryResult = {
    val r = new ComunicaNewEngine().query(queryStr)
    println(" ----- RESULTS ----")
    println(r)
    println(" ----- END ----")
    QueryResult("", "")
  }
}