# What is discovery
[![GitHub license](https://img.shields.io/github/license/p2m2/discovery.svg)](https://github.com/p2m2/discovery/blob/master/LICENSE)
[![GitHub release](https://img.shields.io/github/v/release/p2m2/discovery.svg)](https://github.com/p2m2/discovery/releases/)

discovery is a software library which aims to ease the development of decision support tools
exploiting RDF databases.
The library offers a dedicated query language that can be used in several runtime environments (Browser/JS, Node/JS, JVM/Scala).

discovery is developed as part of the work package "Creating FAIR e-resources for knowledge mining" for [the 
national infrastructure for metabolomics and fluxomics - MetaboHUB](https://www.metabohub.fr/home.html) 

## Metabolomic example

This example shows how to retrieve "Metabolights Studies" 
linking to the CHEBI:4167-D-glucopyranose compound. This example uses the following SPARQL endpoint https://metabolights.semantic-metabolomics.fr/sparql


```html
<script 
        type="text/javascript" 
        src="https://cdn.jsdelivr.net/gh/p2m2/discovery@0.2.0/dist/discovery-web.min.js"> 
</script>
<script>
      var config = SWDiscoveryConfiguration.setConfigString(`
          {
          "sources" : [{
          "id"  : "metabolights",
          "url" : "https://metabolights.semantic-metabolomics.fr/sparql"
           }]}
          `);

      SWDiscovery(config)
          .something()
            .set(URI("http://purl.obolibrary.org/obo/CHEBI_4167"))
              .isObjectOf(URI("https://www.ebi.ac.uk/metabolights/property#Xref"),"study")
                .datatype(URI("http://www.w3.org/2000/01/rdf-schema#label"),"label")
          .select("study","label")
               .commit()
               .raw().then((response) => {
          
          for (let i=0;i<response.results.bindings.length;i++) {
            let study=response.results.bindings[i]["study"].value;
            let label=response.results.datatypes["label"][study][0].value; 
            console.log(study+"-->"+label);
          }

        }).catch( (error) => {
          console.error(" -- catch exception --")
          console.error(error)
        } );
</script>
```

## Scala application with discovery

### template

https://github.com/p2m2/discovery-scala-executable

# User documentation
see [User documentation](./user_documentation.md)

# Implementations

 - [ChEBI-discovery](https://github.com/eMetaboHUB/ChEBI-discovery) : ScalaJs implementation of *"Improving lipid mapping in Genome Scale Metabolic Networks using ontologies."* [[1]](#1). 
   This methodology uses the Chemical Entities of Biological Interest (ChEBI)

## Contact/Issues

Add [GitHub issue](https://github.com/p2m2/discovery/issues/new) to request bugs, new features or suggest modifications to existing features. 

## Licence
The software is licensed under MIT and under development.

## References
<a id="1">[1]</a>
Poupin, N., Vinson, F., Moreau, A. et al. Improving lipid mapping in Genome Scale Metabolic Networks using ontologies. Metabolomics 16, 44 (2020). https://doi.org/10.1007/s11306-020-01663-5