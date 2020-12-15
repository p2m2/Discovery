package inrae.semantic_web.sparql

/**
 * Simple manager to memorize results
 */

case class QueryResultManager() {
  var mapQueryResult = Map[String,String]()

  private def encode(value : String ) : String = {
    value
  }

  private def decode(hash : String ) : String = {
    hash
  }

  def set(queryString : String ,results : String ) = {
    mapQueryResult += encode(queryString) -> encode(results)
  }

  def remove(queryString : String) = {
    mapQueryResult = mapQueryResult.-(queryString)
  }

  def get(queryString : String) : Option[String] = mapQueryResult.get(queryString) match {
    case Some(hash) => Some(decode(hash))
    case None => None
  }
}
