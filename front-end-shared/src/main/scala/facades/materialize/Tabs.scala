package facades.materialize

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

/**
 *
 * Materialize simple Sidenav facade
 *
 */
@js.native
trait Tab extends js.Object {
  def updateTabIndicator() : Unit = js.native
}

@js.native
@JSGlobal("M.Tabs")
object Tabs extends js.Object{

  def init(elements: js.Any, options: js.Any) : Unit = js.native
  def getInstance(element: js.Any): Tab = js.native

}
