# Discovery

[![p2m2](https://circleci.com/gh/p2m2/Discovery.svg?style=shield)](https://app.circleci.com/pipelines/github/p2m2)
[![codecov](https://codecov.io/gh/p2m2/Discovery/branch/develop/graph/badge.svg)](https://codecov.io/gh/p2m2/Discovery)
[![CodeFactor](https://www.codefactor.io/repository/github/p2m2/discovery/badge)](https://www.codefactor.io/repository/github/p2m2/discovery)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/8d8ecb66f9ff4963a22efab3c693b629)](https://www.codacy.com/gh/p2m2/Discovery/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=p2m2/Discovery&amp;utm_campaign=Badge_Grade)

## Installation

```sbt
resolvers += Resolver.bintrayRepo("hmil", "maven")
libraryDependencies += "com.github.p2m2" %%% "discovery" % "0.0.2-SNAPSHOT"
```

## Library generation 

```
sbt discoveryJS/fullOptJS
sbt discoveryJVM/package
sbt publishLocal 
```

### Library generation html/nodejs  

```bash
sbt discoveryJS/fullOptJS
cp js/target/scala-x.xx/discovery-opt.js dist/discovery.js
browserify -r ./dist/discovery.js -s discovery > dist/discovery-web.js
```

## test
```
sbt discoveryJVM/test  
```

## coverage
```
sbt discoveryJVM/coverageReport 
```

### Html/Js example

#### Html import 

```html 
<script type="text/javascript" src="https://cdn.jsdelivr.net/gh/p2m2/Discovery@master/dist/discovery-web.js"></script> 
<script>

var SWDiscovery = discovery.SWDiscovery ;
var SWDiscoveryConfiguration = discovery.SWDiscoveryConfiguration ;
var URI = discovery.URI ;

</script>
```

[js fiddle example](https://jsfiddle.net/ofilangi/3xkay1f6/10/)

#### Node import 

##### dependencies 

```bash
npm install require-from-url
```

```node
var requireFromUrl = require('require-from-url/sync');
var discoveryjs = requireFromUrl("https://cdn.jsdelivr.net/gh/p2m2/Discovery@master/dist/discovery.js")

var SWDiscoveryConfiguration = discoveryjs.SWDiscoveryConfiguration ;
var SWDiscovery = discoveryjs.SWDiscovery ;
var URI = discoveryjs.URI ;
```

[discovery-tutorial-html-js](https://github.com/p2m2/discovery-tutorial-html-js)

### NodeJs example

[discovery-tutorial-nodejs](https://github.com/p2m2/discovery-tutorial-nodejs)

### Scala Exemple 

full example with [table view implementation](https://github.com/p2m2/discovery-table-view) using [scalatags](https://github.com/lihaoyi/scalatags)


## note dev

```
sbt compile
sbt run   # run web app http://localhost:9000
sbt package
sbt test
sbt fastOptJS => generer le JS
sbt discoveryJVM/testOnly inrae.semantic_web.QueryPlannerTest
sbt publishLocal
```
