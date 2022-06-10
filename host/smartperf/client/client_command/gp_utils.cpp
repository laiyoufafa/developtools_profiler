/*
 * Copyright (C) 2021 Huawei Device Co., Ltd.
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

#include <fstream>
#include <sstream>
#include <unistd.h>
#include <climits>
#include <cstdio>
#include <cstdlib>
#include "securec.h"
#include "include/gp_utils.h"

namespace OHOS {
    namespace SmartPerf {
        void GPUtils::mSplit(const std::string &content, const std::string &sp, std::vector<std::string> &out)
        {
            size_t index = 0;
            while (index != std::string::npos) {
                size_t t_end = content.find_first_of(sp, index);
                std::string tmp = content.substr(index, t_end - index);
                if (tmp != "" && tmp != " ") {
                    out.push_back(tmp);
                }
                if (t_end == std::string::npos) {
                    break;
                }
                index = t_end + 1;
            }
        }

        bool GPUtils::canOpen(const std::string &path)
        {
            if (access(path.c_str(), F_OK) == -1) {
                return false;
            }
            FILE *fp = fopen(path.c_str(), "r");
            if (fp == nullptr) {
                return false;
            }
            if (fclose(fp) == EOF) {
                return false;
            }
            return true;
        }

        // popen
        std::string GPUtils::readFile(const std::string &cmd)
        {
            const int buffLengh = 1024;
            std::string res = "NA";
            FILE *fp = popen(cmd.c_str(), "r");
            char line[buffLengh];
            line[0] = '\0';
            while (fgets(line, buffLengh, fp) != nullptr) {
                res = std::string(line);
            }

            if (pclose(fp) == EOF) {
                return "";
            }
            return res;
        }

        // fopen
        std::string GPUtils::freadFile(const std::string &path)
        {
            std::string res = "NA";
            const int buffLengh = 1024;
            if (access(path.c_str(), F_OK) == -1) {
                return res;
            }
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
        std::string GPUtils::getNumber(const std::string &str)
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

        // wirte to csv by path
        void GPUtils::writeCsv(const std::string &path, std::vector<GPData> &vmap)
        {
            std::ofstream outFile;
            char realPath[PATH_MAX + 1] = {0x00};
            if (strlen(path.c_str()) > PATH_MAX || realpath(path.c_str(), realPath) == NULL) {
                printf("write csv path --->> %s\n", path.c_str());
            }
            outFile.open(path.c_str(), std::ios::out);
            int i = 0;
            std::string title = "";
            for (GPData gpdata : vmap) {
                std::map<std::string, std::string>::iterator iter;
                std::string line_content = "";
                for (iter = gpdata.values.begin(); iter != gpdata.values.end(); ++iter) {
                    if (i == 0) {
                        title += iter->first + ",";
                    }
                    line_content += iter->second + ",";
                }
                if (i == 0) {
                    title.pop_back();
                    outFile << title << std::endl;
                }
                line_content.pop_back();
                outFile << line_content << std::endl;
                ++i;
            }
            outFile.close();
        }
    }
}
