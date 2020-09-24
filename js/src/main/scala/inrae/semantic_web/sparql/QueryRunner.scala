package inrae.semantic_web.sparql

case class QueryRunner(service: String) {
  def query(queryStr: String): QueryResult = {
    //val r: String = "nothing"
    QueryResult("","")
  }
}
