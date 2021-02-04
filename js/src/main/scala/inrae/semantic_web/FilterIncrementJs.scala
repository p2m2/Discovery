package inrae.semantic_web

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("SWFilterIncrement")
case class FilterIncrementJs(swf: SWDiscoveryJs) {
  var negation = false

  @JSExport
  def isLiteral: SWDiscoveryJs = {
    swf.sw.filter.isLiteral; swf
  }

  @JSExport
  def isUri: SWDiscoveryJs = {
    swf.sw.filter.isUri; swf
  }

  @JSExport
  def isBlank: SWDiscoveryJs = {
    swf.sw.filter.isBlank; swf
  }

  @JSExport
  def contains(l: String): SWDiscoveryJs = {
    swf.sw.filter.contains(l); swf
  }

  @JSExport
  def not: FilterIncrementJs = {
    swf.sw.filter.not; this
  }
}
