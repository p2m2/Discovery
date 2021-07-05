import { StatementConfiguration, SWDiscovery, URI} from '../discovery'

const json : string =  `
{
"sources" : [{
"id"  : "metabolights",
"url" : "https://metabolights.semantic-metabolomics.fr/sparql"
 }]}
`

const localConf = StatementConfiguration.setConfigString(json)
const disco = new SWDiscovery(json).something()

disco.console()
console.log("Hello World !")