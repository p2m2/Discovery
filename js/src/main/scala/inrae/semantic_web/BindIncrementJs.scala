package inrae.semantic_web

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("BindIncrement")
case class BindIncrementJs(swf: SWDiscoveryJs,`var` : String) {
  var negation = false

  @JSExport
  def subStr(startingLoc : Int,length : Int ) :  SWDiscoveryJs = SWDiscoveryJs(swf.config,swf.sw.bind(`var`).subStr(startingLoc,length))


}
