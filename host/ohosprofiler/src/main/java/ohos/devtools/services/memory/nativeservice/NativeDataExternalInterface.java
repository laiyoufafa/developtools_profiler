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

package ohos.devtools.services.memory.nativeservice;

import ohos.devtools.services.memory.nativebean.NativeFrame;
import ohos.devtools.services.memory.nativebean.NativeInstanceObject;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * NativeDataExternalInterface
 *
 * @since: 2021/9/20
 */
public class NativeDataExternalInterface {
    private static final Logger LOGGER = LogManager.getLogger(NativeDataExternalInterface.class);

    private MultiValueMap nativeInstanceMap = new MultiValueMap();

    /**
     * addr Function
     */
    private Map<String, String> addrFunctionMap = new HashMap<>();

    /**
     * addr, addrID
     */
    private Map<String, String> addrMap = new HashMap<>();
    private boolean start = false;

    /**
     * parseNativeFile
     *
     * @param filePath filePath
     */
    public void parseNativeFile(String filePath) {
        File file = new File(filePath);
        if (file == null || !file.isFile()) {
            return;
        }
        FileInputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = new FileInputStream(file);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            executeParseFile(bufferedReader);
        } catch (FileNotFoundException fileNotFoundException) {
            LOGGER.error("FileNotFoundException ", fileNotFoundException);
        } catch (IOException ioException) {
            LOGGER.error("ioException ", ioException);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    private void executeParseFile(BufferedReader bufferedReader) throws IOException {
        boolean isNeedNextLine = false;
        boolean isSkip = false;
        boolean startRecord = false;
        String line = null;
        NativeInstanceObject instance = null;
        while ((line = bufferedReader.readLine()) != null) {
            if ((!line.startsWith("malloc")) && (!start)) {
                continue;
            }
            if (line.startsWith("malloc")) {
                start = true;
                instance = createInstance(line);
                isNeedNextLine = true;
                isSkip = false;
            }
            if ((!line.startsWith("malloc")) && (!line.startsWith("free")) && (!isSkip)) {
                if (line.contains("malloc")) {
                    startRecord = true;
                }
                if (!startRecord) {
                    continue;
                }
                NativeFrame nativeFrame = createNativeFrame(line);
                if (instance != null) {
                    instance.addNativeFrames(nativeFrame);
                    if (isNeedNextLine) {
                        instance.setFileName(nativeFrame.getFileName());
                        instance.setFunctionName(nativeFrame.getFunctionName());
                        addrFunctionMap.put(instance.getAddr(), instance.getFunctionName());
                        isNeedNextLine = false;
                        if (!instance.isAdd()) {
                            nativeInstanceMap.put(instance.getFunctionName(), instance);
                            instance.setAdd(true);
                        }
                    }
                }
            }
            if (line.startsWith("free")) {
                startRecord = false;
                String[] split = line.split(",");
                String freeAddr = split[3];
                String addrId = getAddrId(freeAddr);
                String freeFunction = addrFunctionMap.get(addrId);
                updateFreeInstance(freeAddr, freeFunction);
                isSkip = true;
            }
        }
    }

    private void updateFreeInstance(String freeAddr, String freeFunction) {
        String addrId = getAddrId(freeAddr);
        Collection collection = nativeInstanceMap.getCollection(freeFunction);
        if (collection != null) {
            Iterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                Object next = iterator.next();
                if (next instanceof NativeInstanceObject) {
                    NativeInstanceObject nativeInstance = (NativeInstanceObject) next;
                    if (addrId == String.valueOf(nativeInstance.getAddr())) {
                        nativeInstance.setDeAllocated(true);
                    }
                }
            }
        }
    }

    private String getAddrId(String addr) {
        return addrMap.get(addr);
    }

    private String updateAddrId(String addr) {
        String value = addr + System.nanoTime();
        addrMap.put(addr, value);
        return value;
    }

    private NativeInstanceObject createInstance(String string) {
        String[] split = string.split(",");
        String addr = split[3];
        String mallocSize = split[4];
        NativeInstanceObject nativeInstanceObject = new NativeInstanceObject();
        String addrId = updateAddrId(addr);
        nativeInstanceObject.setAddr(addrId);
        nativeInstanceObject.setSize(Long.parseLong(mallocSize));
        nativeInstanceObject.setInstanceCount(1);
        return nativeInstanceObject;
    }

    private NativeFrame createNativeFrame(String string) {
        String[] split = string.split(",");
        String pc = split[0];
        String fileName = split[1];
        String functionName = split[2];
        return new NativeFrame(pc, fileName, functionName);
    }

    public MultiValueMap getNativeInstanceMap() {
        return nativeInstanceMap;
    }
}
