package EasySparql

sealed trait Node {
  var children : Array[Node] = Array[Node]()
  var endpoints : Array[String] = Array[String]()

  def addChildren(n : Node) : Node = {
    children = children :+ n
    return n
  }
}

class Root extends Node 
class ReferenceNode(var uniqRef : String) extends Node 
class Something(uniqRef : String) extends ReferenceNode(uniqRef) 
class SubjectOf(uniqRef : String, var uri : URI) extends ReferenceNode(uniqRef)  
class ObjectOf(uniqRef : String, var uri : URI) extends ReferenceNode(uniqRef)  
class Attribute(uniqRef : String, var uri : URI) extends ReferenceNode(uniqRef)  
class Value(var uri : RdfType) extends Node 
