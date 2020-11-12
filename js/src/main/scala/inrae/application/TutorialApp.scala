package inrae.application

import inrae.semantic_web.rdf.URI
import inrae.semantic_web.{SW, StatementConfiguration}
import org.scalajs.dom
import org.scalajs.dom.document

import scala.scalajs.js.annotation.JSExportTopLevel

object TutorialApp {
  def main(args: Array[String]): Unit = {

    println("start----------------------------------");
    val config: StatementConfiguration = new StatementConfiguration()
    config.setConfigString(
      """
        |{
        | "sources" : [{
        |   "id"  : "dbpedia",
        |   "url" : "https://dbpedia.org/sparql",
        |   "typ" : "tps",
        |   "method" : "POST"
        | }]}
        |""".stripMargin)
    val query = new SW(config)
    val r = query.something("h1")
      .set(URI("http://dbpedia.org/resource/%C3%84lvdalen"))
      .isSubjectOf(URI("http://www.w3.org/2002/07/owl#sameAs"))
      .select()

    print(r)
    //appendPar(document.body, "Hello World")
  }

  def appendPar(targetNode: dom.Node, text: String): Unit = {
    val parNode = document.createElement("p")
    parNode.textContent = text
    targetNode.appendChild(parNode)
  }

  @JSExportTopLevel("addClickedMessage")
  def addClickedMessage(): Unit = {
    appendPar(document.body, "You clicked the button!!!!!!!")
  }
}
