package inrae.semantic_web.sparql

import inrae.semantic_web.sparql.QueryResult
import wvlet.log.Logger.rootLogger.info

import scala.concurrent.Future

case class ConfigurationHttpRequest(
                                     url : String,
                                     login: String = "", // proxy, basic case
                                     password: String="", // proxy, basic case
                                     token: String = "", // bearer case
                                     auth: String = "", // basic, proxy, bearer
                                     mimetype : String ="json"
                                   ) {

}

final case class HttpRequestDriverException(private val message: String = "",
                                            private val cause: Throwable = None.orNull) extends Exception(message,cause)

abstract class HttpRequestDriver {

  def request(`type`: String , query: String, config : ConfigurationHttpRequest ): Future[QueryResult] = {
    info(" -- HttpRequestDriver > " + this.getClass.getName )
    `type`.toLowerCase() match {
      case "post" => post( query, config  )
      case "post_encoded" => post_encoded( query, config  )
      case "get" => get( query, config  )
      case _ => throw HttpRequestDriverException(s"Unknown http type request : ${`type`}")
    }
  }

  def get( query: String, config : ConfigurationHttpRequest ) : Future[QueryResult]

  def post( query: String, config : ConfigurationHttpRequest ) : Future[QueryResult]

  def post_encoded( query: String, config : ConfigurationHttpRequest  ) : Future[QueryResult]

}
