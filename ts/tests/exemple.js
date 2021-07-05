"use strict";
exports.__esModule = true;
var discovery_1 = require("../discovery");
var json = "\n{\n\"sources\" : [{\n\"id\"  : \"metabolights\",\n\"url\" : \"https://metabolights.semantic-metabolomics.fr/sparql\"\n }]}\n";
var localConf = discovery_1.StatementConfiguration.setConfigString(json);
var disco = new discovery_1.SWDiscovery(json).something();
disco.console();
console.log("Hello World !");
