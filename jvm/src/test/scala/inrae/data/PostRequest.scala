package inrae.data

import java.io.{BufferedReader, BufferedWriter, InputStreamReader, OutputStreamWriter}
import java.net.{HttpURLConnection, URL}
import scala.concurrent.Future


abstract class SimpleHttpRequest {
  type Param = Map[String, String]

  def post(url:String, data:Param):String
  def post(url:String, data:Param, headers:Param):String
}


case object PostRequest extends SimpleHttpRequest {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  def post(url:String, data:Param):String = this.post(url, data, null)
  def post(url:String, data:Param, headers:Param):String = {
    try {
      val http = this.getHttpURLConnection(url, "POST", headers)(h => {
        h.setDoOutput(true)
      })

      val bw = new BufferedWriter(new OutputStreamWriter(http.getOutputStream))
      try {
        bw.write(data.toString)
        bw.flush
      } finally {
        bw.close
      }

      this.getContentsBody(http)
    } catch {
      case _ => null
    }
  }


  protected def getHttpURLConnection(url:String, method:String, headers:Param)
                                    (implicit interrupt:HttpURLConnection => Unit):HttpURLConnection = {
    val http = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
    http.setRequestMethod(method)
    http.setInstanceFollowRedirects(false)
    if(headers != null){
      headers.foreach(h => http.setRequestProperty(h._1, h._2))
    }
    interrupt(http)
    http.connect
    http
  }

  protected def getContentsBody(http:HttpURLConnection):String = {
    val br = new BufferedReader(new InputStreamReader(http.getInputStream))
    val sb = new StringBuilder(1024)
    var line = ""
    try {
      while({line = br.readLine; line != null}){
        sb.append(line + "\n")
      }
    } finally {
      br.close
    }
    sb.toString
  }

  def put(stringQuery: String, url_endpoint: String) : Future[String] = {
    Future { post(url_endpoint, Map("query" -> stringQuery)) }
  }
}
