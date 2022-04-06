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

#include "rpc_server.h"

#include <cstring>
#include <functional>
#include <cstdint>

#include "log.h"

namespace SysTuning {
namespace TraceStreamer {
bool RpcServer::ParseData(const uint8_t* data, size_t len, ResultCallBack resultCallBack)
{
    TS_LOGI("RPC ParseData, has parsed len %zu + %zu", lenParseData_, len);
    do {
        constexpr size_t blockSize = 1024 * 1024;
        size_t parseSize = std::min(len, blockSize);
        std::unique_ptr<uint8_t[]> buf = std::make_unique<uint8_t[]>(parseSize);
        std::copy(data, data + parseSize, buf.get());

        if (!ts_->ParseTraceDataSegment(std::move(buf), parseSize)) {
            if (resultCallBack) {
                resultCallBack("formaterror\r\n");
            }
            return false;
        }
        data += parseSize;
        len -= parseSize;
        lenParseData_ += parseSize;
    } while (len > 0);
    if (resultCallBack) {
        resultCallBack("ok\r\n");
    }
    return true;
}

bool RpcServer::ParseDataOver(const uint8_t* data, size_t len, ResultCallBack resultCallBack)
{
    TS_LOGI("RPC ParseDataOver, has parsed len %zu", lenParseData_);

    ts_->WaitForParserEnd();
    ts_->Clear();
    if (resultCallBack) {
        resultCallBack("ok\r\n");
    }
    lenParseData_ = 0;
    return true;
}

bool RpcServer::SqlOperate(const uint8_t* data, size_t len, ResultCallBack resultCallBack)
{
    std::string sql(reinterpret_cast<const char*>(data), len);
    TS_LOGI("RPC SqlOperate(%s, %zu)", sql.c_str(), len);

    int ret = ts_->OperateDatabase(sql);
    if (resultCallBack) {
        std::string response = "ok\r\n";
        if (ret != 0) {
            response = "dberror\r\n";
        }
        resultCallBack(response);
    }
    return (ret == 0);
}

bool RpcServer::SqlQuery(const uint8_t* data, size_t len, ResultCallBack resultCallBack)
{
    std::string sql(reinterpret_cast<const char*>(data), len);
    TS_LOGI("RPC SqlQuery %zu:%s", len, sql.c_str());

    int ret = ts_->SearchDatabase(sql, resultCallBack);
    if (resultCallBack && ret != 0) {
        resultCallBack("dberror\r\n");
    }
    return (ret == 0);
}

bool RpcServer::Reset(const uint8_t* data, size_t len, ResultCallBack resultCallBack)
{
    TS_LOGI("RPC reset trace_streamer");

    ts_->WaitForParserEnd();
    ts_ = std::make_unique<TraceStreamerSelector>();
    if (resultCallBack) {
        resultCallBack("ok\r\n");
    }
    return true;
}

int RpcServer::WasmSqlQuery(const uint8_t* data, size_t len, uint8_t* out, int outLen)
{
    std::string sql(reinterpret_cast<const char*>(data), len);
    TS_LOGI("WASM RPC SqlQuery out(%p:%d) sql(%zu:%s)", reinterpret_cast<void*>(out), outLen,
        len, sql.c_str());

    int ret = ts_->SearchDatabase(sql, out, outLen);
    return ret;
}
} // namespace TraceStreamer
} // namespace SysTuning
