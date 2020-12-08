package inrae.http
import org.scalajs.dom

import wvlet.log.Logger.rootLogger._

import inrae.semantic_web.sparql.QueryResult
import scala.concurrent.{Future,Promise}
import scala.scalajs.js.URIUtils
import scala.scalajs.js


case class Request(var url : String) {

  def addEncodedData(keyname: String, value: String) = {
    URIUtils.encodeURIComponent(keyname)+"="+URIUtils.encodeURIComponent(value)
  }

  def addData(keyname: String, value: String) = {
    value
  }

  def addQueryAsEncodedData(query : String) = {
    addEncodedData("query",query)
  }

  def addQueryAsData(query : String) = {
    addData("query",query)
  }

  def addDefaultGraph(graphs : Seq[String]) = {
    graphs.reduce( (g1,g2) => addEncodedData("default-graph-uri",g1)+"&"+addEncodedData("default-graph-uri",g2))
  }

  def addNamedGraphUri(graphs : Seq[String]) = {
    graphs.reduce( (g1,g2) => addEncodedData("named-graph-uri",g1)+"&"+addEncodedData("named-graph-uri",g2))
  }

  def send(xhr : dom.XMLHttpRequest, data: scala.scalajs.js.Any, mimetype : String) : Future[QueryResult] = {
    val p = Promise[QueryResult]()

    xhr.onload = { (e: dom.Event) =>
      if (xhr.status == 200) {
        val qr2 = mimetype match {
          case "json" => p success QueryResult(xhr.responseText, mimetype)
          case _ =>
            p.failure(js.JavaScriptException("[Configuration] Parser not available for this MIME type : "+mimetype))
        }
      } else
        p.failure(js.JavaScriptException(xhr))
    }

    xhr.onerror = { e: dom.ErrorEvent =>
      error(e.toString)
      p.failure(js.JavaScriptException(xhr))
    }

    xhr.send(data)
    p.future
  }

  def queryViaPost(query : String, mimetype : String) : Future[QueryResult] = {
    val xhr = new dom.XMLHttpRequest()

    xhr.open("POST", url);
    xhr.setRequestHeader("Content-Type", "application/sparql-query")
    //xhr.setRequestHeader("Access-Control-Allow-Origin", "*")
    xhr.setRequestHeader("Accept", mime(mimetype))
    val data = addQueryAsData(query)
    send(xhr,data,mimetype)
  }

  def queryViaGet(query : String, mimetype : String) : Future[QueryResult] = {
    val xhr = new dom.XMLHttpRequest()

    xhr.open("GET", url+"?"+addQueryAsData(query));
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded")
    //xhr.setRequestHeader("Access-Control-Allow-Origin", "*")
    xhr.setRequestHeader("Accept", mime(mimetype))

    send(xhr,"",mimetype)
  }

  def queryViaUrlEncodedPost(query : String, mimetype : String) : Future[QueryResult] = {
    val xhr = new dom.XMLHttpRequest()

    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded")
    //xhr.setRequestHeader("Access-Control-Allow-Origin", "*")
    xhr.setRequestHeader("Accept", mime(mimetype))

    val data = addQueryAsEncodedData(query)
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
      case _ => "text/plain"
    };
  }

}
