package inrae.semantic_web.event

object DiscoveryStateRequestEvent extends Enumeration {
  type DiscoveryRequestEvent = Value
  val
  QUERY_BUILD,
  DATATYPE_BUILD,
  DATATYPE_DONE,
  START_HTTP_REQUEST,
  PROCESS_HTTP_REQUEST,
  ERROR_HTTP_REQUEST,
  FINISHED_HTTP_REQUEST,
  RESULTS_BUILD,
  RESULTS_DONE = Value
}

case class DiscoveryRequestEvent(state : DiscoveryStateRequestEvent.Value)
