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

package ohos.devtools.views.layout.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * 文件工具类
 *
 * @version 1.0
 * @date 2021/2/2 19:02
 **/
public class FileUtil {
    // 日志
    private static final Logger LOGGER = LogManager.getLogger(FileUtil.class);

    /**
     * 保存日志内容到txt中
     *
     * @param path    path
     * @param content content
     */
    public void writeFile(String path, String content) {
        File writefile;
        // 通过这个对象来判断是否向文本文件中追加内容
        writefile = new File(path);
        // 如果文本文件不存在则创建它
        if (!writefile.exists()) {
            try {
                writefile.createNewFile();
                writefile = new File(path);
            } catch (IOException exception) {
                LOGGER.error("createNewFile" + exception.getMessage());
            }
        }
        try (FileOutputStream fw = new FileOutputStream(writefile, true);
            Writer out = new OutputStreamWriter(fw, "utf-8")) {
            // 重新实例化
            out.write(content);
            String newline = System.getProperty("line.separator");
            // 写入换行
            out.write(newline);
        } catch (IOException exception) {
            LOGGER.error("createNewFile" + exception.getMessage());
        }
    }

    /**
     * 读txt 文件
     *
     * @param filePath filePath
     * @return String
     */
    public String readTxtFile(String filePath) {
        String end = null;
        InputStreamReader read = null;
        try {
            String encoding = "utf-8";
            String lineTxt = null;
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                // 判断文件是否存在
                read = new InputStreamReader(new FileInputStream(file), encoding);
                // 考虑到编码格式
                try (BufferedReader bufferedReader = new BufferedReader(read)) {
                    lineTxt = bufferedReader.readLine();
                    while (lineTxt != null) {
                        end = end + lineTxt + File.separator;
                        lineTxt = bufferedReader.readLine();
                    }
                }
                read.close();
            }
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        } finally {
            try {
                if (read != null) {
                    read.close();
                }
            } catch (IOException exception) {
                LOGGER.error("createNewFile" + exception.getMessage());
            }
        }
        return end.toString();
    }
}
