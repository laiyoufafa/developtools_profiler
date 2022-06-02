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
 **/
#include <node_api.h>
#include <cerrno>
#include <unistd.h>
#include "gp_utils.h"

namespace gpUtils {
    void mSplit(const std::string &content, const std::string &sp, std::vector<std::string> &out) {
        int index = 0;
        while (index != std ::string::npos) {
            int t_end = content.find_first_of(sp, index);
            std::string tmp = content.substr(index, t_end - index);
            if (tmp != "" && tmp != " ")
                out.push_back(tmp);
            if (t_end == std::string::npos)
                break;
            index = t_end + 1;
        }
    }

    bool canOpen(const std::string &path) {
        FILE* fp = fopen(path.c_str(), "r");
        if (fp == nullptr) {
            return false;
        }
        if (fclose(fp) == EOF) {
            return false;
        }
        return true;
    }

    bool canCmd(const std::string &cmd) {
        FILE* pp = popen(cmd.c_str(), "r");
        if (pp == nullptr) {
            return false;
        }
        pclose(pp);
        return true;
    }

    // popen
    std::string readCmd(const std::string &cmd)
    {
        const int buffLength = 1024;
        std::string res = "NA";
        FILE *pp = popen(cmd.c_str(), "r");
        if (pp == nullptr) {
            return res;
        } else {
            char line[buffLength];
            line[0] = '\0';
            while (fgets(line, buffLength, pp) != nullptr) {
                res = std::string(line);
            }
        }
        pclose(pp);
        return res;
    }

    // fopen
    std::string readFile(const std::string &path)
    {
        std::string res = "NA";
        const int buffLengh = 1024;
        FILE *fp;
        if ((fp = fopen(path.c_str(), "r")) != nullptr) {
            char s[buffLengh];
            s[0] = '\0';
            while (fgets(s, sizeof(s), fp) != nullptr) {
                res += std::string(s);
            }
        }
        if (fp != nullptr) {
            fclose(fp);
        }
        return res;
    }
    // get number_str from str
    std::string extractNumber(const std::string &str)
    {
        int cntInt = 0;
        const int shift = 10;

        for (int i = 0; str[i] != '\0'; ++i) {
            if (str[i] >= '0' && str[i] <= '9') {
                cntInt *= shift;
                cntInt += str[i] - '0';
            }
        }
        return std::to_string(cntInt);
    }
}