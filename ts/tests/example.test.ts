var v = require("../../js/target/scala-2.13/scalajs-bundler/main/discovery-opt.js")
import { SWDiscoveryConfiguration, SWDiscovery } from '../discovery'

describe('Test person.ts', () => {

    beforeEach(() => { console.log("before test")});

    test('something console', () => {

      const json : string =  `
      {
      "sources" : [{
      "id"  : "metabolights",
      "url" : "https://metabolights.semantic-metabolomics.fr/sparql"
       }]}
      `

      const localConf = v.SWDiscoveryConfiguration.setConfigString(json)
      const disco = new v.SWDiscovery(json)

      //disco.console()
      console.log("Hello World !")
    })
});