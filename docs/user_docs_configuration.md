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

each source defined un identifier.

- `id`       : `<identifyTheSourceEndpoint>`

::: tip Titre
Three types of RDF sources can be exploited : SPARQL endpoint, RDF Files, RDF inlining content
:::

### SPARQL endpoint

- `mimetype  = "application/sparql-query"`
- `url`      =  `<String>`

optional argument
- `method`    = `"POST" | "POST_ENCODED" | "GET"`


#### authentication

- `auth`            = `"basic" | "digest" | "bearer" | "proxy"`
- `login`           = `<login>`
- `password`        = `<password>`

##### example

```json
{
   "id"       : "dbpedia",
   "url"      : "https://dbpedia.org/sparql",
   "mimetype" : "application/sparql-query",
   "method"   : "POST"
 }
```

#### RDF files
- `mimetype`  = `"text/turtle" | "text/n3" | "text/rdf-xml"`
- `file`   = `<String>`

##### example

```json
{
   "id"       : "local_content",
   "mimetype" : "text/turtle",
   "file"     : "https://raw.githubusercontent.com/p2m2/discovery/develop/shared/src/test/resources/metabo.ttl"
 } 
```

#### RDF inline content
- `mimetype`  = `"text/turtle" | "text/n3" | "text/rdf-xml"`
- `content`   = `<String>`

##### example

```json
{
   "id"       : "local_content",
   "mimetype" : "text/turtle",
   "content"  : "<http://example.org/#subject1> <http://example.org/#predicate1> <http://example.org/#object1> ."
 } 
```

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