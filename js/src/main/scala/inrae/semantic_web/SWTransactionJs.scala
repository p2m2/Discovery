package inrae.semantic_web

import scala.scalajs.js.JSConverters.JSRichFutureNonThenable
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel(name="SWDiscoveryTransaction")
case class SWTransactionJs(transaction : SWTransaction) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  @JSExport
  def progression(  callBack  : Double => Unit  ): Unit = transaction.progression(callBack)

  @JSExport
  def requestEvent(callBack  : String => Unit  ): Unit = transaction.requestEvent(callBack)

  @JSExport
  def abort(): Unit = transaction.abort()

  @JSExport
  def commit() : SWTransactionJs = { transaction.commit() ; this }

  @JSExport
  def raw() : Promise[Dynamic] = {
    transaction.raw.map(x => scala.scalajs.js.JSON.parse(x.toString())).toJSPromise
  }

}
