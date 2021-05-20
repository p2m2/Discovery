# What is discovery

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
        src="https://cdn.jsdelivr.net/gh/p2m2/discovery@master/dist/discovery-web.min.js"> 
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

## Configuration

see [documentation](./api/inrae/semantic_web/StatementConfigurationException.html)

## DSL

see [documentation](./api/inrae/semantic_web/SWDiscovery.html)

## Contact/Issues

The software is licensed under MIT and under development.
Use GitHub form to request bugs, new features or suggest modifications to existing features. 
