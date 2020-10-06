package inrae.http
import inrae.semantic_web.sparql.QueryResult
import org.scalajs.dom

import scala.concurrent.{Future,Promise}
import scala.scalajs.js.URIUtils
import scala.scalajs.js

case class Request(var url : String) {

  def addData(keyname: String,value: String) = {
    URIUtils.encodeURIComponent(keyname)+"="+URIUtils.encodeURIComponent(value)
  }

  def addQueryAsData(query : String) = {
    addData("query",query)
  }

  def addDefaultGraph(graphs : Seq[String]) = {
    graphs.reduce( (g1,g2) => addData("default-graph-uri",g1)+"&"+addData("default-graph-uri",g2))
  }

  def addNamedGraphUri(graphs : Seq[String]) = {
    graphs.reduce( (g1,g2) => addData("named-graph-uri",g1)+"&"+addData("named-graph-uri",g2))
  }

  def send(xhr : dom.XMLHttpRequest, data: scala.scalajs.js.Any, mimetype : String) : Future[QueryResult] = {
    val p = Promise[QueryResult]()

    xhr.onload = { (e: dom.Event) =>
      print(xhr.responseText)
      if (xhr.status == 200) {
        val qr2 = mimetype match {
          case "json" => p success QueryResult(xhr.responseText, mimetype)
          case _ => p.failure(new js.JavaScriptException("Unknown formatter : "+mimetype))
        }
        //println(xhr.responseText)
      } else
        throw new Exception("Error : "+xhr.responseText)
    }

    xhr.onerror = { e: dom.ErrorEvent =>
      p.failure(new js.JavaScriptException(xhr))
    }

    xhr.send(data)
    p.future
  }

  def queryViaPost(query : String, mimetype : String) : Future[QueryResult] = {
    val xhr = new dom.XMLHttpRequest()

    xhr.open("POST", url);
    xhr.setRequestHeader("Content-Type", "application/sparql-query")
    xhr.setRequestHeader("Accept", mime(mimetype))
    val data = addQueryAsData(query)
    send(xhr,data,mimetype)
  }

  def queryViaGet(query : String, mimetype : String) : Future[QueryResult] = {
    val xhr = new dom.XMLHttpRequest()

    xhr.open("GET", url+"?"+addQueryAsData(query));
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded")
    xhr.setRequestHeader("Accept", mime(mimetype))

    send(xhr,"",mimetype)
  }



  def queryViaUrlEncodedPost(query : String, mimetype : String) : Future[QueryResult] = {
    val xhr = new dom.XMLHttpRequest()

    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded")
    val data = addQueryAsData(query)
    send(xhr,data,mimetype)
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
