package EasySparql


trait VisitorNodesInterface {
  def visit( node : Root ) : Unit
  def visit( node : Something ) : Unit
  def visit( node : SubjectOf ) : Unit
  def visit( node : ObjectOf ) : Unit
}