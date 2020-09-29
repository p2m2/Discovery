package inrae.http
import org.scalajs.dom

case class Request(var url : String) {
  var fd = new dom.FormData()

  def queryViaGet(query : String, mimetype : String) : dom.XMLHttpRequest = {
    val xhr = new dom.XMLHttpRequest()

    xhr.open("GET", url, false);
    xhr.setRequestHeader("Content-Type", "application/sparql-query")
    xhr.setRequestHeader("Accept", mime(mimetype))
    fd.append("query",query)

    xhr.onload = { (e: dom.Event) =>
      if (xhr.status == 200) {
          println(xhr.responseText)
      }
    }

    xhr.send(fd)

    return xhr
  }

  def queryViaPost(query : String, mimetype : String) : dom.XMLHttpRequest = {
    val xhr = new dom.XMLHttpRequest()

    xhr.open("POST", url, false);
    xhr.setRequestHeader("Content-Type", "application/sparql-query")
    xhr.setRequestHeader("Accept", mime(mimetype))
    fd.append("query",query)

    xhr.onload = { (e: dom.Event) =>
      if (xhr.status == 200) {
        println(xhr.responseText)
      }
    }


    xhr.send(fd)

    return xhr
  }

  def queryViaUrlEncodedPost(query : String, mimetype : String) : dom.XMLHttpRequest = {
    val xhr = new dom.XMLHttpRequest()

    xhr.open("POST", url, false);
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded")
    xhr.setRequestHeader("Accept", mime(mimetype))
    fd.append("query",query)
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
