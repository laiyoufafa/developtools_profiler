/*
 * Copyright (C) 2022 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

importScripts("trace_streamer_builtin.js", "TempSql.js");
self.onerror = function (error: any) {
}
let Module: any = null;

function initWASM() {
    return new Promise((resolve, reject) => {
        // @ts-ignore
        let wasm = trace_streamer_builtin_wasm
        Module = wasm({
            locateFile: (s: any) => {
                return s
            },
            print: (line: any) => {
            },
            printErr: (line: any) => {
            },
            onRuntimeInitialized: () => {
                resolve("ok");
            },
            onAbort: () => {
                reject("on abort");
            }
        });
    })
}

const REQ_BUF_SIZE = 32 * 1024 * 1024;
self.onmessage = async (e: MessageEvent) => {
    if (e.data.action === "open") {
        await initWASM();
        // @ts-ignore
        self.postMessage({id: e.data.id, action: "open", ready: true,index:0});
        let uint8Array = new Uint8Array(e.data.buffer);
        let p = Module._malloc(uint8Array.length);
        Module.HEAPU8.set(uint8Array, p);
        let r1 = Module._TraceStreamerParseData(p, uint8Array.length);
        let r2 = Module._TraceStreamerParseDataOver();
        if(r1 == -1){
            // @ts-ignore
            self.postMessage({id: e.data.id, action: "open", init: false,msg:"parse data error"});
            return ;
        }
        // @ts-ignore
        temp_init_sql_list.forEach((item, index) => {
            let r = createView(item);
            // @ts-ignore
            self.postMessage({id: e.data.id, ready: true, index: index + 1});
        });
        // @ts-ignore
        self.postMessage({id: e.data.id, action: "open", init: true,msg:"ok"});
    } else if (e.data.action === "exec") {
        let arr = query(e.data.name, e.data.sql, e.data.params);
        // @ts-ignore
        self.postMessage({id: e.data.id, action: "exec", results: arr});
    }
}

function createView(sql: string) {
    let enc = new TextEncoder();
    let dec = new TextDecoder();
    let sqlPtr = Module._malloc(sql.length);
    Module.HEAPU8.set(enc.encode(sql), sqlPtr);
    let res = Module._TraceStreamerSqlOperate(sqlPtr, sql.length);
    return res;
}

function query(name: string, sql: string, params: any) {
    if (params) {
        Reflect.ownKeys(params).forEach((key: any) => {
            if(typeof params[key] ==="string"){
                sql = sql.replace(new RegExp(`\\${key}`, "g"), `'${params[key]}'`);
            }else{
                sql = sql.replace(new RegExp(`\\${key}`, "g"), params[key]);
            }
        });
    }
    let arr: Array<any> = []
    let enc = new TextEncoder();
    let dec = new TextDecoder();
    let sqlPtr = Module._malloc(sql.length);
    let outPtr = Module._malloc(REQ_BUF_SIZE);
    Module.HEAPU8.set(enc.encode(sql), sqlPtr);
    let a = new Date().getTime();
    let res = Module._TraceStreamerSqlQuery(sqlPtr, sql.length, outPtr, REQ_BUF_SIZE);
    let out = Module.HEAPU8.subarray(outPtr, outPtr + res);
    let str = dec.decode(out);
    Module._free(sqlPtr);
    Module._free(outPtr);
    str = str.substring(str.indexOf("\n") + 1);
    let parse = JSON.parse(str);
    let columns = parse.columns;
    let values = parse.values;
    for (let i = 0; i < values.length; i++) {
        let obj: any = {}
        for (let j = 0; j < columns.length; j++) {
            obj[columns[j]] = values[i][j]
        }
        arr.push(obj)
    }
    return arr
}