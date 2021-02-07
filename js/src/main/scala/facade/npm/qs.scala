package facade.npm

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("qs", JSImport.Default)
object qs extends js.Object {
  def stringify(data: js.Dictionary[String]): String = js.native
}
