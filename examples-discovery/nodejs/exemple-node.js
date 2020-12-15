/* eslint no-console: "error" */
/**
Simple request send to DBpedia using NodeJs/Discovery lib.

sbt discoveryJS/fullOptJS
nodejs ./examples-discovery/nodejs/exemple-node.js

*/

var EasySparqlStatementConfiguration = require("../../discovery/js/target/scala-2.13/discovery-opt.js").EasySparqlStatementConfiguration ;
var EasySparqlEngine = require("../../discovery/js/target/scala-2.13/discovery-opt.js").EasySparqlEngine ;
var URI = require("../../discovery/js/target/scala-2.13/discovery-opt.js").URI ;


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
