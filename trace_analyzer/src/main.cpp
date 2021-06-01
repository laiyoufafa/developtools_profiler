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
#include <fstream>
#include <iostream>
#include <memory>

#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

#include "file.h"
#include "log.h"
#include "trace_streamer.h"

namespace SysTuning {
namespace TraceStreamer {
namespace {
constexpr size_t G_CHUNK_SIZE = 1024 * 1024;
TraceStreamer* g_traceStreamer;

void ExportStatusToLog(base::ErrStatus stauts)
{
    std::string path = base::GetExecutionDirectoryPath() + "/trace_streamer.log";
    std::ofstream out(path, std::ios_base::trunc);
    out << (std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::system_clock::now().time_since_epoch()))
                .count()
        << ":" << stauts << std::endl;
    out.close();
}


int ExportDatabase(const std::string& outputName)
{
    return g_traceStreamer->ExportDatabase(outputName);
}

void ShowHelpInfo(char** argv)
{
    TUNING_LOGI(
        "trace analyse tool， it can transfer a bytrace file into a "
        "SQLite database and save result to a local file trace_streamer.log.\n"
        "Usage: %s FILE -e sqlite_out.pb\n"
        "Options:\n"
        " -e    transfer a bytrace file into a SQLiteBased DB.\n"
        " -v    show version.",
        argv[0]);
}

void PrintVersion()
{
    printf("version 0.1.106\n");
}

int FileRead(TraceStreamer& ta, int fd)
{
    ssize_t loadSize = 0;
    while (true) {
        std::unique_ptr<uint8_t[]> buf = std::make_unique<uint8_t[]>(std::move(G_CHUNK_SIZE));
        auto rsize = base::Read(fd, buf.get(), G_CHUNK_SIZE);
        if (rsize == 0) {
            break;
        }

        if (rsize < 0) {
            TUNING_LOGI("Reading trace file failed (errno: %d, %s)", errno, strerror(errno));
            return 1;
        }
        loadSize += rsize;
        if (!ta.Parse(std::move(buf), static_cast<size_t>(rsize))) {
            return 1;
        };
        fprintf(stdout, "\rLoading file: %.2f MB\r", static_cast<double>(loadSize) / 1E6);
    }

    return 0;
}

int EnterTraceStreamer(int argc, char** argv)
{
    if (argc < G_MIN_PARAM_NUM) {
        ShowHelpInfo(argv);
        return 1;
    }

    std::string traceFilePath;
    std::string sqliteFilePath;
    for (int i = 1; i < argc; i++) {
        if (!strcmp(argv[i], "-e")) {
            if (++i == argc) {
                ShowHelpInfo(argv);
                return 1;
            }
            sqliteFilePath = std::string(argv[i]);
            continue;
        } else if (!strcmp(argv[i], "-v") || !strcmp(argv[i], "--v") 
            || !strcmp(argv[i], "-version") || !strcmp(argv[i], "--version")) {
            PrintVersion();
            return 0;
        }
        traceFilePath = std::string(argv[i]);
    }
    if (traceFilePath.empty()) {
        ShowHelpInfo(argv);
        return 1;
    }

    std::unique_ptr<TraceStreamer> ta = std::make_unique<TraceStreamer>();
    int fd(base::OpenFile(traceFilePath, O_RDONLY, G_FILE_PERMISSION));
    if (fd < 0) {
        TUNING_LOGI("%s does not exist", traceFilePath.c_str());
        return 1;
    }
    if (FileRead(*ta, fd)) {
        close(fd);
        return 1;
    }

    close(fd);

    g_traceStreamer = ta.get();

    if (!sqliteFilePath.empty()) {
        return ExportDatabase(sqliteFilePath);
    }

    return 0;
}
} // namespace
} // namespace TraceStreamer
} // namespace SysTuning

int main(int argc, char** argv)
{
    int result = SysTuning::TraceStreamer::EnterTraceStreamer(argc, argv);
    SysTuning::TraceStreamer::ExportStatusToLog(SysTuning::base::GetAnalysisResult());
    return result;
}
