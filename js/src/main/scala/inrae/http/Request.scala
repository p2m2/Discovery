package inrae.http
import inrae.semantic_web.sparql.QueryResult
import org.scalajs.dom

import scala.concurrent.{Future,Promise}
import scala.scalajs.js.URIUtils

case class Request(var url : String) {

  var fd = new dom.FormData()


  def queryViaGet(query : String, mimetype : String) : dom.XMLHttpRequest = {
    val xhr = new dom.XMLHttpRequest()

    xhr.open("GET", url);
    xhr.setRequestHeader("Content-Type", "application/sparql-query")
    xhr.setRequestHeader("Accept", mime(mimetype))
    fd.append("query",query)

    xhr.onload = { (e: dom.Event) =>
      if (xhr.status == 200) {
          println(xhr.responseText)
      } else {
        println(e);
      }
    }

    xhr.send(fd)

    return xhr
  }

  def queryViaPost(query : String, mimetype : String) : Future[QueryResult] = {
    val xhr = new dom.XMLHttpRequest()

    xhr.open("POST", url);
    xhr.setRequestHeader("Content-Type", "application/sparql-query")
    xhr.setRequestHeader("Accept", mime(mimetype))
    fd.append("query",query)

    val p = Promise[QueryResult]()

    xhr.onload = { (e: dom.Event) =>
      print(xhr.responseText)
      if (xhr.status == 200) {
        val qr2 = mimetype match {
          case "json" => p success QueryResult(xhr.responseText, mimetype)
          case _ => throw new Exception("Unknown formatter : "+mimetype)
        }
        //println(xhr.responseText)
      } else
          throw new Exception("Error : "+xhr.responseText)
    }
    xhr.send(fd)
    p.future

  }

  def queryViaUrlEncodedPost(query : String, mimetype : String) : dom.XMLHttpRequest = {
    val xhr = new dom.XMLHttpRequest()

    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded")
    xhr.setRequestHeader("Accept", mime(mimetype))
    fd.append("query",URIUtils.encodeURIComponent(query))
    xhr.send(fd)
    return xhr
  }

  def setDefaultGraph(graphs : Seq[String]) = {
    fd.append("default-graph-uri",graphs)
  }

  def setNamedGraphUri(graphs : Seq[String]) = {
    fd.append("named-graph-uri",graphs)
  }

  def mime(idx : String) : String = {
    idx match {
      case "csv" => "text/csv; charset=utf-8"
      case "tsv" => "text/tab-separated-values; charset=utf-8"
      case "xml" => "application/sparql-results+xml"
      case "json" => "application/sparql-results+json"
      case "rdf" => "application/rdf+xml"
      case "n3" => "text/rdf+n3"
      case "plain" => "text/plain"
      case "turtle" => "application/x-turtle"
    };
  }

}
