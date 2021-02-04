package inrae.semantic_web


import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("ProjectionExpressionIncrement")
case class ProjectionExpressionIncrementJs(swf: SWTransactionJs,`var` : String) {
  @JSExport
  def count(distinct: Boolean=false) : SWTransactionJs = SWTransactionJs(swf.transaction.aggregate(`var`).count(distinct))

  @JSExport
  def countAll(distinct: Boolean=false) : SWTransactionJs = SWTransactionJs(swf.transaction.aggregate(`var`).countAll(distinct))
}
