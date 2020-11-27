package facades.materialize

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

/**
 *
 * Materialize Modal simple facade
 *
 */

@js.native
trait ModalElement extends js.Object {
  def open() : Unit = js.native
  def close() : Unit = js.native
}

@js.native
@JSGlobal("M.Modal")
object Modal extends js.Object{

  def init(elements: js.Any, options: js.Any): ModalElement = js.native

  def getInstance(element: js.Any): ModalElement = js.native

}