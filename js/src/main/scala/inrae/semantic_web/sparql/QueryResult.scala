package inrae.semantic_web.sparql

import inrae.http.ResultSet

case class QueryResult(results : ResultSet) {
  def print(): Unit = {
    None: Option[String]
  }

  def get(): ResultsFormat = {

    ResultsFormat()
  }

  def getString() : String = {
    "---"
  }
}
