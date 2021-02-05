package inrae.semantic_web


import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("ProjectionExpressionIncrement")
case class ProjectionExpressionIncrementJs(swf: SWTransactionJs,`var` : String) {
  @JSExport
  def count(ref: String, distinct: Boolean=false) : SWTransactionJs = SWTransactionJs(swf.transaction.aggregate(`var`).count(ref,distinct))

  @JSExport
  def countAll(distinct: Boolean=false) : SWTransactionJs = SWTransactionJs(swf.transaction.aggregate(`var`).countAll(distinct))
}
