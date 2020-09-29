package inrae.semantic_web.sparql

import inrae.semantic_web.rdf.RdfType

case class ResultsFormat ()  {

  case class ResultsRow ( values : Map[String,RdfType] ) {
    def get(varname :String) : Option[RdfType] = {
      try {
        Some(values(varname))
      } catch {
        case _  : Throwable => None
      }
    }
  }

  var rows : Seq[ResultsRow] = Seq[ResultsRow]()

  def get(i:Int) : ResultsRow = { rows(i) }

  override def toString(): String = {
    rows.toString
  }

}