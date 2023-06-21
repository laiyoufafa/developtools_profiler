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

#include <cstdint>
#include <cstring>
#include <functional>
#if IS_WASM
#include <filesystem>
#endif
#include "log.h"
#include "string_help.h"
#include "version.h"

#define UNUSED(expr)             \
    do {                         \
        static_cast<void>(expr); \
    } while (0)
namespace SysTuning {
namespace TraceStreamer {
const int32_t MAX_LEN_STR = 100;
uint32_t g_fileLen = 0;
bool RpcServer::ParseData(const uint8_t* data, size_t len, ResultCallBack resultCallBack)
{
    g_loadSize += len;
    size_t blockSize = 1024 * 1024;
    do {
        size_t parseSize = std::min(len, blockSize);
        std::unique_ptr<uint8_t[]> buf = std::make_unique<uint8_t[]>(parseSize);
        std::copy(data, data + parseSize, buf.get());
        if (!ts_->ParseTraceDataSegment(std::move(buf), parseSize)) {
            if (resultCallBack) {
                resultCallBack("formaterror\r\n", SEND_FINISH);
            }
            return false;
        }
        data += parseSize;
        len -= parseSize;
        lenParseData_ += parseSize;
    } while (len > 0);
    if (resultCallBack) {
        resultCallBack("ok\r\n", SEND_FINISH);
    }
    return true;
}

int32_t RpcServer::UpdateTraceTime(const uint8_t* data, int32_t len)
{
    std::unique_ptr<uint8_t[]> buf = std::make_unique<uint8_t[]>(len);
    std::copy(data, data + len, buf.get());
    ts_->UpdateTraceRangeTime(buf.get(), len);
    return 0;
}

int32_t RpcServer::TraceStreamer_Init_ThirdParty_Config(const uint8_t* data, int32_t len)
{
    TS_LOGE("TraceStreamer_Init_ThirdParty_Config is comming!");
    std::string thirdPartyConfig = reinterpret_cast<const char*>(data);
    TS_LOGE("thirdPartyConfig = %s", thirdPartyConfig.c_str());
    int32_t size = thirdPartyConfig.size();
    std::vector<std::string> comPonentStr = SplitStringToVec(thirdPartyConfig, ";");
    const int32_t EVENT_COUNT_PAIR = 2;
    if (comPonentStr.size() % EVENT_COUNT_PAIR != 0) {
        TS_LOGE("thirdPartyConfig is wrong!");
        return -1;
    }
    for (int32_t m = 0; m < comPonentStr.size(); m += EVENT_COUNT_PAIR) {
        int32_t componentId = std::stoi(comPonentStr.at(m));
        std::string componentName = comPonentStr.at(m + 1);
        TS_LOGE("comPonentStr[m] = %d, comPonentStr[m + 1] = %s", componentId, componentName.c_str());
        g_thirdPartyConfig.insert((std::map<int32_t, std::string>::value_type(componentId, componentName)));
    }
    return 0;
}

bool RpcServer::ParseDataOver(const uint8_t* data, size_t len, ResultCallBack resultCallBack)
{
    UNUSED(data);
    UNUSED(len);
    MetaData* metaData = ts_->GetMetaData();
    metaData->SetSourceFileName("input stream mode");
    metaData->SetOutputFileName("wasm mode");
    metaData->SetParserToolVersion(TRACE_STREAM_VERSION);
    metaData->SetParserToolPublishDateTime(TRACE_STREAM_PUBLISHVERSION);
    metaData->SetTraceDataSize(g_loadSize);
    metaData->SetTraceType((ts_->DataType() == TRACE_FILETYPE_H_TRACE) ? "proto-based-trace" : "txt-based-trace");
    TS_LOGI("RPC ParseDataOver, has parsed len %zu", lenParseData_);

    ts_->WaitForParserEnd();
#ifndef USE_VTABLE
    ts_->Clear();
#endif
    if (resultCallBack) {
        resultCallBack("ok\r\n", SEND_FINISH);
    }
    lenParseData_ = 0;
    g_loadSize = 0;
    return true;
}

bool RpcServer::SqlOperate(const uint8_t* data, size_t len, ResultCallBack resultCallBack)
{
    ts_->SetCancel(false);
    std::string sql(reinterpret_cast<const char*>(data), len);
    TS_LOGI("RPC SqlOperate(%s, %zu)", sql.c_str(), len);

    int32_t ret = ts_->OperateDatabase(sql);
    if (resultCallBack) {
        std::string response = "ok\r\n";
        if (ret != 0) {
            response = "dberror\r\n";
        }
        resultCallBack(response, SEND_FINISH);
    }
    return (ret == 0);
}

bool RpcServer::SqlQuery(const uint8_t* data, size_t len, ResultCallBack resultCallBack)
{
    ts_->SetCancel(false);
    std::string sql(reinterpret_cast<const char*>(data), len);
    TS_LOGI("RPC SqlQuery %zu:%s", len, sql.c_str());

    int32_t ret = ts_->SearchDatabase(sql, resultCallBack);
    if (resultCallBack && ret != 0) {
        resultCallBack("dberror\r\n", SEND_FINISH);
    }
    ts_->SetCancel(false);
    return (ret == 0);
}

void RpcServer::CancelSqlQuery()
{
    ts_->SetCancel(true);
}

bool RpcServer::Reset(const uint8_t* data, size_t len, ResultCallBack resultCallBack)
{
    UNUSED(data);
    UNUSED(len);
    TS_LOGI("RPC reset trace_streamer");

    ts_->WaitForParserEnd();
    ts_ = std::make_unique<TraceStreamerSelector>();
    if (resultCallBack) {
        resultCallBack("ok\r\n", SEND_FINISH);
    }
    return true;
}

int32_t RpcServer::WasmSqlQuery(const uint8_t* data, size_t len, uint8_t* out, int32_t outLen)
{
    ts_->SetCancel(false);
    std::string sql(reinterpret_cast<const char*>(data), len);
    TS_LOGI("WASM RPC SqlQuery outlen(%d) sql(%zu:%s)", outLen, len, sql.c_str());

    int32_t ret = ts_->SearchDatabase(sql, out, outLen);
    return ret;
}
int32_t RpcServer::WasmSqlQueryWithCallback(const uint8_t* data, size_t len, ResultCallBack callback) const
{
    ts_->SetCancel(false);
    std::string sql(reinterpret_cast<const char*>(data), len);
    TS_LOGI("WASM RPC SqlQuery sql(%zu:%s)", len, sql.c_str());

    int32_t ret = ts_->SearchDatabase(sql, callback);
    return ret;
}

int32_t RpcServer::WasmExportDatabase(ResultCallBack resultCallBack)
{
    return ts_->ExportDatabase("default.db", resultCallBack);
}

#if IS_WASM
int32_t RpcServer::DownloadELFCallback(const std::string& fileName,
                                       size_t totalLen,
                                       const uint8_t* data,
                                       size_t len,
                                       int32_t finish,
                                       ParseELFFileCallBack parseELFFile)
{
    g_fileLen += len;
    FILE* fd;
    std::string filePath = "";
    TS_LOGI("fileName = %s", fileName.c_str());
    std::string symbolsPath = fileName.substr(0, fileName.find("/"));
    TS_LOGI("symbolsPath = %s", symbolsPath.c_str());
    filePath = fileName.substr(0, fileName.find_last_of("/"));
    TS_LOGI("filePath = %s", filePath.c_str());
    if (std::filesystem::create_directories(filePath)) {
        TS_LOGI("create_directories success");
    }
    if (g_fileLen < totalLen) {
        fd = fopen(fileName.c_str(), "a+");
        if (fd == nullptr) {
            TS_LOGE("wasm file create failed");
            return false;
        }
        int32_t writeLength = fwrite(data, len, 1, fd);
        if (!writeLength) {
            fclose(fd);
            TS_LOGE("wasm write file failed");
            return false;
        }
        fclose(fd);
        return false;
    }
    g_fileLen = 0;
    fd = fopen(fileName.c_str(), "a+");
    if (fd == nullptr) {
        TS_LOGE("wasm file open failed");
        return false;
    }

    int32_t writeLength = fwrite(data, len, 1, fd);
    (void)fclose(fd);
    if (!writeLength) {
        TS_LOGE("wasm write file failed");
        return false;
    }
    TS_LOGI("symbolsPath = %s, fileName = %s", symbolsPath.c_str(), fileName.c_str());
    symbolsPathFiles_.emplace_back(fileName);
    parseELFFile("file send over\r\n", SEND_FINISH);

    if (finish) {
        if (!ts_->ReloadSymbolFiles(symbolsPath, symbolsPathFiles_)) {
            if (parseELFFile) {
                parseELFFile("formaterror\r\n", SEND_FINISH);
            }
            return false;
        }
        if (parseELFFile) {
            parseELFFile("ok\r\n", SEND_FINISH);
        }
        std::filesystem::remove_all(filePath);
    }
    return true;
}
#endif

} // namespace TraceStreamer
} // namespace SysTuning
