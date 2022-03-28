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
#include "trace_converter.h"

namespace {
constexpr int ARGC_MIN_VALUE = 4;
}

int main(int argc, char* argv[])
{
    if (argc <= ARGC_MIN_VALUE) {
        printf("Usage: %s [-g] [-f] -i htrace.bin -o ftrace.txt\n", argv[0]);
        printf("    -g   parse tgid");
        printf("    -f   parse flags");
        return 1;
    }

    bool parseGid = false;
    bool parseFlags = false;
    std::string input = "htrace.bin";
    std::string output = "ftrace.txt";
    for (int i = 1; i < argc; i++) {
        std::string arg = argv[i];
        if (arg == "-g") {
            parseGid = true;
        } else if (arg == "-f") {
            parseFlags = true;
        } else if (arg == "-i") {
            input = argv[++i];
        } else if (arg == "-o") {
            output = argv[++i];
        }
    }

    TraceConverter converter;
    converter.SetParseGid(parseGid);
    converter.SetParseFlags(parseFlags);
    converter.SetInput(input);
    converter.SetOutput(output);

    printf("convert %s to %s start!\n", input.c_str(), output.c_str());
    if (!converter.Convert()) {
        printf("convert %s to %s failed!\n", input.c_str(), output.c_str());
    }
    return 0;
}