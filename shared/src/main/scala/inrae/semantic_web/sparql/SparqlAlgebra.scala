package inrae.semantic_web.sparql

import scala.language.implicitConversions
import inrae.semantic_web.internal.{RdfNode, _}
import inrae.semantic_web.rdf._
import inrae.semantic_web.internal.pm.SparqlGenerator

import scala.annotation.tailrec
import scala.collection.mutable

/**
 * https://www.w3.org/2011/09/SparqlAlgebra/ARQalgebra
 * https://www.w3.org/TR/sparql11-query/
 *
 *
 * Validator : http://sparql.org/query-validator.html
 */

// https://www.w3.org/TR/sparql11-query/#Grammar

sealed trait SparqlAlgebra {
  def sparql() : String
}

case class Nothing() extends SparqlAlgebra {
  def sparql() : String = ""
}

case class Base(iri : IRI, prefixes : Seq[Prefix], start : StartQuerySparqlAlgebra) extends SparqlAlgebra {
  def sparql() : String = {
    prefixes.map( _.sparql() ).mkString("\n")
    start.sparql()
  }
}

case class Prefix(short : String, long : IRI) extends SparqlAlgebra {
  def sparql() : String = "PREFIX "+ short + ": "+ long.toString()
}

trait StartQuerySparqlAlgebra extends SparqlAlgebra

case class Slice (start : StartQuerySparqlAlgebra, limit: Int, offset : Int) extends StartQuerySparqlAlgebra {
  def sparql() : String = start.sparql() + {
    if (limit > 0) " LIMIT " + limit.toString
  } + {
    if (offset > 0) " OFFSET " + offset.toString
  }
}

trait TypeProject extends SparqlAlgebra

case class DistinctProject() extends TypeProject {
  def sparql() : String = "DISTINCT"
}

case class SimpleProject() extends TypeProject {
  def sparql() : String = ""
}
/*
case class REDUCED() extends TypeProject {
  def sparql() : String = "REDUCE"
}
*/
case class Project(typeProject : TypeProject, variables : Variables, datasetsClause : Seq[DatasetClause]) extends StartQuerySparqlAlgebra {
  def sparql() : String = "SELECT "+typeProject.sparql()+" " + variables.sparql() + datasetsClause.map( _.sparql() ).mkString("\n")
}

case class Variables(variables : Seq[Variable], gp : SparqlAlgebra, groupBy : GroupByVar) extends StartQuerySparqlAlgebra {
  def sparql() : String = variables.mkString(" ") + " WHERE " + "{" + gp.sparql() + " } " + groupBy.sparql()
}

case class Variable(name : String) extends SparqlAlgebra {
  def sparql() : String = "?"+name
}

trait DatasetClause extends SparqlAlgebra

case class DefaultGraphClause(iri : IRI) extends DatasetClause {
  def sparql() : String = "FROM " + iri.toString()
}
case class NamedGraphClause(iri : URI) extends DatasetClause {
  def sparql() : String = "FROM NAMED " + iri.toString()
}

case class GroupByVar(variables : Seq[String]) extends SparqlAlgebra {
  def sparql() : String = "GROUP BY " + variables.mkString(" ")
}

trait GraphPattern extends SparqlAlgebra


case class Bgp(lTriples : Seq[Triple] , filters : Seq[Filter]) extends GraphPattern {
  def sparql() : String = lTriples.map( _.sparql() ).mkString(".\n") + filters.map ( _.sparql()).mkString(".\n")
}
case class Triple(s: Either[SparqlDefinition,Variable], p: Either[SparqlDefinition,Variable], o:Either[SparqlDefinition,Variable]) extends SparqlAlgebra {
  def sparql() : String =  s.fold( _.sparql(), _.sparql() ) + " " +
                           p.fold( _.sparql(), _.sparql() ) + " " +
                           o.fold( _.sparql(), _.sparql() )
}


case class Join(left : SparqlAlgebra, right : SparqlAlgebra) extends GraphPattern {
  def sparql() : String = left.sparql() + "\n" + right.sparql()
}
case class LeftJoin(left : SparqlAlgebra, right : SparqlAlgebra) extends GraphPattern {
  def sparql() : String = left.sparql() + "\n OPTIONAL {" + right.sparql() + "}"
}
case class Union(left : SparqlAlgebra, right : SparqlAlgebra) extends GraphPattern {
  def sparql() : String = "{ "+ left.sparql() + "} UNION { " + right.sparql() + " }"
}

case class SubSelect(start : StartQuerySparqlAlgebra) extends GraphPattern {
  def sparql() : String = "{" +  start + "}"
}
case class Filter(operator : Operator) extends SparqlAlgebra {
  def sparql() : String = "FILTER {" + operator.sparql() + "}"
}

// https://www.rubydoc.info/github/ruby-rdf/sparql/SPARQL/Algebra/Operator
trait Operator extends SparqlAlgebra {
  def arity : Int
}

trait OperatorNullary extends Operator {
  def arity : Int = 0
}

trait OperatorUnary extends Operator {
  def arity : Int = 1
}

trait OperatorBinary extends Operator {
  def arity : Int = 2
}

trait OperatorTernary extends Operator {
  def arity : Int = 3
}

case class Contains(left : Either[SparqlDefinition,String], right: Either[SparqlDefinition,String]) extends OperatorBinary {
  def sparql() : String = "CONTAINS(" + left.fold( _.sparql(),_.toString ) + " " + right.fold( _.sparql(), _.toString ) + ")"
}


object SparqlAlgebra {
  implicit def string2Variable(x: String) = Variable(x)

  def join(children : Seq[Node], map : Map[String,String]) : SparqlAlgebra = children match {
    case x :: Nil => algebra(x,map)
    case x :: tail => Join(algebra(x,map),join(tail,map))
  }

  def union(children : Seq[Node], map : Map[String,String]) : SparqlAlgebra = children match {
    case x :: Nil => algebra(x,map)
    case x :: tail => Union(algebra(x,map),union(tail,map))
  }

  /*
   pas d union au root
   */
  def algebra(root : Root, map : Map[String,String] ) : SparqlAlgebra = {
    if (root.children.length == 0 ) {
      Triple(Right("s"),Right("s"),Right("s"))
    } else {
        join(root.children,map)
    }
  }
  /*
     union ou join
   */
  def algebra(n : Node , map : Map[String,String] ) : SparqlAlgebra = {
    if (n.children.length == 0 ) {
      Nothing()
    } else {
      val unionBlock : SparqlAlgebra = n.children.collect( _ match { case u : UnionBlock => union(u.children,map) } )
                                                 .reduce( (u1,u2) => Union(u1,u2))
      val bgpBlock : SparqlAlgebra = Bgp(n.children.collect( _ match { case rdf : RdfNode => algebra(n,rdf,map) } )
                                         ,List()) // TODO Filter
      Join(bgpBlock,unionBlock)
    }
  }

  def algebra(sire : RdfNode, n : Node, map : Map[String,String]) : SparqlAlgebra = n match {
    case rdf : RdfNode => algebra(sire,rdf,map)
    case union : Union => algebra(sire,union,map)
  }

  def algebra(sire : Node, child : RdfNode, map : Map[String,String]) : Triple = sire match {
    case l : LogicNode => algebra(l,child,map)
  }

  def algebra(ln : LogicNode, child : RdfNode, map : Map[String,String]) : Triple = algebra(ln.sire,child,map)


  def algebra(sire : RdfNode, child : RdfNode, map : Map[String,String]) : Triple = {
    val sireSparqlAlgebra : Either[SparqlDefinition,Variable] = sire match {
      case r : RdfNode => Right(Variable(map(r.reference())))
    }
    child match {
      case a : SubjectOf => Triple(sireSparqlAlgebra,Left(a.term),Right(Variable(map(child.reference()))))
      case a : ObjectOf =>Triple(Right(Variable(map(child.reference()))),Left(a.term),sireSparqlAlgebra)
      case a : LinkTo => Triple(sireSparqlAlgebra,Right(Variable(map(child.reference()))),Left(a.term))
      case a : LinkFrom => Triple(Left(a.term),Right(Variable(map(child.reference()))),sireSparqlAlgebra)
    }
  }

  def nodeToSparqlAlgebra( r : Root, n : Node, distinct : Boolean, limit : Int, offset : Int ) = {
    val (mapIdVar,_) = SparqlGenerator.correspondenceVariablesIdentifier(n)
    val variables = mapIdVar.values.map( Variable(_)).toSeq

    Base(IRI("http://www.inrae.fr/easySparql/"),
      r.prefixes.map( {case (k,v) => Prefix(k,v)}).toSeq,
      { (distinct, limit, offset) match {
        case (false,l,o) if ( l < 0 && o < 0 ) => Project(SimpleProject(),
          Variables(variables,
            algebra(r,mapIdVar),
            GroupByVar(List())),
          r.defaultGraph.map( DefaultGraphClause(_)))
      } })
  }
}


