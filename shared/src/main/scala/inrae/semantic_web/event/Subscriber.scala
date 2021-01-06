package inrae.semantic_web.event

trait Subscriber[-Event,-Publisher] {
  def notify (pub: Publisher, event: Event): Unit
}
