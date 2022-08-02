/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
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

#include "wasm_func.h"

namespace SysTuning {
namespace TraceStreamer {
RpcServer g_wasmTraceStreamer;
extern "C" {
using ReplyFunction = void (*)(const char* data, uint32_t len);
ReplyFunction g_reply;
uint8_t* g_reqBuf;
uint32_t g_reqBufferSize;
void ResultCallback(const std::string& jsonResult)
{
    g_reply(jsonResult.data(), jsonResult.size());
}
EMSCRIPTEN_KEEPALIVE uint8_t* Initialize(ReplyFunction replyFunction, uint32_t reqBufferSize)
{
    g_reply = replyFunction;
    g_reqBuf = new uint8_t[reqBufferSize];
    g_reqBufferSize = reqBufferSize;
    return g_reqBuf;
}

// return 0 while ok, -1 while failed
EMSCRIPTEN_KEEPALIVE int TraceStreamerParseData(const uint8_t* data, int dataLen)
{
    if (g_wasmTraceStreamer.ParseData(data, dataLen, nullptr)) {
        return 0;
    }
    return -1;
}
// return 0 while ok, -1 while failed
EMSCRIPTEN_KEEPALIVE int TraceStreamerParseDataEx(int dataLen)
{
    if (g_wasmTraceStreamer.ParseData(g_reqBuf, dataLen, nullptr)) {
        return 0;
    }
    return -1;
}
EMSCRIPTEN_KEEPALIVE int TraceStreamerParseDataOver()
{
    if (g_wasmTraceStreamer.ParseDataOver(nullptr, 0, nullptr)) {
        return 0;
    }
    return -1;
}
EMSCRIPTEN_KEEPALIVE int TraceStreamerSqlOperate(const uint8_t* sql, int sqlLen)
{
    if (g_wasmTraceStreamer.SqlOperate(sql, sqlLen, nullptr)) {
        return 0;
    }
    return -1;
}
EMSCRIPTEN_KEEPALIVE int TraceStreamerSqlOperateEx(int sqlLen)
{
    if (g_wasmTraceStreamer.SqlOperate(g_reqBuf, sqlLen, nullptr)) {
        return 0;
    }
    return -1;
}
EMSCRIPTEN_KEEPALIVE int TraceStreamerReset()
{
    g_wasmTraceStreamer.Reset(nullptr, 0, nullptr);
    return 0;
}
// return the length of result, -1 while failed
EMSCRIPTEN_KEEPALIVE int TraceStreamerSqlQuery(const uint8_t* sql, int sqlLen, uint8_t* out, int outLen)
{
    return g_wasmTraceStreamer.WasmSqlQuery(sql, sqlLen, out, outLen);
}
// return the length of result, -1 while failed
EMSCRIPTEN_KEEPALIVE int TraceStreamerSqlQueryEx(int sqlLen)
{
    return g_wasmTraceStreamer.WasmSqlQueryWithCallback(g_reqBuf, sqlLen, &ResultCallback);
}
EMSCRIPTEN_KEEPALIVE int TraceStreamerCancel()
{
    g_wasmTraceStreamer.CancelSqlQuery();
    return 0;
}
} // extern "C"
} // namespace TraceStreamer
} // namespace SysTuning
