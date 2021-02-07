package facade.npm

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("showdown", JSImport.Default)
object Showdown extends js.Object {
  @js.native
  class Converter(options : js.Dynamic) extends js.Object {
    def makeHtml( text : String  ): String = js.native
  }

  def Converter() : Converter = js.native
  def setOption(key : js.Any,value : js.Any) : js.Any = js.native
  def getOption(key : js.Any ) : js.Any = js.native

}
