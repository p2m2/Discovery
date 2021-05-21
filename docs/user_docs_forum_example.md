


```html
<script
        type="text/javascript"
        src="https://cdn.jsdelivr.net/gh/p2m2/discovery@0.2.0/dist/discovery-web.min.js">
</script>


<script>

/* -------------------------------------*/
/* Part 1. Setting Forum Configuration  */
/* -------------------------------------*/
let config = SWDiscoveryConfiguration.setConfigString(`
          {
          "sources" : [{
          "id"  : "forum",
          "url" : "https://forum.semantic-metabolomics.fr/sparql/"
           }]}
          `) ;
          
/* ------------------------------------------------------------------------*/
/* Part 2. Building Request  "cid:CID336986 compound is link to a mesh term" */
/* ------------------------------------------------------------------------*/
 var req = SWDiscovery(config)
             .prefix("skos","http://www.w3.org/2004/02/skos/core#")
             .graph("https://forum.semantic-metabolomics.org/EnrichmentAnalysis/CID_MESH/2020")
             .graph("https://forum.semantic-metabolomics.org/EnrichmentAnalysis/CHEBI_MESH/2020")
             .graph("https://forum.semantic-metabolomics.org/EnrichmentAnalysis/CHEMONT_MESH/2020")
             .prefix("cid","http://rdf.ncbi.nlm.nih.gov/pubchem/compound/")
              .something("compound")
                 .set("cid:CID6036")
                   .isSubjectOf("skos:related","meshTerm")
                     .datatype("rdfs:label","labelMesh")
/* -----------------------------------------------*/
/* Part 3. Executing request to get list of Album */
/* -----------------------------------------------*/
   let list_transactions = req.selectByPage("meshTerm","labelMesh")
   list_transactions.then( (args) => {
     let numberOfPages = Object.values(args)[0] ;
     let lazyPages = Object.values(args)[1] ;

     console.log(numberOfPages);
     document.body.innerHTML += "<p>cid:CID33698 link to :</p>";
document.body.innerHTML +="<ul>" ;
     lazyPages[0].commit()
           .raw()
             .then((response) => {
                  for (let i=0;i<response.results.bindings.length;i++) {
                    let meshId = response.results.bindings[i]["meshTerm"].value;
                    document.body.innerHTML += "<li>" + response.results.datatypes["labelMesh"][meshId][0].value + "</li>" ;
                
                  }
               });
      document.body.innerHTML +="</ul>" ;
   });
```

