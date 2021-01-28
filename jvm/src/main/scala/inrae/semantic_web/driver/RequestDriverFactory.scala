package inrae.semantic_web.driver

import inrae.semantic_web.ConfigurationObject.Source
import inrae.semantic_web.SWDiscoveryException
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.sail.memory.config.MemoryStoreConfig
import org.eclipse.rdf4j.repository.config.RepositoryConfig
import wvlet.log.Logger.rootLogger.debug

import java.io.File
import java.net.URL
import scala.util.{Failure, Success, Try}

object RequestDriverFactory  {
/*


  val datadir3 = new File("./mytest_stream_rdf")
  val repo3 = new SailRepository(new NativeStore(datadir3))
*/

    val pathManagerRepo = "./manager_repo"
    val baseDir = new File(pathManagerRepo)
    val manager = new LocalRepositoryManager(baseDir)

    manager.init()

    val persist = true
    val backendConfig = new MemoryStoreConfig(persist)
    // stack an inferencer config on top of our backend-config
    //backendConfig = new SchemaCachingRDFSInferencerConfig(backendConfig);

    // create a configuration for the repository implementation
    val repositoryTypeSpec : SailRepositoryConfig = new SailRepositoryConfig(backendConfig);



    val repositoryId = "local-discovery"

    val repConfig = new RepositoryConfig(repositoryId, repositoryTypeSpec)
    manager.addRepositoryConfig(repConfig)
    val repository: Repository = manager.getRepository(repositoryId)





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
