package inrae.semantic_web.driver

import inrae.semantic_web.ConfigurationObject.Source
import inrae.semantic_web.SWDiscoveryException
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.sail.nativerdf.NativeStore
import wvlet.log.Logger.rootLogger.debug

import java.io.File
import java.net.URL
import scala.util.{Failure, Success, Try}

object RequestDriverFactory  {


  lazy val repository = new SailRepository(new NativeStore())

  def build( source : Source ) : RequestDriver = {

    val graph = "fr:inrae:semantic_web:discovery:"+source.id

    source.mimetype match {
      case "application/sparql-query" if source.url != "" =>
        Rdf4jSparqlRequestDriver(
          source.id,
          source.url,
          source.login,
          source.password,
          source.token,
          source.auth)
      case "text/turtle" | "text/n3" | "text/rdf-xml" | "application/rdf+xml" =>
        if (source.url != "") {

          /* name file is the graph name  */
          Try(repository.getConnection().add(new URL(source.url), graph, RequestDriverFactory.mimetypeToRdfFormat(source.mimetype))) match {
            case Success(_) => debug(graph + " is loaded !")
            case Failure(e) => throw SWDiscoveryException(e.getMessage)
          }

        } else if ( source.file != "" ) {
          Try(repository.getConnection().add(new File(source.file), graph, RequestDriverFactory.mimetypeToRdfFormat(source.mimetype))) match {
            case Success(_) => debug(graph + " is loaded !")
            case Failure(e) => throw SWDiscoveryException(e.getMessage)
          }

        } else if ( source.content != "" ) {
          val targetStream = new java.io.ByteArrayInputStream(source.content.getBytes(java.nio.charset.StandardCharsets.UTF_8.name))
          Try(repository.getConnection().add(targetStream, graph, RequestDriverFactory.mimetypeToRdfFormat(source.mimetype))) match {
            case Success(_) => debug(graph + " is loaded !")
            case Failure(e) => throw SWDiscoveryException(e.getMessage)
          }

        } else {
          throw SWDiscoveryException("Bad definition of source configuration :"+source.toString)
        }

        Rdf4jRequestDriver(repository.getConnection())

      case _ =>
        throw SWDiscoveryException("Bad definition of source configuration :"+source.toString)
    }
  }

  def mimetypeToRdfFormat( mimetype : String ) = mimetype match {
    case "text/turtle" => RDFFormat.TURTLE
    case "text/n3" => RDFFormat.N3
    case "text/rdf-xml" => RDFFormat.RDFXML
    case _ => throw SWDiscoveryException(mimetype + " : this format is not supported")
  }

}
