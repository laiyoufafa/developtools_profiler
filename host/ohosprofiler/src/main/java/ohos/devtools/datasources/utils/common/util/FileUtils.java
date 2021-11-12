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

package ohos.devtools.datasources.utils.common.util;

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * FileUtils
 *
 * @since 2021/08/06 15:22
 */
public final class FileUtils {
    private static final Logger LOGGER = LogManager.getLogger(FileUtils.class);

    private static final String TMP_DIR = SessionManager.getInstance().tempPath();

    private FileUtils() {
    }

    /**
     * unzipTarFile
     *
     * @param file file
     * @return List<String>
     */
    public static List<String> unzipTarFile(File file) {
        List<String> fileNames = new ArrayList<>();
        if (Objects.nonNull(file)) {
            TarArchiveInputStream tarArchiveInputStream = null;
            FileOutputStream fileOutputStream = null;
            try {
                tarArchiveInputStream = new TarArchiveInputStream(new FileInputStream(file));
                while (true) {
                    TarArchiveEntry nextTarEntry = tarArchiveInputStream.getNextTarEntry();
                    if (Objects.isNull(nextTarEntry)) {
                        break;
                    }
                    fileNames.add(nextTarEntry.getName());
                    if (nextTarEntry.isDirectory()) {
                        new File(TMP_DIR + nextTarEntry.getName()).mkdirs();
                    } else {
                        File pluginFile = new File(TMP_DIR + nextTarEntry.getName());
                        if (!pluginFile.getParentFile().exists()) {
                            pluginFile.getParentFile().mkdirs();
                        }
                        if (!pluginFile.exists()) {
                            pluginFile.createNewFile();
                        }
                        fileOutputStream = new FileOutputStream(pluginFile);
                        byte[] bs = new byte[1024];
                        int len = -1;
                        while ((len = tarArchiveInputStream.read(bs)) != -1) {
                            fileOutputStream.write(bs, 0, len);
                        }
                        fileOutputStream.flush();
                    }
                }
            } catch (IOException ioException) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("uzipFile Exception ", ioException);
                }
            } finally {
                closeStream(tarArchiveInputStream);
                closeStream(fileOutputStream);
            }
        }
        return fileNames;
    }

    private static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ioException) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("uzipFile Exception ", ioException);
                }
            }
        }
    }
}
