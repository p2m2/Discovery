# Discovery

[![p2m2](https://circleci.com/gh/p2m2/Discovery.svg?style=shield)](https://app.circleci.com/pipelines/github/p2m2)
[![codecov](https://codecov.io/gh/p2m2/Discovery/branch/develop/graph/badge.svg)](https://codecov.io/gh/p2m2/Discovery)
[![CodeFactor](https://www.codefactor.io/repository/github/p2m2/discovery/badge)](https://www.codefactor.io/repository/github/p2m2/discovery)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/8d8ecb66f9ff4963a22efab3c693b629)](https://www.codacy.com/gh/p2m2/Discovery/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=p2m2/Discovery&amp;utm_campaign=Badge_Grade)


- easy sparql query construct using a simple editor and a web browser
- display rich information on the web page or console 
- offers building blocks to facilitate queries


### Html/Js example

#### Html import 

```html 
<script type="text/javascript" src="https://cdn.jsdelivr.net/gh/p2m2/discovery@develop/dist/discovery-web.min.js"> </script> 
<script>
      let config = SWDiscoveryConfiguration.setConfigString(`
          {
          "sources" : [{
          "id"  : "peakforest",
          "url" : "https://peakforest.semantic-metabolomics.fr/sparql"
           }]}
          `)
        let r = SWDiscovery(config)
                     .prefix("peak_class","https://metabohub.peakforest.org/ontology/class#")
                     .prefix("peak_prop","https://metabohub.peakforest.org/ontology/property#")
                     .root()
                      .something("i")
                        .setList(18,19,20)
                     .root()
                       .something("m1")
                         .isA("peak_class:Compound")
                         .datatype("rdfs:label","label1")
                         .isSubjectOf("peak_prop:InChIKey","inchikey1")
                           .bind("block1").subStr(0,"?i")
                     .root()
                       .something("m2")
                         .filter.notEqual("?m1")
                         .isA("peak_class:Compound")
                         .datatype("rdfs:label","label2")
                         .isSubjectOf("peak_prop:InChIKey","inchikey2")
                           .bind("block2").subStr(0,"?i")
                           .filter.equal("?block1")
                      .focus("m1")
                        .console() // display information on the console
                        .helper("metabo") // display information on the web page and propose new building block.
                     .select("m1","m2","label1","label2","block1","block2")
                     .limit(10);
      
       
        r.commit().raw().then((response) => {
          
          for (let i=0;i<response.results.bindings.length;i++) {
            let m1 =response.results.bindings[i]["m1"].value;
            let m2 =response.results.bindings[i]["m2"].value;
            /* decorations/datatype properties management */ 
            let label1=response.results.datatypes["label1"][m1][0].value; // all studies with all languages, here we take the first one arbitrarily.
            let label2=response.results.datatypes["label2"][m2][0].value;
            
            let b1 =response.results.bindings[i]["block1"].value;
            let b2 =response.results.bindings[i]["block2"].value;

            console.log(label1 + "-" + label2 + " block1:"+b1 + " block2:"+b2 );
          }

        }).catch( (error) => {
          console.error(" -- catch exception --")
          console.error(error)
        } );

    </script>
 ```


[js fiddle example](https://jsfiddle.net/ofilangi/3xkay1f6/10/)

#### Node import 

##### dependencies 

### html examples

[discovery-tutorial-html-js](https://github.com/p2m2/discovery-tutorial-html-js)

### NodeJs examples

[discovery-tutorial-nodejs](https://github.com/p2m2/discovery-tutorial-nodejs)

### Scala examples 

full example with [table view implementation](https://github.com/p2m2/discovery-table-view) using [scalatags](https://github.com/lihaoyi/scalatags)
