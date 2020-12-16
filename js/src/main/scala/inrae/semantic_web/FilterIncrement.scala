package inrae.semantic_web

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("FilterIncrement")
class FilterIncrement(swf: EasySparqlEngine) {
  var negation = false

  @JSExport
  def isLiteral: EasySparqlEngine = {
    swf.sw.filter.isLiteral; swf
  }

  @JSExport
  def isUri: EasySparqlEngine = {
    swf.sw.filter.isUri; swf
  }

  @JSExport
  def isBlank: EasySparqlEngine = {
    swf.sw.filter.isBlank; swf
  }

  @JSExport
  def contains(l: String): EasySparqlEngine = {
    swf.sw.filter.contains(l); swf
  }

  @JSExport
  def not: FilterIncrement = {
    swf.sw.filter.not; this
  }
}
