//@ts-ignore
import {perfDataQuery} from "../../../../dist/trace/component/hiperf/PerfDataQuery.js"

describe('perfDataQuery Test',()=>{

    it('perfDataQueryTest01 ', function () {
        let callChain  = {
            symbolId:-1,
            fileId:1,
            fileName:"unkown",
            vaddrInFile:1,
        }
        expect(perfDataQuery.setCallChainName(callChain)).toBe("+0x1");
    });

    it('perfDataQueryTest02 ', function () {
        let callChain  = {
            tid:1,
            threadState:"",
            bottomUpMerageId:"1",
        }
        perfDataQuery.threadData[callChain.tid] = jest.fn(()=>[])
        perfDataQuery.threadData[callChain.tid].threadName = jest.fn(()=>"")
        expect(perfDataQuery.addProcessThreadStateData(callChain)).toBeUndefined();
    });

    it('perfDataQueryTest03 ', function () {
        expect(perfDataQuery.getCallChainsBySampleIds([{length:1}],true)).not.toBeUndefined();
    });

    it('perfDataQueryTest04 ', function () {
        perfDataQuery.mapGroupBy = jest.fn(()=>true)
        expect(perfDataQuery.recursionCreateData("","",true)).toBeUndefined();
    });

    it('perfDataQueryTest05 ', function () {
        let merageData  = {
            parentId:1,
            parentNode:"",
        }
        expect(perfDataQuery.recursionCreateTree(merageData)).toBeUndefined();
    });

    it('perfDataQueryTest06 ', function () {
        let callChain  = {
            topDownMerageId:1,
            bottomUpMerageId:1,
        }
        expect(perfDataQuery.mapGroupBy('',callChain,true)).not.toBeUndefined();
    });
})