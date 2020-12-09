package inrae.semantic_web.driver

import inrae.semantic_web.sparql.{ConfigurationHttpRequest, HttpRequestDriver, HttpRequestDriverException, QueryResult}
import org.portablescala.reflect.annotation.EnableReflectiveInstantiation
import wvlet.log.Logger.rootLogger.info

import scala.concurrent.Future

@EnableReflectiveInstantiation
class HttpRequestLihaoyiDriver() extends HttpRequestDriver {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def jvmCompat() : Boolean = false
  def jsCompat() : Boolean = true

  val connectTimeOut = 100000
  val readTimeout = 100000
/*
  def auth(config : ConfigurationHttpRequest): RequestAuth = config.auth match {
    case "bearer" => RequestAuth.Bearer(config.token)
    case "basic" => new RequestAuth.Basic(config.login,config.password)
    case "proxy" => RequestAuth.Proxy(config.login,config.password)
    case _ => requests.auth
  }
*/
  def post( query: String, config : ConfigurationHttpRequest ): Future[QueryResult] = {
    info(" -- post --")
    Future {
      val r = requests.post(
        config.url,
  //      auth=auth(config),
        data = Map("query" -> query),
        headers = Map(
          "Content-Type"->"application/x-www-form-urlencoded",
          "Accept" -> "application/json"
        ),
        readTimeout = readTimeout,
        connectTimeout = connectTimeOut,
      )
      QueryResult(r.text())
    }
  }

  def get( query: String, config : ConfigurationHttpRequest ) : Future[QueryResult] = {
    info(" -- get --")
    Future {
      val r = requests.get(
        config.url,
     //   auth=auth(config),
        data = Map("query" -> query),
        headers = Map(
          "Content-Type"->"application/x-www-form-urlencoded",
          "Accept" -> "application/json"
        ),
        readTimeout = readTimeout,
        connectTimeout = connectTimeOut,
      )
      QueryResult(r.text())
    }
  }

  def post_encoded( query: String, config : ConfigurationHttpRequest  ) : Future[QueryResult] = {
    throw HttpRequestDriverException("post_encoded not available with "+this.getClass.getName)
  }
}
