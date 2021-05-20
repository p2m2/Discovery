# Philosophy

discovery allows you to build and execute a request to an RDF resource by simply defining 
a configuration, and a sequence of building blocks atomic element .

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

# Configuration

The configuration have to be defined in a json format. It should a contains a list of "source" definition and a general settings.

```json 
{
     "sources" : [{
       "id"  : "dbpedia",
       "url" : "https://dbpedia.org/sparql",
       "mimetype" : "application/sparql-query",
       "method" : "POST"
     }],
     "settings" : {
       "cache" : true,
       "logLevel" : "info",
       "sizeBatchProcessing" : 10,
       "pageSize" : 10
     }
}
```

## source definition

### mandatory arguments
 - `id`       : `<identifyTheSourceEndpoint>`

### RDF resource 

#### SPARQL endpoint

 - `mimetype  = "application/sparql-query"`
 - `url`      =  `<String>`

##### optional argument
 - `method`    = `"POST" | "POST_ENCODED" | "GET"`

#### request RDF files

#### request RDF content
 - `mimetype`  = `"text/turtle" | "text/n3" | "text/rdf-xml"`
 - `content`   = `<String>`
```json
"sources" : [
{
   "id"       : "local_content",
   "mimetype" : "text/turtle",
   "content"  : "<http://example.org/#subject1> <http://example.org/#predicate1> <http://example.org/#object1> ."
 } 
```

#### authentication

 - `auth`            = `"basic" | "digest" | "bearer" | "proxy"`
 - `login`           = `<login>`
 - `password`        = `<password>`

## General settings

### using cache 
 - `cache`           = `"true" | "false"`

### defining a console log level 
 - `logLevel`        = `"trace" | "debug" | "info" | "warn" | "error" | "all" | "off"`

`"warn"` is the default value.

### customize pool batch processing (see DSL#datatype) 

 - `sizeBatchProcessing`  = `<Int>`
   
150 is the default value.
   
### customize selectByPage

 - `pageSize`             = `<Int>`
   
10 is the default value.
   

## DSL / Building blocks of request atomic element

see [API (Scaladoc)](./api/index.html)

