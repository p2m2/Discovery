package inrae.semantic_web

import inrae.semantic_web.QueryPlanner.factorize
import inrae.semantic_web.rdf.URI
import inrae.semantic_web.sparql.{BgpGroupe,OrGroupe,AndGroupe}
import utest._
import inrae.semantic_web.internal.{LinkFrom, LinkTo, Node, ObjectOf, Root, Something, SourcesNode, SubjectOf, UnionBlock, Value}

import scala.util.{Failure, Success}

object QueryPlannerTest extends TestSuite {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def tests = Tests {
    /*
    */
    test("factorize stability AND") {
      val s1 = Something("s1")
      val u = UnionBlock(s1)
      val s2 = SubjectOf("s2",new URI("uri2"))
      val s3 = SubjectOf("s3",new URI("uri3"))
      val s4 = SubjectOf("s4",new URI("uri4"))
      val logic = (BgpGroupe(List(s1,s2,s3,s4)))
      //println(logic)
      assert(factorize(logic) == logic)
    }
    /*
    */
    test("factorize stability OR") {
      val s1 = Something("s1")
      val u = new UnionBlock(s1)
      val s2 = SubjectOf("s2",new URI("uri2"))
      val s3 = SubjectOf("s3",new URI("uri3"))
      val logic = OrGroupe(
        List(BgpGroupe(List(s1,s2)),
          BgpGroupe(List(s1,s3))))

      assert(factorize(logic) == logic)
    }

    /*
                 S1
                 AND
               S2  S3

               [S1 S2 S3]
     */

        test("AND")  {
          val r : Root = Root()
          val s1 = Something("s1")
          val s2 = SubjectOf("s2",new URI("uri2"))
          val s3 = SubjectOf("s3",new URI("uri3"))
          r.addChildren(s1)
          s1.addChildren(s2)
          s1.addChildren(s3)
          val plan = QueryPlanner.buildPlanning(r)
        //  println(plan)
          assert(
            (plan == BgpGroupe(List(s1,s2,s3)) )
          )
        }

        /*
                S1
                 OR
              S2    S3
        */

        test("OR")  {
          val r : Root = Root()
          val s1 = Something("s1")
          val u = new UnionBlock(s1)
          s1.addChildren(u)
          val s2 = SubjectOf("s2",new URI("uri2"))
          val s3 = SubjectOf("s3",new URI("uri3"))
          r.addChildren(s1)
          u.addChildren(s2)
          u.addChildren(s3)
          val plan = QueryPlanner.buildPlanning(r)
         // println(plan)
          assert(
              (plan == OrGroupe(
                List(BgpGroupe(List(s1,s2)),
                  BgpGroupe(List(s1,s3)))))
          )
        }

        /*
               S1
                OR
             S2 S3 S4 S5
        */

            test("OR2")  {
              val r : Root = Root()
              val s1 = Something("s1")
              val u = new UnionBlock(s1)
              s1.addChildren(u)

              val s2 = SubjectOf("s2",new URI("uri2"))
              val s3 = SubjectOf("s3",new URI("uri3"))
              val s4 = SubjectOf("s4",new URI("uri4"))
              val s5 = SubjectOf("s5",new URI("uri5"))

              r.addChildren(s1)
              u.addChildren(s2)
              u.addChildren(s3)
              u.addChildren(s4)
              u.addChildren(s5)

              val plan = QueryPlanner.buildPlanning(r)
              assert(
                plan == OrGroupe(
                  List(BgpGroupe(List(s1,s2)),
                       BgpGroupe(List(s1,s3)),
                       BgpGroupe(List(s1,s4)),
                       BgpGroupe(List(s1,s5))))
              )
            }

            /*
                   S1
                 OR  OR
               S2 S3  S4 S5
            */

            test("OR2_2")  {
              val r : Root = Root()
              val s1 = Something("s1")
              val u1 = new UnionBlock(s1)
              val u2 = new UnionBlock(s1)
              s1.addChildren(u1)
              s1.addChildren(u2)

              val s2 = SubjectOf("s2",new URI("uri2"))
              val s3 = SubjectOf("s3",new URI("uri3"))
              val s4 = SubjectOf("s4",new URI("uri4"))
              val s5 = SubjectOf("s5",new URI("uri5"))

              r.addChildren(s1)
              u1.addChildren(s2)
              u1.addChildren(s3)
              u2.addChildren(s4)
              u2.addChildren(s5)

              val plan = QueryPlanner.buildPlanning(r)
             // println(plan)
              assert(
                plan == OrGroupe(
                  List(BgpGroupe(List(s1,s2)),
                    BgpGroupe(List(s1,s3)),
                    BgpGroupe(List(s1,s4)),
                    BgpGroupe(List(s1,s5))))
              )
            }

            /*
                  S1
                 AND
               S2 S3 S4 S5

             */

            test("AND2")  {
              val r : Root = Root()
              val s1 = Something("s1")
              val s2 = SubjectOf("s2",new URI("uri2"))
              val s3 = SubjectOf("s3",new URI("uri3"))
              val s4 = SubjectOf("s4",new URI("uri4"))
              val s5 = SubjectOf("s5",new URI("uri5"))
              r.addChildren(s1)
              s1.addChildren(s2)
              s1.addChildren(s3)
              s1.addChildren(s4)
              s1.addChildren(s5)
              val plan = QueryPlanner.buildPlanning(r)
              assert(
                plan == BgpGroupe(List(s1,s2,s3,s4,s5))
              )
            }

                /*
                     S1
                    AND
                  S2 S3
                     AND
                    S4 S5

                */

                test("AND3")  {
                  val r : Root = Root()
                  val s1 = Something("s1")
                  val s2 = SubjectOf("s2",new URI("uri2"))
                  val s3 = SubjectOf("s3",new URI("uri3"))
                  val s4 = SubjectOf("s4",new URI("uri4"))
                  val s5 = SubjectOf("s5",new URI("uri5"))
                  r.addChildren(s1)
                  s1.addChildren(s2)
                  s1.addChildren(s3)
                  s3.addChildren(s4)
                  s3.addChildren(s5)
                  val plan = QueryPlanner.buildPlanning(r)
                  assert(
                    plan == BgpGroupe(List(s1,s2,s3,s4,s5))
                  )
                }

                  /*
                       S1
                      AND
                    S2 S3
                       AND
                      S4 S5
                     AND  AND
                  S6 S7   S8 S9
                          AND
                        S10 S11
                  */

                  test("AND3") {
                    val r : Root = Root()
                    val s1 = Something("s1")
                    val s2 = SubjectOf("s2",new URI("uri2"))
                    val s3 = SubjectOf("s3",new URI("uri3"))
                    val s4 = SubjectOf("s4",new URI("uri4"))
                    val s5 = SubjectOf("s5",new URI("uri5"))
                    val s6 = SubjectOf("s6",new URI("uri6"))
                    val s7 = SubjectOf("s7",new URI("uri7"))
                    val s8 = SubjectOf("s8",new URI("uri8"))
                    val s9 = SubjectOf("s9",new URI("uri9"))
                    val s10 = SubjectOf("s10",new URI("uri10"))
                    val s11 = SubjectOf("s11",new URI("uri11"))
                    r.addChildren(s1)
                    s1.addChildren(s2)
                    s1.addChildren(s3)
                    s3.addChildren(s4)
                    s3.addChildren(s5)
                    s4.addChildren(s6)
                    s4.addChildren(s7)
                    s5.addChildren(s8)
                    s5.addChildren(s9)
                    s8.addChildren(s10)
                    s8.addChildren(s11)
                    val plan = QueryPlanner.buildPlanning(r)
                   // println(plan)
                    assert(
                      plan == BgpGroupe(List(s1,s2,s3,s4,s6,s7,s5,s8,s10,s11,s9))
                    )
                  }

                     /*
                          S1
                         AND
                       S2 S3  OR
                             S4 S5

                     */

                     test("AND3_OR")  {
                       val r : Root = Root()
                       val s1 = Something("s1")
                       val s2 = SubjectOf("s2",new URI("uri2"))
                       val s3 = SubjectOf("s3",new URI("uri3"))
                       val s4 = SubjectOf("s4",new URI("uri4"))
                       val s5 = SubjectOf("s5",new URI("uri5"))
                       val u = UnionBlock(s1)
                       r.addChildren(s1)
                       s1.addChildren(s2)
                       s1.addChildren(s3)
                       s1.addChildren(u)
                       u.addChildren(s4)
                       u.addChildren(s5)
                       val plan = QueryPlanner.buildPlanning(r)
                      // println(plan)
                       assert(
                           plan ==
                             OrGroupe(
                               List(BgpGroupe(List(s1,s2,s3,s4)),BgpGroupe(List(s1,s2,s3,s5))))
                       )
                     }

                     /*
                         S1
                        OR
                      S2 S3
                         OR
                        S4 S5

                    */

                     test("OR1_OR2")  {
                       val r : Root = Root()
                       val s1 = Something("s1")
                       val s2 = SubjectOf("s2",new URI("uri2"))
                       val s3 = SubjectOf("s3",new URI("uri3"))
                       val s4 = SubjectOf("s4",new URI("uri4"))
                       val s5 = SubjectOf("s5",new URI("uri5"))
                       val u1 = new UnionBlock(s1)
                       val u2 = new UnionBlock(s3)
                       r.addChildren(s1)
                       s1.addChildren(u1)
                       u1.addChildren(s2)
                       u1.addChildren(s3)

                       s3.addChildren(u2)
                       u2.addChildren(s4)
                       u2.addChildren(s5)

                       val plan = QueryPlanner.buildPlanning(r)
                       assert(
                         plan ==
                           OrGroupe(List(
                             BgpGroupe(List(s1,s2)),
                               BgpGroupe(List(s1,s3,s4)),
                               BgpGroupe(List(s1,s3,s5))
                             )
                           )
                       )
                     }

                     /*
                                  S1
                                  S2
                                1 etp
                      */
                     test("AND - Check variable .1")  {
                       val r : Root = Root()
                       val s1 = Something("s1")
                       val s2 = SubjectOf("s2",new URI("uri2"))
                       r.addChildren(s1)
                       s1.addChildren(s2)
                       val plan = QueryPlanner.buildPlanning(r)

                       assert(
                         plan == BgpGroupe(List(s1,s2))
                       )

                       r.lSourcesNodes = r.lSourcesNodes ++ List(new SourcesNode(s1.reference(),List("etp1")))
                       r.lSourcesNodes = r.lSourcesNodes ++ List(new SourcesNode(s2.reference(),List("etp1")))

                       val y = QueryPlanner.ordonnanceBySource(plan,r)

                       assert(
                         y == QueryPlanner.INTERSECTION_RESULTS_SET(
                           Map( "etp1" -> List(s1,s2))
                         )
                       )
                     }

                     /*
                                  S1
                                  S2
                                2 etp
                      */
                     test("AND - Check variable .2")  {
                       val r : Root = Root()
                       val s1 = Something("s1")
                       val s2 = SubjectOf("s2",new URI("uri2"))
                       r.addChildren(s1)
                       s1.addChildren(s2)
                       val plan = QueryPlanner.buildPlanning(r)

                       assert(
                         plan == BgpGroupe(List(s1,s2))
                       )

                       r.lSourcesNodes = r.lSourcesNodes ++ List(new SourcesNode(s1.reference(),List("etp1","etp2")))
                       r.lSourcesNodes = r.lSourcesNodes ++ List(new SourcesNode(s2.reference(),List("etp1","etp2")))

                       val y = QueryPlanner.ordonnanceBySource(plan,r)

                       assert(
                         y == QueryPlanner.INTERSECTION_RESULTS_SET(
                           Map( "etp1" -> List(s1,s2) ,  "etp2" -> List(s1,s2))
                         )
                       )
                     }

                     /*
                                      S1
                                      S2
                                    2 etp on S1
                                    1 etp on S2
                          */
                     test("AND - Check variable .3")  {
                       val r : Root = Root()
                       val s1 = Something("s1")
                       val s2 = SubjectOf("s2",new URI("uri2"))
                       r.addChildren(s1)
                       s1.addChildren(s2)
                       val plan = QueryPlanner.buildPlanning(r)

                       assert(
                         plan == BgpGroupe(List(s1,s2))
                       )

                       r.lSourcesNodes = r.lSourcesNodes ++ List(new SourcesNode(s1.reference(),List("etp1","etp2")))
                       r.lSourcesNodes = r.lSourcesNodes ++ List(new SourcesNode(s2.reference(),List("etp1")))

                       val y = QueryPlanner.ordonnanceBySource(plan,r)

                       assert(
                         y == QueryPlanner.INTERSECTION_RESULTS_SET(
                           Map( "etp1" -> List(s1,s2) ,  "etp2" -> List(s1))
                         )
                       )
                     }

                     /*
                                      S1
                                      S2
                                    2 etp // on S1,S2
                          */
                     test("AND - Check variable .4")  {
                       val r : Root = Root()
                       val s1 = Something("s1")
                       val s2 = SubjectOf("s2",new URI("uri2"))
                       r.addChildren(s1)
                       s1.addChildren(s2)
                       val plan = QueryPlanner.buildPlanning(r)

                       assert(
                         plan == BgpGroupe(List(s1,s2))
                       )

                       r.lSourcesNodes = r.lSourcesNodes ++ List(new SourcesNode(s1.reference(),List("etp1")))
                       r.lSourcesNodes = r.lSourcesNodes ++ List(new SourcesNode(s2.reference(),List("etp2")))

                       val y = QueryPlanner.ordonnanceBySource(plan,r)

                       assert(
                         y == QueryPlanner.INTERSECTION_RESULTS_SET(
                           Map( "etp1" -> List(s1) ,  "etp2" -> List(s2))
                         )
                       )
                     }

                     /*
                                      S1
                                    S2 S3 S4
                                   S2 -> ETP1
                                   S3 -> ETP2
                                   S4 -> ETP1,ETP2
                          */
                     test("AND - Check variable .5")  {
                       val r : Root = Root()
                       val s1 = Something("s1")
                       val s2 = SubjectOf("s2",new URI("uri2"))
                       val s3 = SubjectOf("s3",new URI("uri3"))
                       val s4 = SubjectOf("s4",new URI("uri4"))

                       r.addChildren(s1)
                       s1.addChildren(s2)
                       s1.addChildren(s3)
                       s1.addChildren(s4)

                       val plan = QueryPlanner.buildPlanning(r)

                       assert(
                         plan == BgpGroupe(List(s1,s2,s3,s4))
                       )

                       r.lSourcesNodes = r.lSourcesNodes ++ List(new SourcesNode(s1.reference(),List("etp1","etp2")))
                       r.lSourcesNodes = r.lSourcesNodes ++ List(new SourcesNode(s2.reference(),List("etp1")))
                       r.lSourcesNodes = r.lSourcesNodes ++ List(new SourcesNode(s3.reference(),List("etp2")))
                       r.lSourcesNodes = r.lSourcesNodes ++ List(new SourcesNode(s4.reference(),List("etp1","etp2")))

                       val y = QueryPlanner.ordonnanceBySource(plan,r)

                       assert(
                         y == QueryPlanner.INTERSECTION_RESULTS_SET(
                           Map( "etp1" -> List(s1,s2,s4) ,  "etp2" -> List(s1,s3,s4))
                         )
                       )
                     }


                     /*
                           S1
                            OR
                         S2 S3 S4 S5

                         S2,S4 -> etp1
                         S3    -> etp2
                         S5    -> etp1,etp2
                    */

                     test("OR - Check variable .1")  {
                       val r : Root = Root()
                       val s1 = Something("s1")
                       val u = new UnionBlock(s1)
                       s1.addChildren(u)

                       val s2 = SubjectOf("s2",new URI("uri2"))
                       val s3 = SubjectOf("s3",new URI("uri3"))
                       val s4 = SubjectOf("s4",new URI("uri4"))
                       val s5 = SubjectOf("s5",new URI("uri5"))

                       r.addChildren(s1)
                       u.addChildren(s2)
                       u.addChildren(s3)
                       u.addChildren(s4)
                       u.addChildren(s5)

                       val plan = QueryPlanner.buildPlanning(r)
                       assert(
                         plan == OrGroupe(
                           List(BgpGroupe(List(s1,s2)),
                             BgpGroupe(List(s1,s3)),
                             BgpGroupe(List(s1,s4)),
                             BgpGroupe(List(s1,s5))))
                       )

                       r.lSourcesNodes = r.lSourcesNodes ++ List(new SourcesNode(s1.reference(),List("etp1","etp2")))
                       r.lSourcesNodes = r.lSourcesNodes ++ List(new SourcesNode(s2.reference(),List("etp1")))
                       r.lSourcesNodes = r.lSourcesNodes ++ List(new SourcesNode(s3.reference(),List("etp2")))
                       r.lSourcesNodes = r.lSourcesNodes ++ List(new SourcesNode(s4.reference(),List("etp1")))
                       r.lSourcesNodes = r.lSourcesNodes ++ List(new SourcesNode(s5.reference(),List("etp1","etp2")))

                       val y = QueryPlanner.ordonnanceBySource(plan,r)

                       assert(
                         y == QueryPlanner.OR_RESULTS_SET(List(
                              QueryPlanner.INTERSECTION_RESULTS_SET(
                                     Map( "etp1" -> List(s1,s2) ,  "etp2" -> List(s1))
                              ),
                           QueryPlanner.INTERSECTION_RESULTS_SET(
                                     Map( "etp1" -> List(s1) ,  "etp2" -> List(s1,s3))
                           ),
                           QueryPlanner.INTERSECTION_RESULTS_SET(
                             Map( "etp1" -> List(s1,s4) ,  "etp2" -> List(s1))
                           ),
                           QueryPlanner.INTERSECTION_RESULTS_SET(
                             Map( "etp1" -> List(s1,s5) ,  "etp2" -> List(s1,s5))
                           )
                         )
                       )
                       )
                     }


    /*
    test("Planning test EBI/uniprot") {
      val config: StatementConfiguration = new StatementConfiguration()
      config.setConfigString(
        """
          |{
          | "sources" : [{
          |   "id"  : "ebi",
          |   "url" : "https://www.ebi.ac.uk/rdf/services/sparql",
          |   "typ" : "tps",
          |   "method" : "POST_ENCODED",
          |   "mimetype" : "json"
          | },{
          |   "id"  : "uniprot",
          |   "url" : "https://sparql.uniprot.org/sparql",
          |   "typ" : "tps",
          |   "method" : "POST_ENCODED",
          |   "mimetype" : "json"
          | }]}
          |""".stripMargin)
      val query = new SW(config)

      query.something("h1")
        .prefix("ensembl","http://rdf.ebi.ac.uk/resource/ensembl/")
        .prefix("ensemblterms","http://rdf.ebi.ac.uk/terms/ensembl/")
        .prefix("dc","http://purl.org/dc/elements/1.1/")
        .prefix("core","http://purl.uniprot.org/core/")
        .prefix("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#")
        .set(URI("ENSG00000128573","ensembl"))                  // EBI
        .isSubjectOf(URI("DEPENDENT","ensemblterms"))           // EBI
        .isSubjectOf(URI("identifier","dc"))                    // EBI
        .isSubjectOf(URI("sequence","core"))                    // UNIPROT
        .isSubjectOf(URI("value","rdf"))
        .debug
      } */
/*
    test("Wikidata / British Library") {
      val config: StatementConfiguration = new StatementConfiguration()
      config.setConfigString(
        """
          |{
          | "sources" : [{
          |   "id"  : "wikidata",
          |   "url" : "https://query.wikidata.org/sparql",
          |   "typ" : "tps",
          |   "method" : "POST_ENCODED",
          |   "mimetype" : "json"
          | },{
          |   "id"  : "bnb",
          |   "url" : "http://bnb.data.bl.uk/sparql",
          |   "typ" : "tps",
          |   "method" : "GET",
          |   "mimetype" : "json"
          | }]}
          |""".stripMargin)
      val query = new SW(config)
      val s = query
        .prefix("schema","http://schema.org/")
        .prefix("library","http://purl.org/library/")
        .prefix("wdt","http://www.wikidata.org/prop/direct/")
        .prefix("dct","http://purl.org/dc/terms/")
        .something("author")
        .isSubjectOf(URI("P214","wdt"),"creatorID")
        .isObjectOf(URI("P50","wdt"),"work")
        .isSubjectOf(URI("wdt:P1476","wdt"),"workLabel")
        .focus("creatorID")
        .isObjectOf(URI("creator","dct"),"work")
        .isSubjectOf(URI("title","dct"),"work")

        s.select
        .onComplete{
          case Success(value) => {
            s.debug
            println(value)
            assert(true)
          }
          case Failure(exception) => {
            System.err.println(exception)
            assert (false )
          }
        }
    }*/
  }
}