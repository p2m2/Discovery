package inrae.semantic_web

import scala.concurrent.Future
import scala.scalajs.js.annotation.JSExportTopLevel

/**
 * Useful to request on triplestore only if user use theses solutions .
 * @param wrp
 */
@JSExportTopLevel(name="LazyFutureJsonValue")
case class LazyFutureSwResults(wrp : () => SWTransaction) {
  lazy val wrapped = wrp()
}
