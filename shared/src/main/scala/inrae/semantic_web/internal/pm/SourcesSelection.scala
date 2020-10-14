package inrae.semantic_web.internal.pm

import inrae.semantic_web.internal._
import inrae.semantic_web.{ConfigurationObject, QueryManager, StatementConfiguration}

import scala.concurrent.Future
/**
 * 
 */
object SourcesSelection  {

    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
/*
    def selectSourcesNode(n: Node,config: StatementConfiguration) : Future[Seq[ConfigurationObject.Source]] = {
        val sources = config.sources()
          Future.sequence(sources.map(s => QueryManager.testNode(n,s))).map {
             bs => (sources zip bs).filter(_._2).map(_._1)
          }
    }

    def get( n: Node,config: StatementConfiguration ) : Future[Seq[ConfigurationObject.Source]] = {
        n match {
            case node : Something          => Future { config.sources() } // all sources is available
            case node : SubjectOf          => selectSourcesNode(node,config)// detect if relation exist
            case node : ObjectOf           => selectSourcesNode(node,config)// detect if relation exist
            case _                         => Future { Seq[ConfigurationObject.Source]() }
        }
    }

 */
}

