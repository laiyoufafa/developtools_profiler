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

importScripts("trace_streamer_builtin.js", "TempSql.js", "TraceWorkerPerfDataQuery.js", "TraceWorkerNativeMemory.js");
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

const REQ_BUF_SIZE = 4 * 1024 * 1024;
let reqBufferAddr:number = -1;
let _response:Function|undefined|null = undefined;
self.onmessage = async (e: MessageEvent) => {
    if (e.data.action === "open") {
        await initWASM();
        // @ts-ignore
        self.postMessage({id: e.data.id, action: e.data.action, ready: true, index: 0});
        let uint8Array = new Uint8Array(e.data.buffer);
        let callback = (heapPtr: number, size: number) => {
            let out = Module.HEAPU8.subarray(heapPtr, heapPtr + size);
            let str = dec.decode(out);
            arr.length = 0;
            str = str.substring(str.indexOf("\n") + 1);
            if (!str) {
            }else{
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
            }
            if (e.data.action === "exec") {
                // @ts-ignore
                self.postMessage({id: e.data.id, action: e.data.action, results: arr});
            }
        }
        let fn = Module.addFunction(callback, "vii");
        reqBufferAddr = Module._Initialize(fn,REQ_BUF_SIZE);
        let wrSize = 0;
        let r2 = -1;
        while (wrSize < uint8Array.length) {
            const sliceLen = Math.min(uint8Array.length - wrSize, REQ_BUF_SIZE);
            const dataSlice = uint8Array.subarray(wrSize, wrSize + sliceLen);
            Module.HEAPU8.set(dataSlice, reqBufferAddr);
            wrSize += sliceLen;
            r2 = Module._TraceStreamerParseDataEx(sliceLen);
            if (r2 == -1) {
                break;
            }
        }
        Module._TraceStreamerParseDataOver();
        if (r2 == -1) {
            // @ts-ignore
            self.postMessage({id: e.data.id, action: e.data.action, init: false, msg: "parse data error"});
            return;
        }
        // @ts-ignore
        temp_init_sql_list.forEach((item, index) => {
            let r = createView(item);
            // @ts-ignore
            self.postMessage({id: e.data.id, ready: true, index: index + 1});
        });
        // @ts-ignore
        self.postMessage({id: e.data.id, action: e.data.action, init: true, msg: "ok", buffer: e.data.buffer},  [e.data.buffer]);
    } else if (e.data.action === "exec") {
        query(e.data.name, e.data.sql, e.data.params);
        // @ts-ignore
        self.postMessage({id: e.data.id, action: e.data.action, results: arr});
    } else if (e.data.action == "exec-buf") {
        let arr = queryArrayBuffer(e.data.name, e.data.sql, e.data.params);
        // @ts-ignore
        self.postMessage({id: e.data.id, action: e.data.action, results: arr}, [arr]);
    } else if (e.data.action == "perf-init") {
        // @ts-ignore
        perfDataQuery.initPerfFiles(query)
        self.postMessage({id: e.data.id, action: e.data.action, results: perfDataQuery.callChainMap, msg: "ok"});
    } else if (e.data.action == "perf-action") {
        // @ts-ignore
        self.postMessage({id: e.data.id, action: e.data.action, results: perfDataQuery.resolvingAction(e.data.params)});
    } else if (e.data.action == "native-memory-init") {
        // @ts-ignore
        self.postMessage({
            id: e.data.id,
            action: e.data.action,
            results: nativeMemoryWorker.initNativeMemory(query),
            msg: "ok"
        });
    } else if (e.data.action == "native-memory-action") {
        // @ts-ignore
        self.postMessage({
            id: e.data.id,
            action: e.data.action,
            results: nativeMemoryWorker.resolvingAction(e.data.params)
        });
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

function queryArrayBuffer(name: string, sql: string, params: any) {
    if (params) {
        Reflect.ownKeys(params).forEach((key: any) => {
            if (typeof params[key] === "string") {
                sql = sql.replace(new RegExp(`\\${key}`, "g"), `'${params[key]}'`);
            } else {
                sql = sql.replace(new RegExp(`\\${key}`, "g"), params[key]);
            }
        });
    }
    let arr: Array<any> = []
    let enc = new TextEncoder();
    let dec = new TextDecoder();
    let sqlPtr = Module._malloc(sql.length);
    let outPtr = Module._malloc(REQ_BUF_SIZE);
    let sqlUintArray = enc.encode(sql);
    Module.HEAPU8.set(sqlUintArray, sqlPtr);
    let res = Module._TraceStreamerSqlQuery(sqlPtr, sql.length, outPtr, REQ_BUF_SIZE);
    let out = Module.HEAPU8.subarray(outPtr, outPtr + res);
    Module._free(sqlPtr);
    Module._free(outPtr);
    out = out.buffer.slice(out.byteOffset, out.byteLength + out.byteOffset)
    return out;
}
let enc = new TextEncoder();
let dec = new TextDecoder();
let arr: Array<any> = []
function query(name: string, sql: string, params: any) {
    if (params) {
        Reflect.ownKeys(params).forEach((key: any) => {
            if (typeof params[key] === "string") {
                sql = sql.replace(new RegExp(`\\${key}`, "g"), `'${params[key]}'`);
            } else {
                sql = sql.replace(new RegExp(`\\${key}`, "g"), params[key]);
            }
        });
    }
    let sqlUintArray = enc.encode(sql);
    Module.HEAPU8.set(sqlUintArray, reqBufferAddr);
    Module._TraceStreamerSqlQueryEx(sqlUintArray.length);
    return arr
}