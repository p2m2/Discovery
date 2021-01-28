package inrae.semantic_web.driver

import inrae.semantic_web.ConfigurationObject.Source
import inrae.semantic_web.SWDiscoveryException

object RequestDriverFactory  {


  def build( source : Source ) : RequestDriver = {

    val graph = "fr:inrae:semantic_web:discovery:"+source.id

    source.mimetype match {
      case "application/sparql-query" if source.url != "" =>
        SHttpRequestDriver(
          source.id,
          source.method,
          source.url,
          source.login,
          source.password,
          source.token,
          source.auth)
      case "text/turtle" | "text/n3" | "text/rdf-xml" | "application/rdf+xml" =>
          throw SWDiscoveryException( source.mimetype+" : functionality only available on the server side")
      case _ =>
        throw SWDiscoveryException("Bad definition of source configuration :"+source.toString)
    }
  }

}
