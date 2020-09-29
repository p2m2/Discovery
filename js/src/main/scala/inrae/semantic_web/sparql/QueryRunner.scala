package inrae.semantic_web.sparql

import inrae.semantic_web.ConfigurationObject

case class QueryRunner(source: ConfigurationObject.Source) {
  def query(queryStr: String): QueryResult = {
    //val r: String = "nothing"
    QueryResult("","")
  }
}
