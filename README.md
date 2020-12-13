# Discovery

test
## Compil

```
sbt discoveryJVM/run
sbt discoveryJVM/test
```

## commands
```
npm install jsdom
sbt discoveryJS/run
sbt discoveryJS/test
sbt discoveryJS/fastOptJS
sbt discoveryJS/fullOptJS
sbt discoveryJS/fastOptJS::webpack => bundle package
```

## note dev

```
sbt compile
sbt run   # run web app http://localhost:9000
sbt package
sbt test
sbt fastOptJS => generer le JS
sbt discoveryJVM/testOnly inrae.semantic_web.QueryPlannerTest
```

Vscode support : Metals plugin
npm install source-map-support

### Js example
```html
<script type="text/javascript" src="https://cdn.jsdelivr.net/gh/ofilangi/easySparql@develop/ext/es-opt.js"></script>
```

