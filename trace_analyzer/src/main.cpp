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

#include <chrono>
#include <cinttypes>
#include <fcntl.h>
#include <fstream>
#include <iostream>
#include <memory>

#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

#include "file.h"
#include "filter/slice_filter.h"
#include "log.h"
#include "parser/bytrace_parser/bytrace_event_parser.h"
#include "parser/bytrace_parser/bytrace_parser.h"

#include "thread_state.h"
#include "trace_streamer/trace_streamer_selector.h"
#include "trace_streamer_filters.h"
using namespace SysTuning::TraceStreamer;

namespace SysTuning {
namespace TraceStreamer {
using namespace SysTuning::TraceStreamer;
using namespace SysTuning::base;
constexpr size_t G_CHUNK_SIZE = 1024 * 1024;
constexpr int G_MIN_PARAM_NUM = 2;
constexpr size_t G_FILE_PERMISSION = 664;
size_t g_loadSize = 0;
const char* TRACE_STREAM_VERSION = "1.1.102";          // version
const char* TRACE_STREAM_PUBLISHVERSION = "2021/8/30"; // publish datetime

void ExportStatusToLog(TraceParserStatus stauts)
{
    std::string path = GetExecutionDirectoryPath() + "/trace_streamer.log";
    std::ofstream out(path, std::ios_base::trunc);
    out << (std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::system_clock::now().time_since_epoch()))
               .count()
        << ":" << stauts << std::endl;
    out.close();
}

void ShowHelpInfo(const char* argv)
{
    TS_LOGI(
        "trace analyze tool, it can transfer a bytrace/htrace file into a "
        "SQLite database and save result to a local file trace_streamer.log.\n"
        "Usage: %s FILE -e sqlite_out.pb\n"
        " or    %s FILE -c\n"
        "Options:\n"
        " -e    transfer a bytrace file into a SQLiteBased DB.\n"
        " -c    command line mode.\n"
        " -v    show version.",
        argv, argv);
}
void PrintVersion()
{
    fprintf(stderr, "version %s\n", TRACE_STREAM_VERSION);
}

bool ReadAndParser(SysTuning::TraceStreamer::TraceStreamerSelector& ta, int fd)
{
    auto startTime =
        (std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::system_clock::now().time_since_epoch()))
            .count();
    g_loadSize = 0;
    while (true) {
        std::unique_ptr<uint8_t[]> buf = std::make_unique<uint8_t[]>(std::move(G_CHUNK_SIZE));
        auto rsize = Read(fd, buf.get(), G_CHUNK_SIZE);
        if (rsize == 0) {
            break;
        }

        if (rsize < 0) {
            TS_LOGI("Reading trace file failed (errno: %d, %s)", errno, strerror(errno));
            return false;
        }
        g_loadSize += rsize;
        if (!ta.ParseTraceDataSegment(std::move(buf), static_cast<size_t>(rsize))) {
            return false;
        };
        printf("\rLoadingFile:\t%.2f MB\r", static_cast<double>(g_loadSize) / 1E6);
    }
    ta.WaitForParserEnd();
    auto endTime =
        (std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::system_clock::now().time_since_epoch()))
            .count();
    fprintf(stdout, "\nParserDuration:\t%u ms\n", static_cast<unsigned int>(endTime - startTime));
    fprintf(stdout, "ParserSpeed:\t%.2f MB/s\n", (g_loadSize / (endTime - startTime) / 1E3));
    return true;
}
int OpenAndParserFile(TraceStreamerSelector& ts, const std::string& traceFilePath)
{
    int fd(OpenFile(traceFilePath, O_RDONLY, G_FILE_PERMISSION));
    if (fd < 0) {
        TS_LOGI("%s does not exist", traceFilePath.c_str());
        ExportStatusToLog(TRACE_PARSER_ABNORMAL);
        return 1;
    }
    if (!ReadAndParser(ts, fd)) {
        close(fd);
        ExportStatusToLog(TRACE_PARSER_ABNORMAL);
        return 1;
    }
    MetaData* metaData = ts.GetMetaData();

    std::string fileNameTmp = traceFilePath;
#ifdef _WIN32
    if (!base::GetCoding(reinterpret_cast<const uint8_t*>(fileNameTmp.c_str()), fileNameTmp.length())) {
        fileNameTmp = base::GbkToUtf8(fileNameTmp.c_str());
    }
#endif
    metaData->SetSourceFileName(fileNameTmp);
    metaData->SetTraceType((ts.DataType() == TRACE_FILETYPE_H_TRACE) ? "proto-based-trace" : "txt-based-trace");

    close(fd);
    return 0;
}
int ExportDatabase(TraceStreamerSelector& ts, const std::string& sqliteFilePath)
{
    auto startTime =
        (std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::system_clock::now().time_since_epoch()))
            .count();
    if (!sqliteFilePath.empty()) {
        MetaData* metaData = ts.GetMetaData();
        std::string fileNameTmp = sqliteFilePath;
#ifdef _WIN32
        if (!base::GetCoding(reinterpret_cast<const uint8_t*>(fileNameTmp.c_str()), fileNameTmp.length())) {
            fileNameTmp = base::GbkToUtf8(fileNameTmp.c_str());
        }
#endif
        metaData->SetOutputFileName(fileNameTmp);
        metaData->SetParserToolVersion(TRACE_STREAM_VERSION);
        metaData->SetParserToolPublishDateTime(TRACE_STREAM_PUBLISHVERSION);
        metaData->SetTraceDataSize(g_loadSize);
        fprintf(stdout, "ExportDatabase begin...\n");
        if (ts.ExportDatabase(sqliteFilePath)) {
            fprintf(stdout, "ExportDatabase failed\n");
            ExportStatusToLog(TRACE_PARSER_ABNORMAL);
            return 1;
        }
        fprintf(stdout, "ExportDatabase end\n");
    }
    ExportStatusToLog(GetAnalysisResult());
    auto endTime =
        (std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::system_clock::now().time_since_epoch()))
            .count();
    endTime += 1; // for any exception of endTime == startTime
    fprintf(stdout, "ExportDuration:\t%u ms\n", static_cast<unsigned int>(endTime - startTime));
    fprintf(stdout, "ExportSpeed:\t%.2f MB/s\n", (g_loadSize / (endTime - startTime) / 1E3));
    return 0;
}
} // namespace TraceStreamer
} // namespace SysTuning
int main(int argc, char** argv)
{
    if (argc < G_MIN_PARAM_NUM) {
        ShowHelpInfo(argv[0]);
        ExportStatusToLog(TRACE_PARSER_ABNORMAL);
        return 1;
    }
    std::string traceFilePath;
    std::string sqliteFilePath;
    bool interactiveState = false;
    bool exportMetaTable = true;
    for (int i = 1; i < argc; i++) {
        if (!strcmp(argv[i], "-e")) {
            if (++i == argc) {
                ShowHelpInfo(argv[0]);
                ExportStatusToLog(TRACE_PARSER_ABNORMAL);
                return 1;
            }
            sqliteFilePath = std::string(argv[i]);
            continue;
        } else if (!strcmp(argv[i], "-c") || !strcmp(argv[i], "--command")) {
            interactiveState = true;
            continue;
        } else if (!strcmp(argv[i], "-nm") || !strcmp(argv[i], "--nometa")) {
            exportMetaTable = false;
            continue;
        } else if (!strcmp(argv[i], "-v") || !strcmp(argv[i], "--v") || !strcmp(argv[i], "-version") ||
                   !strcmp(argv[i], "--version")) {
            PrintVersion();
            return 0;
        }
        traceFilePath = std::string(argv[i]);
    }
    if (traceFilePath.empty() || (!interactiveState && sqliteFilePath.empty())) {
        ShowHelpInfo(argv[0]);
        ExportStatusToLog(TRACE_PARSER_ABNORMAL);
        return 1;
    }

    TraceStreamerSelector ts;
    ts.EnableMetaTable(exportMetaTable);
    if (OpenAndParserFile(ts, traceFilePath)) {
        return 1;
    }
    if (interactiveState && sqliteFilePath.empty()) {
        sqliteFilePath = "default.db";
    }
    if (ExportDatabase(ts, sqliteFilePath)) {
        ExportStatusToLog(TRACE_PARSER_ABNORMAL);
        return 1;
    }
    ExportStatusToLog(GetAnalysisResult());
    if (interactiveState) {
        ts.SearchData(sqliteFilePath);
    }
    return 0;
}
