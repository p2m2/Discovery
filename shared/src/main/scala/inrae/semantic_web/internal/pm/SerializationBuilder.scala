package inrae.semantic_web.internal.pm

import inrae.semantic_web.SWDiscovery
import upickle.default._

object SerializationBuilder {

  def serialize(sw: SWDiscovery): String = write(sw)

  def deserialize(str: String): SWDiscovery = read[SWDiscovery](str)

}
