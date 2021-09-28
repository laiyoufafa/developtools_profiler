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

import ohos.devtools.datasources.utils.common.Constant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * file SHA-256
 */
public final class FileSafeUtils {
    /**
     * Gets a logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger(FileSafeUtils.class);

    private FileSafeUtils() {
    }

    /**
     * Gets the MD5 code of the file. Available algorithmType options include MD5, SHA1, SHA-256, SHA-384, and SHA-512.
     *
     * @param file Indicates the file to obtain the MD5 code.
     * @param algorithmType Indicates the algorithm type.
     * @return Returns a string of the MD5 code.
     */
    public static String getFileSha(File file, String algorithmType) {
        if (file == null) {
            return "Sha File is null";
        }

        // Check whether the file exists.
        if (!file.isFile() || algorithmType == null) {
            LOGGER.error("Sha File is not exists : " + file.getName());
            return "Sha File is not exists or algorithmType is null";
        }

        // Define the MessageDigest and FileInputStream for encryption.
        MessageDigest messageDigest = null;
        FileInputStream fileInputStream = null;
        byte[] buffer = new byte[Constant.MB];
        int len;
        try {
            // Perform SHA algorithm encryption using file input streams.
            messageDigest = MessageDigest.getInstance(algorithmType);
            fileInputStream = new FileInputStream(file);
            while (true) {
                len = fileInputStream.read(buffer, 0, Constant.MB);
                if (len == Constant.ABNORMAL) {
                    break;
                }
                messageDigest.update(buffer, 0, len);
            }
        } catch (NoSuchAlgorithmException exception) {
            LOGGER.error("getFileMD5 fail: " + exception.getMessage());
        } catch (IOException exception) {
            LOGGER.error("getFileMD5 fail: " + exception.getMessage());
            return "";
        } finally {
            try {
                if (fileInputStream != null) {
                    // Close the file input stream.
                    fileInputStream.close();
                }
            } catch (IOException exception) {
                LOGGER.error("fileInputStream: " + exception.getMessage());
            }
        }

        // Use BigInteger.
        BigInteger bigInteger = new BigInteger(1, messageDigest.digest());
        return bigInteger.toString(Constant.RADIX);
    }

    /**
     * Sets whether to recursively search files in the sub-directories for the MD code of files.
     *
     * @param dirFile Indicates the files in the sub-directories.
     * @param algorithm Indicates the algorithm.
     * @param listChild Indicates whether to recursively search files in the sub-directories.
     * @return Returns Map<String, String>
     */
    private static Map<String, String> getDirMD5(File dirFile, String algorithm, boolean listChild) {
        if (!dirFile.isDirectory()) {
            return new HashMap<>();
        }
        Map<String, String> pathAlgMap = new HashMap<String, String>();
        String algCode;
        File[] files = dirFile.listFiles();
        for (int index = 0; index < files.length; index++) {
            File file = files[index];
            if (file.isDirectory() && listChild) {
                pathAlgMap.putAll(getDirMD5(file, algorithm, listChild));
            } else {
                algCode = getFileSha(file, algorithm);
                if (algCode != null) {
                    pathAlgMap.put(file.getPath(), algCode);
                }
            }
        }
        return pathAlgMap;
    }

    /**
     * Compares the Hash values in two files.
     *
     * @param foreFileMD5 Indicates the MD5 code of the source file.
     * @param laterFileMD5 Indicates the MD5 code of the target file.
     * @return Returns true if the MD5 codes are the same; returns false otherwise.
     */
    private static boolean checkHash(String foreFileMD5, String laterFileMD5) {
        return foreFileMD5.equals(laterFileMD5);
    }

    /**
     * Checks for file changes.
     *
     * @param foreFile Indicates the original file.
     * @param laterFile Indicates the new file.
     * @param algorithmType Indicates the algorithm type.
     * @return Returns true if there is no change in the file; returns false otherwise.
     */
    private static boolean checkChange(File foreFile, File laterFile, String algorithmType) {
        // Get the Hash value of the original and new files.
        String foreHash = getFileSha(foreFile, algorithmType);
        String laterHash = getFileSha(laterFile, algorithmType);

        // Call checkHash to compare whether there are changes in the file.
        boolean checkHash = checkHash(foreHash, laterHash);
        if (checkHash) {
            LOGGER.debug("file : {}and file: {}name and content all the same", foreFile.getName(), laterFile.getName());
        } else {
            LOGGER.debug("file: {} and file: {}name or content different", foreFile.getName(), laterFile.getName());
        }
        return checkHash;
    }
}
