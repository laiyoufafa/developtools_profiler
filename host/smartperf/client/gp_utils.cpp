/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
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

#include <errno.h>
#include <fstream>
#include <sstream>
#include "include/gp_utils.h"

namespace OHOS
{
    namespace SmartPerf
    {
        void GPUtils::mSplit(const std::string &content, const std::string &sp, std::vector<std::string> &out)
        {
            int index = 0;
            while (index != std::string::npos)
            {
                int t_end = content.find_first_of(sp, index);
                std::string tmp = content.substr(index, t_end - index);
                if (tmp != "" && tmp != " ")
                    out.push_back(tmp);
                if (t_end == std::string::npos)
                    break;
                index = t_end + 1;
            }
        }

        bool GPUtils::canOpen(const std::string &path)
        {
            FILE *fp;
            fp = fopen(path.c_str(), "r");
            if (fp == NULL)
            {
                // printf("open path: %s failed, err=%s\n", path.c_str(), strerror(errno));
                return false;
            }
            fclose(fp);
            return true;
        }

        // popen
        std::string GPUtils::readFile(const std::string &cmd)
        {
            std::string res = "NA";
            FILE *fp = popen(cmd.c_str(), "r");
            if (fp == NULL)
            {
                // printf("read_file:%s failed",cmd.c_str());
            }
            char line[1024];
            line[0] = '\0';
            while (fgets(line, 1024, fp) != NULL)
            {
                res = std::string(line);
            }
            fclose(fp);
            return res;
        }

        // fopen
        std::string GPUtils::freadFile(const std::string &path)
        {
            std::string res = "NA";

            FILE *fp;
            if ((fp = fopen(path.c_str(), "r")) == NULL)
            {
                // printf("no such file %s",path.c_str());
            }
            else
            {
                char s[1024];
                s[0] = '\0';
                while (fgets(s, sizeof(s), fp) != NULL)
                {
                    // printf("read line: %s",s);
                    res += std::string(s);
                }
            }
            fclose(fp);
            return res;
        }

        // get number_str from str
        std::string GPUtils::getNumber(const std::string &str)
        {

            int cnt_int = 0;

            for (int i = 0; str[i] != '\0'; ++i)
            {
                if (str[i] >= '0' && str[i] <= '9')
                {
                    cnt_int *= 10;
                    cnt_int += str[i] - '0';
                }
            }
            return std::to_string(cnt_int);
        }

        // wirte to csv by path
        void GPUtils::writeCsv(const std::string &path, std::vector<GPData> &vmap)
        {

            std::ofstream outFile;
            outFile.open(path.c_str(), std::ios::out);
            int i = 0;
            std::string title = "";
            for (GPData gpdata : vmap)
            {

                std::map<std::string, std::string>::iterator iter;
                std::string line_content = "";
                for (iter = gpdata.values.begin(); iter != gpdata.values.end(); ++iter)
                {
                    if (i == 0)
                    {
                        title += iter->first + ",";
                    }
                    line_content += iter->second + ",";
                }
                if (i == 0)
                {
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
