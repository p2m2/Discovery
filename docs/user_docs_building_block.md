# Building query

To build a query, user successively add **unit blocks** grouped into five categories :

- Query configuration 
- Values assignment block 
- Linking RDF block
- Filter assignment block  
- Creation/Transformation assignment block

Each unit block could increment the query at different location called **focus**. 
The query contains a current focus which is moved each time a unit block is added.
With some exceptions, the current focus is positioned on the last increment.

**example**

```scala
query.something() 
        .isA("peak_class:Compound")
           .isSubjectOf("peak_prop:InChIKey")
```


This request can be translated as "Get something of type compound which are subject of the InChIKey predicate."


## Moving the focus

- *focus()* : get current focus
- *focus(`var`)* : set the focus with named block `var` 
- *root()* : go back to the query root 

## Using named block

- Some unit block can be named. You named block if you want move to this focus later or to get results about this 
  unit block at the executing time. 

## Special unit block "Something"

A query always start this unit block.

- something(`var` = null)

## Query configuration

- prefix(`short`,`long`) : set prefix to avoid long uri during construction
- graph(`iri`)           : set graph or graph named
- namedGraph(`iri`)      : set graph or graph named

## Linking RDF block to browse the semantic graph

- isSubjectOf(`uri`,`var`) 
- isObjectOf(`uri`,`var`)
- isLinkFrom(`uri`,`var`)
- isLinkTo(`uri`,`var`)

**following block unit don't move focus**

- isA(`uri`)  : set the focus type/class
- datatype(`uri`,`var`) : focus is a subject of a triplet which `uri` is a datatype property (OWL)


## Values assignment block

- set(`sparqlDef`)
- setList(`sparqlDef1`,`sparqlDef2`,..)

## Filter assignment block

- filter.not.{fun}
- filter.isLiteral
- filter.isUri
- filter.isBlank
- filter.regex( pattern:`literal|var`, flags : `literal|var` )
- filter.contains( `literal|var` )
- filter.strStarts( `literal|var` )
- filter.strEnds( `literal|var` )
- filter.equal( `literal|var` )
- filter.notEqual( `literal|var` )
- filter.inf( `literal|var` )
- filter.infEqual( `literal|var` )
- filter.sup( `literal|var` )
- filter.supEqual( `literal|var` )

## Creation/Transformation assignment block

- .bind(`var`).subStr(startingLoc : SparqlDefinition,length : SparqlDefinition )
- .bind(`var`).replace(pattern : SparqlDefinition, replacement : SparqlDefinition, flags : SparqlDefinition="")
- .bind(`var`).abs()
- .bind(`var`).ceil()
- .bind(`var`).floor()
- .bind(`var`).rand()
