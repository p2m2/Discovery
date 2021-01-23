package inrae.semantic_web

import inrae.semantic_web.QueryPlanner.factorize
import inrae.semantic_web.internal.{Root, Something, SubjectOf, UnionBlock}
import inrae.semantic_web.rdf.URI
import inrae.semantic_web.sparql.{BgpGroupe, OrGroupe}
import utest._

object QueryPlannerTest extends TestSuite {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def tests = Tests {
    /*
    */
    test("factorize stability AND") {
      val s1 = Something("s1")

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

      val plan = QueryPlanner.buildPlanning(
        r.addChildren(
            s1.addChildren(s2)
              .addChildren(s3)))

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
      val u = UnionBlock(s1)
      val s2 = SubjectOf("s2",new URI("uri2"))
      val s3 = SubjectOf("s3",new URI("uri3"))

      val pTest = r.addChildren(s1.addChildren(u.addChildren(s2).addChildren(s3)))

      val plan = QueryPlanner.buildPlanning(pTest)

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
      val u = UnionBlock(s1)

      val s2 = SubjectOf("s2",new URI("uri2"))
      val s3 = SubjectOf("s3",new URI("uri3"))
      val s4 = SubjectOf("s4",new URI("uri4"))
      val s5 = SubjectOf("s5",new URI("uri5"))

      val pTest = r.addChildren(s1.addChildren(
        u.addChildren(s2)
          .addChildren(s3)
          .addChildren(s4)
          .addChildren(s5)))

      val plan = QueryPlanner.buildPlanning(pTest)
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
      val u1 = UnionBlock(s1)
      val u2 = UnionBlock(s1)

      val s2 = SubjectOf("s2",URI("uri2"))
      val s3 = SubjectOf("s3",URI("uri3"))
      val s4 = SubjectOf("s4",URI("uri4"))
      val s5 = SubjectOf("s5",URI("uri5"))


      val pTest = r.addChildren(s1
        .addChildren(u1.addChildren(s2).addChildren(s3))
        .addChildren(u2.addChildren(s4).addChildren(s5)))


      val plan = QueryPlanner.buildPlanning(pTest)
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
      val pTest = r.addChildren(
        s1.addChildren(s2).addChildren(s3).addChildren(s4).addChildren(s5)
      )

      val plan = QueryPlanner.buildPlanning(pTest)
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

      val pTest = r.addChildren(
        s1.addChildren(s2)
          .addChildren(
            s3.addChildren(s4)
              .addChildren(s5)))

      val plan = QueryPlanner.buildPlanning(pTest)
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

    test("AND4") {
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

      val pTest = r.addChildren(
        s1.addChildren(s2)
          .addChildren(
            s3.addChildren(
              s4.addChildren(s6).addChildren(s7))
              .addChildren(
                s5.addChildren(
                  s8.addChildren(s10).addChildren(s11))
                  .addChildren(s9))))

      val plan = QueryPlanner.buildPlanning(pTest)

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

      val pTest = r.addChildren(
        s1.addChildren(s2)
          .addChildren(s3)
          .addChildren(
            u.addChildren(s4)
              .addChildren(s5))
      )

      val plan = QueryPlanner.buildPlanning(pTest)

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
      val u1 = UnionBlock(s1)
      val u2 = UnionBlock(s3)

      val pTest= r.addChildren(
        s1.addChildren(
          u1.addChildren(s2).addChildren(
            s3.addChildren(
              u2.addChildren(s4)
                .addChildren(s5)))))

      val plan = QueryPlanner.buildPlanning(pTest)
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
    /*
    test("AND - Check variable .1")  {
      val r : Root = Root()
      val s1 = Something("s1")
      val s2 = SubjectOf("s2",new URI("uri2"))
      val r2 = r.addChildren(s1)
      val s3 = s1.addChildren(s2)
      val plan = QueryPlanner.buildPlanning(r)

      assert(
        plan == BgpGroupe(List(s1,s2))
      )
      val r3 = r.addSourceNode(SourcesNode(s1.reference(),List("etp1")))
                .addSourceNode(SourcesNode(s2.reference(),List("etp1")))


      val y = QueryPlanner.ordonnanceBySource(plan,r3)

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
*/
  }
}
