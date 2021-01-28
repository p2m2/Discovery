package inrae.semantic_web.driver

import inrae.semantic_web.ConfigurationObject.Source
import inrae.semantic_web.SWDiscoveryException

object RequestDriverFactory  {

  def build( source : Source ) : RequestDriver = {
    source.mimetype match {
      case "application/sparql-query" if source.url != "" =>
        Rdf4jHttpRequestDriver(
          source.id,
          source.method,
          source.url,
          source.login,
          source.password,
          source.token,
          source.auth)
      case "text/turtle" | "text/n3" | "text/rdf-xml"  =>
        if (source.url != "") {
          Rdf4jUrlFileRequestDriver(
            source.id,
            source.url,
            source.mimetype
          )
        } else if ( source.file != "" ) {
          Rdf4jFileRequestDriver(
            source.id,
            source.file,
            source.mimetype)
        } else if ( source.content != "" ) {
          Rdf4jLocalRequestDriver(
            source.id,
            source.content,
            source.mimetype)
        } else {
          throw SWDiscoveryException("Bad definition of source configuration :"+source.toString)
        }
      case "application/rdf+xml" =>
        Rdf4jHttpRequestDriver(
          source.id,
          source.method,
          source.url,
          source.login,
          source.password,
          source.token,
          source.auth)
      case _ =>
        throw SWDiscoveryException("Bad definition of source configuration :"+source.toString)
    }
  }

}
