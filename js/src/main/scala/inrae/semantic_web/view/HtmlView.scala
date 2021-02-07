package inrae.semantic_web.view

import facade.npm.Showdown
import inrae.semantic_web.internal.pm.{SelectNode, SimpleConsole}
import inrae.semantic_web.{SWDiscovery, SWDiscoveryVersionAtBuildTime}
import org.scalajs.dom.document

import scala.scalajs.js
import scala.scalajs.js.Dynamic

case class HtmlView(sw: SWDiscovery,regex : String = "") {
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

  sw.finder.count.map(c => {
    count = c.toString
    update()
  })

  sw.finder.classes(regex).map(lUris => {
    classes = " - " + lUris.mkString("\n - ")
    update()
  })

  sw.finder.objectProperties(regex).map(lUris => {
    subjectOfObjectProperties = " - " + lUris.mkString("\n - ")
    update()
  })
  sw.finder.datatypeProperties(regex).map(lUris => {
    subjectOfDatatypeProperties = " - " + lUris.mkString("\n - ")
    update()
  })

  sw.finder.subjectProperties(regex).map(lUris => {
    objectOfProperties = " - " + lUris.mkString("\n - ")
    update()
  })

  (if (regex.trim != "" ) {
    sw.filter.regex(regex)
  } else {
    sw
  })
    .select(Seq(sw.focus())).limit(limitValues).commit().raw.map(resultsJson => {
    values = " - " + resultsJson("results")("bindings").arr.mkString("\n - ")
    update()
  })


  val css = ""

  def text =
    s"""
## [Discovery](https://github.com/p2m2/Discovery)
 - build    : ${SWDiscoveryVersionAtBuildTime.version}

### New step on focus

 - **target node**      : ${SelectNode.getNodeWithRef(sw.focusNode,sw.rootNode).mkString(",")}
 - **regex**            : $regex
 - **Number of values** : **$count**

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

#### Common

```
- helper : this page
- focus(`var`) : set the focus
- root() : go back to root
- prefix(`short`,`long`) : set prefix
- graph(`iri`), namedGraph(`iri`) : set graph or graph named
```

#### Browsing the semantic graph

```
- something(`var`) : start a query about something
- isA(`uri`)  : set the focus type/class
- isSubjectOf(`uri`,`var`) : focus is a subject of a triplet which `uri` is the property
- isObjectOf(`uri`,`var`) : focus is the object of a triplet which `uri` is the property

- datatype(`uri`,`var`) : focus is a subject of a triplet which `uri` is a datatype property (OWL)
```

#### Setting values

```
- set(`sparqlDef`)
- setList(`sparqlDef1`,`sparqlDef2`,..)
```

#### Filtering

```
- filter.not.{fun}
- filter.isLiteral
- filter.isUri
- filter.isBlank
- filter.regex( pattern:`literal|var`, flags : `literal|var` )
- filter.contains( `literal|var` )
- filter.strStarts( `literal|var` )
- filter.strEnds( `literal|var` )
- filter.equal( `literal|var` )
- filter.notEqual( `literal|var` )
- filter.inf( `literal|var` )
- filter.infEqual( `literal|var` )
- filter.sup( `literal|var` )
- filter.supEqual( `literal|var` )
```

#### Binding a new variable

```
- .bind(`var`).subStr(startingLoc : SparqlDefinition,length : SparqlDefinition )
- .bind(`var`).replace(pattern : SparqlDefinition, replacement : SparqlDefinition, flags : SparqlDefinition="")
- .bind(`var`).abs()
- .bind(`var`).ceil()
- .bind(`var`).floor()
- .bind(`var`).rand()
```

#### Getting results

```
- select(`ref1,ref2,ref3,..`)
- select(`[ref1,ref2,ref3,..]`,`limit`,`offset`) : getResults as a promise (json format)
- selectByPage(`ref1,ref2,ref3,..`) : getResults as a promise (count,Array[Promise])
```


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
