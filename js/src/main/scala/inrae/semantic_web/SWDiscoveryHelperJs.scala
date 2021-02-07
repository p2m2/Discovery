package inrae.semantic_web

import inrae.semantic_web.rdf.URI

import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js.JSConverters._

@JSExportTopLevel(name="SWDiscoveryHelper")
case class SWDiscoveryHelperJs(sw : SWDiscovery) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  @JSExport
  def count(): Promise[Int] = { sw.helper.count.toJSPromise }

  @JSExport
  def findClasses(regex : String = "",uri:URI = URI(""), page : Int = 0 ): Promise[js.Array[URI]] =
  { sw.helper.findClasses(regex,uri,page).map(array => array.toJSArray).toJSPromise }

  @JSExport
  def findObjectProperties(regex : String = "",motherClassProperties: URI = URI(""), page : Int = 0 ) : Promise[js.Array[URI]] = {
    sw.helper.findObjectProperties(regex,motherClassProperties,page).map(array => array.toJSArray).toJSPromise
  }

  @JSExport
  def findDatatypeProperties(regex : String = "",motherClassProperties: URI = URI("") , page : Int = 0) : Promise[js.Array[URI]] = {
    sw.helper.findDatatypeProperties(regex,motherClassProperties,page).map(array => array.toJSArray).toJSPromise
  }

  @JSExport
  def findSubjectProperties(regex : String = "",motherClassProperties: URI = URI("") , page : Int = 0) : Promise[js.Array[URI]] = {
    sw.helper.findSubjectProperties(regex,motherClassProperties,page).map(array => array.toJSArray).toJSPromise
  }

}
