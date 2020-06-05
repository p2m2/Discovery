package EasySparql

object TutorialApp {
  def main(args: Array[String]): Unit = {
    println("Hello world!")
    val query = new SW()
    val r = query.something("h1")
               .set(URI("http://dbpedia.org/resource/%C3%84lvdalen"))
               .isSubjectOf(URI("http://www.w3.org/2002/07/owl#sameAs"))
               .select()
  }
}


