export declare class SWDiscoveryConfiguration {
        constructor()
        static setConfigString(json : string) : SWDiscoveryConfiguration
    }

export declare class URI { constructor(uri: string) }
    export class Literal { constructor(value : string,datatype? : URI,ta? : string) }
    export class QueryVariable { constructor(name: string) }
    export class PropertyPath { constructor(value : string)  }

    interface DiscoveryResponse {
        readonly results: {
            [key: string] : any
        };
    }

    export class SWTransaction {

        distinct : SWTransaction
        reduced : SWTransaction
        limit(val : number) : SWTransaction
        offset(val : number) : SWTransaction
        orderByAsc(ref : string[]) : SWTransaction
        orderByDesc(ref : string[]) : SWTransaction
        getSerializedString : string
        setSerializedString(transaction_string : String): SWTransaction
        console() : SWTransaction

        //aggregate(lvar:string[])
        commit() : SWTransaction
        raw() : Promise<DiscoveryResponse>

    }

    export class FilterIncrement {

        isLiteral : SWDiscovery
        isUri: SWDiscovery
        isBlank : SWDiscovery

        regex(pattern : rdfterm, flags? : any) : SWDiscovery
        contains(l: rdfterm) : SWDiscovery
        strStarts( string : rdfterm ) : SWDiscovery
        strEnds( string : rdfterm ) : SWDiscovery
        equal( value : rdfterm ) : SWDiscovery
        inf( value : rdfterm ) : SWDiscovery
        infEqual( value : rdfterm ) : SWDiscovery
        sup( value : rdfterm ) : SWDiscovery
        supEqual( value : rdfterm ) : SWDiscovery

        not: FilterIncrement
    }

    type rdfterm = string | URI | QueryVariable | Literal | PropertyPath

    // Types inside here
export declare class SWDiscovery {
        constructor(config? : SWDiscoveryConfiguration)
        focus() : string
        focus(foc:string) : SWDiscovery
        root() : SWDiscovery
        something(uri? : string) : SWDiscovery
        isObjectOf(uri : rdfterm ,ref? : string) : SWDiscovery
        isSubjectOf(uri : rdfterm,ref? : string) : SWDiscovery
        isLinkTo(uri : rdfterm ,ref? : string) : SWDiscovery
        isLinkFrom(uri : rdfterm ,ref? : string) : SWDiscovery
        set(term : string | URI) : SWDiscovery
        setList(term : (string | URI)[]) : SWDiscovery
        isA(uri : string | URI) : SWDiscovery
        datatype(uri : rdfterm,ref : string) : SWDiscovery

        filter : FilterIncrement

        getSerializedString() : string
        setSerializedString(query:string) : SWDiscovery

        console() : SWDiscovery
        sparql() : string


        select(...listVariables : any[]) : SWTransaction
        selectByPage(...listVariables : any[]) : Promise<[number,SWTransaction[]]>
    }