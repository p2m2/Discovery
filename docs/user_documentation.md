## Philosophy

discovery allows you to build and execute a request to an RDF resource by simply defining 
a configuration, and a construction unit sequence

```html 
/* ---------------------------------------*/
/* Import javascript library the          */
/* ---------------------------------------*/
<script 
        type="text/javascript" 
        src="https://cdn.jsdelivr.net/gh/p2m2/discovery@master/dist/discovery-web.min.js"> 
</script>

/* ---------------------------------------*/
/* Request your favorite RDF resource     */
/* ---------------------------------------*/

<script>
/* ----------------------*/
/* Part 1. Configuration */
/* ----------------------*/
let config = SWDiscoveryConfiguration.setConfigString(`
          {
          "sources" : [{
          "id"  : "dbpedia",
          "url" : "https://dbpedia.org/sparql"
           }]}
          `) ;
/* ----------------------*/
/* Part 2. Build Request */
/* ----------------------*/
 var req = SWDiscovery(config)
             .something("some1")
               isA("http://dbpedia.org/ontology/Album")

/* ------------------------*/
/* Part 3. Execute request */
/* ------------------------*/
   req.select("some1")
               .commit()
               .raw().then((response) => {
                  for (let i=0;i<response.results.bindings.length;i++) {
                    console.log(response.results.bindings[i]["some1"].value);
                  }
               })
</script>
```

## Configuration

 - mimetype
``` 
      "application/sparql-query",
      "text/turtle",
      "text/n3",
      "text/rdf-xml",
      "application/rdf+xml"
```


## DSL

see [API (Scaladoc)](./api/index.html)

