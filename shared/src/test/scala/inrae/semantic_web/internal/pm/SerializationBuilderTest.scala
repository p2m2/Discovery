package inrae.semantic_web.internal.pm

import inrae.data.DataTestFactory
import inrae.semantic_web.SWDiscovery
import inrae.semantic_web.internal.Root
import inrae.semantic_web.rdf.{Literal, QueryVariable, URI}
import utest.{TestSuite, Tests, test}


object SerializationBuilderTest extends TestSuite  {
  def tests: Tests = Tests {
    test("serialization basic 1") {
      val sw = SWDiscovery( DataTestFactory.getConfigVirtuoso1(),Root(),Some("test"))
      SerializationBuilder.deserialize(SerializationBuilder.serialize(sw)) == sw
    }

    test("serialization basic 2") {
      val sw = SWDiscovery( DataTestFactory.getConfigVirtuoso1())
      SerializationBuilder.deserialize(SerializationBuilder.serialize(sw)) == sw
    }

    test("serialization Something") {
      val sw =
        SWDiscovery( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )

      SerializationBuilder.deserialize(SerializationBuilder.serialize(sw)) == sw
    }

    test("serialization SubjectOf") {
      val sw =
        SWDiscovery( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .isSubjectOf(URI("bb"))

      SerializationBuilder.deserialize(SerializationBuilder.serialize(sw)) == sw
    }

    test("serialization ObjectOf") {
      val sw =
        SWDiscovery( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .isObjectOf(URI("bb"))

      SerializationBuilder.deserialize(SerializationBuilder.serialize(sw)) == sw
    }

    test("serialization LinkTo") {
      val sw =
        SWDiscovery( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .isLinkTo(URI("bb"))

      SerializationBuilder.deserialize(SerializationBuilder.serialize(sw)) == sw
    }

    test("serialization LinkFrom") {
      val sw =
        SWDiscovery( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .isLinkFrom(QueryVariable("bb"))

      SerializationBuilder.deserialize(SerializationBuilder.serialize(sw)) == sw
    }

    test("serialization Value QueryVariable") {
      val sw =
        SWDiscovery( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .set(QueryVariable("bb"))

      SerializationBuilder.deserialize(SerializationBuilder.serialize(sw)) == sw
    }

    test("serialization Value URI") {
      val sw =
        SWDiscovery( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .set(URI("bb"))

      SerializationBuilder.deserialize(SerializationBuilder.serialize(sw)) == sw
    }

    test("serialization Literal URI") {
      val sw =
        SWDiscovery( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .set(Literal("bb"))

      SerializationBuilder.deserialize(SerializationBuilder.serialize(sw)) == sw
    }

    test("serialization ListValue URI") {
      val sw =
        SWDiscovery( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .setList(Seq(URI("bb")))

      SerializationBuilder.deserialize(SerializationBuilder.serialize(sw)) == sw
    }

    test("serialization graph") {
      val sw =
        SWDiscovery( DataTestFactory.getConfigVirtuoso1())
          .graph("h")
          .something ("h1" )
          .setList(Seq(URI("bb")))

      SerializationBuilder.deserialize(SerializationBuilder.serialize(sw)) == sw
    }

    test("serialization filter") {
      val sw =
        SWDiscovery( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .isLinkTo(URI("bb"))
          .filter.not.contains("filter")

      SerializationBuilder.deserialize(SerializationBuilder.serialize(sw)) == sw
    }

    test("serialization datatype") {
      val sw =
        SWDiscovery( DataTestFactory.getConfigVirtuoso1())
          .something ("h1" )
          .isLinkTo(URI("bb"))
          .datatype(URI("some"),"v")

      SerializationBuilder.deserialize(SerializationBuilder.serialize(sw)) == sw
    }


  }
}
