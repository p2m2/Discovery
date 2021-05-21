#### Getting results

```
- select(`ref1,ref2,ref3,..`)
- select(`[ref1,ref2,ref3,..]`,`limit`,`offset`) : getResults as a promise (json format)
- selectByPage(`ref1,ref2,ref3,..`) : getResults as a promise (count,Array[Promise])
```