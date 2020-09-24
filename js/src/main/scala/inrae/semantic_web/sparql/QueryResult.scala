package inrae.semantic_web.sparql

case class QueryResult(executor: Any, query: Any) {
  def print(): Unit = {
    None: Option[String]
  }

  def asJson(): Option[String] = {
    None: Option[String]
  }

  def asJsonOrError(): String = {
    ""
  }

  def asXML(): Option[String] = {
    None: Option[String]
  }

  def asXMLOrError(): String = {
    ""
  }

  def asCSV(): Option[String] = {
    None: Option[String]
  }

  def asCSVOrError(): String = {
    ""
  }

  def asTextOrError(): String = {
    ""
  }
}
