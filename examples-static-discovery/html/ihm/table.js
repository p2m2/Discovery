let config = new EasySparqlStatementConfiguration()
 //"http://localhost:8089/sparql/",
config.setConfigString(`
          {
          "sources" : [{
          "id"  : "p2m2",
          "url" : "http://endpoint-metabolomics.ara.inrae.fr/peakforest/sparql/",
          "typ" : "tps",
          "method" : "POST_ENCODED",
          "mimetype" : "json"
           }]}
          `)

query = new EasySparqlEngine(config);
/*
  let request = query.something("instance")
                     .datatype(URI("label","rdfs"),"label")
                     .isSubjectOf(URI("a"))
                     .set(URI("DatatypeProperty","owl"))
                     .select("instance","label")

  request.then( response => {
          console.log(response);

          let nb_attrib = response.results.bindings.length
          let column = [] ;
          for (r of response.results.bindings) {
             let uri = r["instance"].value ;
             let label = uri.split("#").pop() ;

             if ( Array.isArray(response.results.datatypes.label[uri]) )
               label = response.results.datatypes.label[uri][0].value ;

              console.log(uri + "  -->  " + label );
              column.push({
                title: label ,
                uri: uri ,
                field: uri,
                sortable: true,
                align: 'center',
                footerFormatter: totalTextFormatter
              }) ;
          }

          initTable(column);
  });


