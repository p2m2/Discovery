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
  def count(): Promise[Int] = { sw.finder.count.toJSPromise }

  @JSExport
  def findClasses(regex : String = "",uri:URI = URI(""), page : Int = 0 ): Promise[js.Array[URI]] =
  { sw.finder.classes(regex,uri,page).map(array => array.toJSArray).toJSPromise }

  @JSExport
  def findObjectProperties(regex : String = "",motherClassProperties: URI = URI(""), page : Int = 0 ) : Promise[js.Array[URI]] = {
    sw.finder.objectProperties(regex,motherClassProperties,page).map(array => array.toJSArray).toJSPromise
  }

  @JSExport
  def findDatatypeProperties(regex : String = "",motherClassProperties: URI = URI("") , page : Int = 0) : Promise[js.Array[URI]] = {
    sw.finder.datatypeProperties(regex,motherClassProperties,page).map(array => array.toJSArray).toJSPromise
  }

  @JSExport
  def findSubjectProperties(regex : String = "",motherClassProperties: URI = URI("") , page : Int = 0) : Promise[js.Array[URI]] = {
    sw.finder.subjectProperties(regex,motherClassProperties,page).map(array => array.toJSArray).toJSPromise
  }

}
