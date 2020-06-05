package EasySparql

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|

@js.native
@JSImport("@comunica/actor-init-sparql", "newEngine")
class ComunicaNewEngine() extends js.Object {
  def apply(n: Double): String = js.native
  def query(q: String): js.Function = js.native
}
