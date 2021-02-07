package inrae.semantic_web.view

import facade.npm.Showdown
import inrae.semantic_web.internal.pm.{SelectNode, SimpleConsole}
import inrae.semantic_web.{SWDiscovery, SWDiscoveryVersionAtBuildTime}
import org.scalajs.dom.document

import scala.scalajs.js
import scala.scalajs.js.Dynamic

case class HtmlView(sw: SWDiscovery) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val updateElapsedTime = 1000
  val limitValues = 10

  val waitingForFuture = "[X]"

  /* count de la solution */
  var count: String = waitingForFuture
  var classes: String = waitingForFuture
  var subjectOfObjectProperties: String = waitingForFuture
  var subjectOfDatatypeProperties: String = waitingForFuture
  var objectOfProperties: String = waitingForFuture
  var values: String = waitingForFuture

  sw.helper.count.map(c => {
    count = c.toString
    update()
  })
//
  sw.helper.findClasses().map(lUris => {
    classes = " - " + lUris.mkString("\n - ") + "\n - [next]()"
    update()
  })
  sw.helper.findObjectProperties().map(lUris => {
    subjectOfObjectProperties = " - " + lUris.mkString("\n - ")
    update()
  })
  sw.helper.findDatatypeProperties().map(lUris => {
    subjectOfDatatypeProperties = " - " + lUris.mkString("\n - ")
    update()
  })
  sw.select(Seq(sw.focus())).limit(limitValues).commit().raw.map(resultsJson => {
    values = " - " + resultsJson("results")("bindings").arr.mkString("\n - ")
    update()
  })


  val css = ""

  def text =
    s"""
## Discovery
 - build    : ${SWDiscoveryVersionAtBuildTime.version}

### New step on focus

 - target node      : ${SelectNode.getNodeWithRef(sw.focusNode,sw.rootNode).mkString(",")}
 - Number of values : *$count*

#### Values ${limitValues.toString} .set(`value`) .setList(`value1`,`value1`,..)
$values

#### Classes .isA("`uri`")

$classes

#### Forward property  .isSubjectOf("`uri`","my_reference_var")

$subjectOfObjectProperties

#### Datatype property .datatype("`uri`","my_reference_var")

$subjectOfDatatypeProperties

#### Backward property .isObjectOf("`uri`","my_reference_var")

$objectOfProperties

### configuration

${sw.config}


### Request
```
${SimpleConsole(consoleColor=false,displayRootStyle=false).get(sw.rootNode)}
```

### Help

 - [declare an issue ?] (https://github.com/p2m2/Discovery/issues/new)

"""


  val options: js.Object with Dynamic = Dynamic.literal(
    "ghCodeBlocks" -> true,
    "tables" -> true,
    "strikethrough" -> false
  )


  def update(): Any = {
    val converter = new Showdown.Converter(options)
    val html: String = converter.makeHtml(css + text)
    document.querySelector("html").innerHTML = html
  }

  update()
}
