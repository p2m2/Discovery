# Test
<!-- 
DOC
https://hackmd.io/c/codimd-documentation/%2F%40codimd%2Fmarkdown-syntax-->

## Main functionalities

- Contextual Query Builder
- Easy configuration
- Ontology/Data/Custom Driven
- Attributes management
- Link to/From Object
- RDF Store Federation
- Cache/Memorize
- Server/Client Side
- Build over Jena Apache/Scala.js (IHM optional)


exemple ihm scala.js
https://github.com/lihaoyi/scala-js-games
https://www.scala-js.org/libraries/facades.html
- scala-js-d3
- moment js to manipulate date

Canavas. exemple svelte
https://svelte.dev/repl/79f4f3e0296a403ea988f74d332a7a4a?version=3.12.1

version bootstrap
https://madewithsvelte.com/sveltestrap 

## init

```javascript=
query = SW({
   endpoints : [ "http://...." ],
   request : {
       method   : POST,
       login    : ...,
       password : ...,
   },
   
   prefixes : {
       owl : 'http://www.w3.org/2002/07/owl#',
       foaf: 'http://xmlns.com/foaf/0.1/',
   },
    
   limit : 200,
    
   from : [ [...] ], /* select graph. size should equivalent to "endpoints" attribut */ 
   /* les types heritent de ces classes, peut etre vide en pure data-driven */
   entities : [ 'owl:Class' , 'rdfs:Class'], 
   /* les relations heritent de ces classes, peut etre vide en pure data-driven */ 
   properties : [ 'owl:DatatypeProperty' , 'owl:ObjectProperty' ],
   
   loadOnInit : true/false,/* load all entities/relations at the initialisation and used then with VALUES clause during the browse inside database. not available if entities/properties are empty*/
    
   sampleSeach : true/false, /* apply a sample or not to seach values. depends database*/
    
   applyFilterOnlocal : false, /* Filter is apply RDF store */ 
   decorations : {
      label : [ 
          "<prop1>", 
          {
              endpoint : "http://......",
              property : []
          } , 
          function fallBack(uri) => { /* *something /}]
   }
})
```

### Natives decorations all URI

```
label  (rdfs:label):
type   (rdf:type) : owl:Class, owl:DatatypeProperty, owl:ObjectTypeProperty,...  
description :
```

#### Properties

```
range  :
domain :
```


## Basic type

### Literal

```typescript=
# string
Literal("John") ;
# string xsd with tag
Literal("John",URI("xsd:string"),"fr") ;
# number
Literal("1",URI("xsd:number")) ;
# number
Literal(1) ;
# boolean
Literal("true",URI("xsd:boolean")) ;
# boolean
Literal(true) ;
```

### URI

URI are associated with endpoints. by default, all endpoints

```typescript=
# basic Uri
URI("http://something") ;
URI("xsd:string") ;

# basic Uri only on endpoint
URI("http://something",["endpoint1"]) ;
```

### PropertyPath

```typescript=
# basic Uri
PropertyPath("<http://something>:") ;
```


## build


```typescript=

/* build URI */
URI("http://somthing/val1");
URI("rdf:type");
PropertyPath("<somthing1>/<something2>*");

/* build Literal */
Literal("something",datatype="xsd:string",lang=undefined);

/* starts with something */
query.something(var ref = undefined : string )     ;

/* add ?thing rdf:type <type> */
query.fromType(var type : string | string[], var ref = undefined : string)         ;

/* add ?lastRef <property> ?thing2
 *  Data-driven FILTER(isURI(?things))
 *  Ontology-driven : <property> a owl:ObjectProperty
 * */
query.subjectOf(var property : string | string[], var ref = undefined : string)  ;

/* add ?thing2 <property> ?lastRef */
query.objectOf(var property : string | string[], var ref = undefined : string)   ;

query.attribute(var property)

/* add ?lastRef ?thing2 <object> */
query.linkTo(var object : string | string[], var ref = undefined : string) ;

/* add <subject> ?thing2 ?lastRef  */
query.linkFrom(var subject : string | string[], var ref = undefined : string) ;



/* add VALUES ?thing { } */
query.value(var value : string | string[] );


/* FILTER OPERATION */

query.filter(var operator : string, var refr : string[], var notexist : boolean) ;

/* BINDING
 * 
 * +, -, *, /,  */

query.operator(var operator : string, var refr : string[]);

/* SET operation */

query.union(var queries : string[]) ;

query.minus(var queries : string[]) ;

query.orderBy(var lRefs = string[]) ;

```

## Grammaire


**Query**          ::= Something (**QueryUnit**)0/1 (**AskUnit** | **ActionUnit** )0/1 
**QueryUnit**      ::= **OperationSet** **Query***
**OperationSet**   ::= ( join | union | minus )
**QueryUnit**      ::= ( subjectOf | objectOf | set ) **QueryUnit***
**QueryUnit**      ::= attribute **QueryAttribute***
**QueryUnit**      ::= uriToString **QueryAttribute***
**QueryAttribute** ::= ( filter | set )
**AskUnit**        ::= ( getType | getObjectPropertyFrom | getObjectPropertyOf | getAttributes | select | count )
**ActionUnit**     ::= cancel | sparql | stringify


## queries as a promesse

```javascript=

/* list types of "ref" . if ref undefined get type of last focus variate */
query.getTypes(var ref=undefined : string )  ;

/* list properties which "ref" is subject. if ref undefined get properties of last focus variate */
query.getAttributes(var ref=undefined : string)  ;
query.getObjectPropertiesOf(var ref=undefined : string) ;

/* list properties which "ref" is object. if ref undefined get properties of last focus variate */
query.getObjectPropertiesFrom(var ref=undefined : string) ;

/* list values of "ref". if ref undefined get properties of last focus variate */
query.select(var ref=undefined : string[]) ;

/* count values of "ref". if ref undefined get properties of last focus variate */
query.count(var ref=undefined : string[]) ;

/* cancelation */
query.cancel() ;
```

## Use

```javascript=
/* get reference string */
query.getFocus() ;
    
/* set reference variate */
query.focus(var ref : string) ;

/* export */

query.stringify();

/* import */
query.parse();

```

## exemples

### basic

```javascript=
query.something("ref0").
     .isSubjectOf(URI("prop1"),"ref1")
     .focus()
     .isObjectOf(URI("prop2"),"ref2")
     .select().then( allRef2 => { 
        /*traitement in ref2 */ 
      });

```

```sql=
SELECT ?ref2 WHERE {
?ref0 <prop1> ?ref1.
?ref1 <prop2> ?ref2
}
```

### could querying of ref1 


```javascript=
query.something("ref0").
     .isSubjectOf(URI("prop1"),"ref1")
     .focus()
     .isObjectOf(URI("prop2"),"ref2")
     .select("ref1").then( allRef1 => { 
        /*traitement on ref1 */ 
     });

```

```sql=
SELECT ?ref1 WHERE {
?ref0 <prop1> ?ref1.
?ref1 <prop2> ?ref2
}
```

### starting with value

```javascript=
query.value([Literal("john")]).
     .isObjectOf(URI("foaf:name"))
     .select().then( subjectsWithJohnName => { 
        /*traitement in ref2 */ 
      });

```

```sql=
SELECT ?ref WHERE {
    ?ref foaf:name "john".
}
```

### using propertypath

```javascript=
query.something().
     .isObjectOf(PropertyPath("<http://navigae.fr/ontology#hasPolygonGeometry>/<http://www.opengis.net/ont/geosparql#asWKT>")
     .select().then( subject => { 
        /* everything that are a geometry on https://www.navigae.fr/repositories/Navigae */ 
      });

```

```sql=
SELECT ?ref WHERE {
    ?ref foaf:name "john".
}
```

### get reference variate

```javascript=
query.something("ref0").
     .isSubjectOf(URI("prop1"),"ref1")
     .focus()
     .isObjectOf(URI("prop2"),"ref2")
     .focus()
     .getFocus() ;

>"ref2"
```

### incremental build

```javascript=
let q1 = query.something("ref0").
              .isSubjectOf("<prop1>","ref1")
              .focus();

let q2 = q1.isObjectOf("<prop2>","ref2");

```

### change reference variate

```javascript=
query.something("ref0").
     .isSubjectOf(URI("prop1"),"ref1")
     .focus()
     .isObjectOf(URI("prop2"),"ref2")
     .isObjectOf(URI("prop3"),"ref3")
     .select().then( allRef3 => { 
        /*traitement on ref1 */ 
     });
```

```sparql=
SELECT ?ref3 WHERE {
 ?ref0 <prop1> ?ref1 .
 ?ref1 <prop2> ?ref2 ;
       <prop3> ?ref3 .
}
```

### filtering

```javascript=
query.something("v0").
     .isSubjectOf(URI("prop1"),"p1")
     .something("v1")
     .isSubjectOf(URI("prop2"),"p2")
     .filter(">",["p1","p2"])
     .select(["v0","v1"]).then( ([v0,v1]) => { 
        /*traitement on v0,v1 */ 
     });
```

# Federation

## service or join 

same fonctionnality. service use the service clause. join apply the join of two independantes queries

```javascript=
query1 = SW({ endpoints : ["http://endpoint1/sparql"] });
query2 = SW({ endpoints : ["http://endpoint2/sparql"] });

query1.something("v0").
     .isSubjectOf(URI("prop1"),"p1") ;

query2.something("p1").
     .isSubjectOf(URI("prop2"),"p2")

```
### Create a Service Clause 
```javascript=
/* use clause service or federate (more efficient) */
query1.service(query2).select(['v0','p1','p2']).then(
    [v0,v1,v2] => {... }
) ;

```

### Create a join between two query

```javascript=                
query1.leftjoin(query2).select(['v0','p1','p2']).then(
    [v0,v1,v2] => {... }
) ;
```


### Federation engine

#### Groupement par source endpoint
#### Count de chaque triplet pour faire l'ordonnancement des Groupement par sources

Query -> Groupement par source -> Appliquer le count minimum sur chauqe groupement

=> Ordonnancement d execution : count minimu au count maximum

-> merge de resultats entre chaque execution de groupement


## Cases search


```typescript= 
query1.something("v0")
       .isSubjectOf(URI("metabol:xref")) ; # ChEBI
       .focus()
```

```getPropertiesOf``` should give every properties defined on EBI.


## Cases request

### Finding metabolite link with ChEBI show inchi/hasDbXref 

```typescript= 
query1.something("v0")
       .isSubjectOf(URI("metabol:xref")) ; # ChEBI
       .focus()
       .isSubjectOf(URI("oboInOwl:inchi")) ;
       .isSubjectOf(URI("oboInOwl:hasDbXref")) ;
```


### Finding metabolite link with ChEBI -> ChEMBL

- skos:exactMatch is defined on the EBI endpoint

```typescript= 
query.something("v0")    # Metabolights, local
   .isSubjectOf(URI("metabol:xref")) ; # ChEBI, local
   .focus()
   .isObjectOf(URI("skos:exactMatch")) ; # ChEBI, ChEMBL, EBI
   .focus() # ChEMLB ;
```

### Select all PeakForest metabolites which are « phosphocholines » [ChEBI Chemical entity ontology]


```typescript= 
query.something("v0")
   .isSubjectOf(URI("metabol:xref")) ; # ChEBI
   .focus()
   .isSubjectOf(URI("rdfs:label"),"label")
   .focus()
   .filter("bif:contains",["?label","phosphocholines"]);
```

### Select All Metabolights studies which mention certain PeakForest metabolite


### Select « phosphocholines » metabolites mentioned for blood [Biofluids in MetaboLigths] or identified by Mass spectrometry [Study design in MTBLS]

?Study_1 a n1:Study .
        ?Study_1 n3:technology_type "mass spectrometry"^^xsd:string .
```typescript=         
query.something("v0")
   .isTypeOf(URI("n1:Study"))
   .isSubjectOf("n3:technology_type")
   .focus()
   .value(Literal("mass spectrometry",URI("xsd:string")))
   .focus("v0")
   .isSubjectOf(URI("metabol:xref")) ; # ChEBI
   .focus()
   .isSubjectOf(URI("rdfs:label"),"label")
   .focus()
   .filter("bif:contains",["?label","phosphocholines"]);        
```

### Select all metabolites from PeakForest which are mentioned for Homo sapiens [Specific organism] or mammals [super class] or blood [Biofluids]


## Validate model with svelte/d3.js

https://svelte.dev/  => sparklis like ?
https://d3js.org/    => askomics like ?

## techno

- scala js : https://www.scala-js.org/
- build sparql from tree : https://github.com/RubenVerborgh/SPARQL.js . Verifier si les propertyPath passent...
- n3    : https://github.com/rdfjs/N3.js
- axios : https://github.com/axios/axios
- webpack : packager

- style : https://icons.getbootstrap.com/
- using svg : svg.js : https://svgjs.com/docs/3.0/manipulating/#positioning
- framework svelte : https://svelte.dev/

- keyword wanimation css

# Test Spec

## Construction d un graph local puis requete dessus

L'idée etant d optimiser les temps de reponse en construisant un graph local à partir de plusieurs endpoints et sources de données

```js
SW.dataset.addSource({
                id   : ep1,
                type : ldfragment,
                url : "...."
              })
             .addSource({
                id   : ep2,
                type: endpoint,
                url : "...."
             })
             .addSource({
                id   : ep2,
                type: csv,
                url : "...."
             })
     .construct.bgp(URI("http://s1"),URI("http://s2"),var("val"),[ep1])
               .bgp("http://s1",VAR("prop1"),VAR("val2"),[ep2])
               .filter()
               .values
               .union
               .bgp()
     .query.sparql("select * from ....")
     .query.graphql("....")
     

```





