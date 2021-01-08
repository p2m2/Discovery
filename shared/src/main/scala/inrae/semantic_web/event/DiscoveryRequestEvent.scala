package inrae.semantic_web.event

object DiscoveryStateRequestEvent extends Enumeration {

  type DiscoveryRequestEvent = Value
  val
  START,
  QUERY_BUILD,
  START_HTTP_REQUEST,
  PROCESS_HTTP_REQUEST,
  FINISHED_HTTP_REQUEST,
  RESULTS_BUILD,
  DATATYPE_BUILD,
  DATATYPE_DONE,
  RESULTS_DONE,
  REQUEST_DONE,
//10 steps
  ERROR_HTTP_REQUEST,
  ABORTED_BY_THE_USER
  = Value

  val nValidStep = 10

}

case class DiscoveryRequestEvent(state : DiscoveryStateRequestEvent.Value)
