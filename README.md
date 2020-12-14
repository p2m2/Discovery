# Discovery

[![p2m2](https://circleci.com/gh/p2m2/Discovery.svg?style=shield)](https://app.circleci.com/pipelines/github/p2m2)
[![codecov](https://codecov.io/gh/p2m2/Discovery/branch/develop/graph/badge.svg)](https://codecov.io/gh/p2m2/Discovery)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/8d8ecb66f9ff4963a22efab3c693b629)](https://www.codacy.com/gh/p2m2/Discovery/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=p2m2/Discovery&amp;utm_campaign=Badge_Grade)

## Compil

```
sbt discoveryJVM/run
sbt discoveryJVM/test
```

## commands
```
npm install jsdom
sbt discoveryJS/run
sbt discoveryJS/test
sbt discoveryJS/fastOptJS
sbt discoveryJS/fullOptJS
sbt discoveryJS/fastOptJS::webpack => bundle package
```

## note dev

```
sbt compile
sbt run   # run web app http://localhost:9000
sbt package
sbt test
sbt fastOptJS => generer le JS
sbt discoveryJVM/testOnly inrae.semantic_web.QueryPlannerTest
```

Vscode support : Metals plugin
npm install source-map-support

### Js example
```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Count</title>
</head>
<body>
<script>var exports = {"__esModule": true};</script>
<script type="text/javascript" src="https://cdn.jsdelivr.net/gh/p2m2/Discovery@develop/ext/discovery-opt.js"></script>
<pre id="demo"></pre>
</body>
<script>
    var EasySparqlStatementConfiguration = exports.EasySparqlStatementConfiguration ;
    var EasySparqlEngine = exports.EasySparqlEngine ;
    var URI = exports.URI ;

      let config = new EasySparqlStatementConfiguration()

      config.setConfigString(`
          {
          "sources" : [{
          "id"  : "metabohub",
          "url" : "http://endpoint-metabolomics.ara.inrae.fr/chembl/sparql/",
          "typ" : "tps",
          "method" : "POST",
          "mimetype" : "json"
           }]}
          `)
        let query = new EasySparqlEngine(config);

        console.log(query)
        let r = query.something("h1")
                     .isSubjectOf(URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                     .debug()
                     .count() ;

        r.then((value) => {
          let res = value;
          console.log(res);
          document.getElementById("demo").appendChild(document.createTextNode(res));
        }).catch( (error) => {
          console.error(" -- catch exception --")
          console.error(error)
        } );
    </script>
</html>
```

### NodeJs example
```nodejs
var EasySparqlStatementConfiguration = require('.discovery-opt.js').EasySparqlStatementConfiguration
var EasySparqlEngine = require('discovery-opt.js').EasySparqlEngine
var URI = require('discovery-opt.js').URI

let config = new EasySparqlStatementConfiguration()
      .setConfigString(`
          {
          "sources" : [{
                    "id"  : "dbpedia",
                    "url" : "https://dbpedia.org/sparql",
                    "typ" : "tps",
                    "method" : "POST"
           }]}
          `)

        let query = new EasySparqlEngine(config);

        let r = query.something("h1")
                     .isSubjectOf(URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                     .count() ;

        r.then((value) => {
          let res = value;
          console.log(res);
        }).catch( (error) => {
          console.error(" -- catch exception --")
          console.error(error)
        } );
```

### Scala Exemple 

```scala 

object GeoSparql {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val configTest: StatementConfiguration = StatementConfiguration()
    .setConfigString(
    """
      {
                "sources" : [{
                "id"  : "geosparql",
                "url" : "https://www.navigae.fr/repositories/Navigae",
                "type" : "tps",
                "method" : "POST",
                "mimetype" : "json"
                 }]}
      """.stripMargin)

  def main(args: Array[String]): Unit = {
      val query = SW(configTest)

      Future {
        query.something("instance")
          .isSubjectOf(URI("a"))
          .set(URI("https://www.navigae.fr/ontology#AerialPhoto"))
          .focus("instance")
          .isSubjectOf(URI("http://navigae.fr/ontology#hasPolygonGeometry"))
          .isSubjectOf(URI("http://www.opengis.net/ont/geosparql#asWKT"), "geometry")
          .focus("instance")
          .datatype(URI("label", "rdfs"), "label")
          .datatype(URI("http://purl.org/dc/terms/isPartOf"), "dbpedia")
          .select(List("label", "geometry","dbpedia"))
          .onComplete {
            case Success(response) => {
              println("RESULTATS ==============================")
              println(response("results")("datatypes")("label"))
              println(response("results")("datatypes")("dbpedia"))
              assert(true)
            }
            case Failure(exception) => println(exception); assert(false)
          }
      }
  }
}

```