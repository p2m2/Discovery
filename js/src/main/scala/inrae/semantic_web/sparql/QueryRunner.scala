package inrae.semantic_web.sparql

import inrae.semantic_web.StatementConfiguration

case class QueryRunner(service: String, config: StatementConfiguration) {
  def query(queryStr: String): QueryResult = {
    //val r: String = "nothing"
    QueryResult("","")
  }
}
