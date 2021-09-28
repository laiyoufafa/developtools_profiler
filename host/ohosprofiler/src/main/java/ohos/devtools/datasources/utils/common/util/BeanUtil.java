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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import ohos.devtools.datasources.transport.grpc.service.HiperfCallPluginConfigOuterClass;
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.CodedOutputStream;

import ohos.devtools.datasources.transport.grpc.service.BytracePluginConfigOuterClass;
import ohos.devtools.datasources.transport.grpc.service.TracePluginConfigOuterClass;
import ohos.devtools.views.common.LayoutConstants;

/**
 * Bean object utilities class.
 */
public class BeanUtil {
    private static final Logger LOGGER = LogManager.getLogger(BeanUtil.class);

    /**
     * Serializes object data.
     *
     * @param <T> <T>
     * @param data Indicates the data to be deserialized.
     * @return byte[]
     */
    public static <T> byte[] serialize(T data) {
        byte[] dataArray = null;
        // 1. Create an OutputStream object.
        // 2. Create an OutputStream wrapper object named ObjectOutputStream,
        // with the object wwritten to the OutputStream.
        try (ByteArrayOutputStream outPutStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outPutStream)) {
            // 3. Write the object to OutputStream.
            objectOutputStream.writeObject(data);
            // 4. Convert OutputStream to a byte array.
            dataArray = outPutStream.toByteArray();
        } catch (IOException exception) {
            LOGGER.error("exception error {}", exception.getMessage());
        }
        return dataArray;
    }

    /**
     * Serialize data by code output stream.
     *
     * @param config Indicates the configuration data.
     * @return Returns a byte array.
     */
    public static byte[] serializeByCodedOutPutStream(TracePluginConfigOuterClass.TracePluginConfig config) {
        byte[] dataArray = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
            config.writeTo(codedOutputStream);
            codedOutputStream.flush();
            dataArray = outputStream.toByteArray();
        } catch (IOException exception) {
            LOGGER.error("exception error {}", exception.getMessage());
        }
        return dataArray;
    }

    /**
     * Serialize data by code output stream.
     *
     * @param config Indicates the configuration data.
     * @return Returns a byte array.
     */
    public static byte[] serializeByCodedOutPutStream(HiperfCallPluginConfigOuterClass.HiperfCallPluginConfig config) {
        byte[] dataArray = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
            config.writeTo(codedOutputStream);
            codedOutputStream.flush();
            dataArray = outputStream.toByteArray();
        } catch (IOException exception) {
            LOGGER.error("exception error {}", exception.getMessage());
        }
        return dataArray;
    }

    /**
     * Serialize data by code output stream.
     *
     * @param config Indicates the configuration data.
     * @return Returns a byte array.
     */
    public static byte[] serializeByCodedOutPutStream(MemoryPluginConfig.MemoryConfig config) {
        byte[] dataArray = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
            config.writeTo(codedOutputStream);
            codedOutputStream.flush();
            dataArray = outputStream.toByteArray();
        } catch (IOException exception) {
            LOGGER.error("exception error {}", exception.getMessage());
        }
        return dataArray;
    }

    /**
     * Serialize by code output stream.
     *
     * @param config Indicates the configuration data.
     * @return Returns a byte array.
     */
    public static byte[] serializeByCodedOutPutStream(BytracePluginConfigOuterClass.BytracePluginConfig config) {
        byte[] dataArray = null;
        com.google.protobuf.CodedOutputStream codedOutputStream = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            codedOutputStream = CodedOutputStream.newInstance(outputStream, config.getSerializedSize());
            config.writeTo(codedOutputStream);
            codedOutputStream.flush();
            dataArray = outputStream.toByteArray();
        } catch (IOException exception) {
            LOGGER.error("exception error {}", exception.getMessage());
        }
        return dataArray;
    }

    /**
     * Deserializes object data.
     *
     * @param data Indicates the data to be deserialized.
     * @return Object Returns the object value.
     */
    public static Object deserialize(byte[] data) {
        Object object = null;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            object = objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            LOGGER.error("exception error {}", exception.getMessage());
        }
        return object;
    }

    /**
     * Get Fields information.
     *
     * @param <T> <T>
     * @param obj Indicates the object.
     * @return List Map
     */
    public static <T> List<Map> getFieldsInfo(T obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        List list = new ArrayList();
        Map infoMap = null;
        for (int index = 0; index < fields.length; index++) {
            infoMap = new HashMap(CommonUtil.collectionSize(LayoutConstants.SIXTEEN));
            infoMap.put("type", fields[index].getType().toString());
            Optional<Object> fieldName = getFieldValueByName(fields[index].getName(), obj);
            if (fieldName.isPresent()) {
                infoMap.put(fields[index].getName(), fieldName.get());
            }
            list.add(infoMap);
        }
        return list;
    }

    /**
     * Returns a list of filed values.
     *
     * @param <T> <T>
     * @param obj Indicates the object.
     * @return List Map
     */
    public static <T> List<Map<String, Object>> getFields(T obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        List list = new ArrayList();
        Map infoMap = null;
        for (int index = 0; index < fields.length; index++) {
            infoMap = new HashMap(CommonUtil.collectionSize(LayoutConstants.SIXTEEN));
            infoMap.put("type", fields[index].getType().toString());
            infoMap.put("name", fields[index].getName());
            Optional<Object> fieldName = getFieldValueByName(fields[index].getName(), obj);
            if (fieldName.isPresent()) {
                infoMap.put("value", fieldName.get());
            }
            list.add(infoMap);
        }
        return list;
    }

    /**
     * Gets Fields information.
     *
     * @param obj Indicates the object.
     * @param <T> <T>
     * @return Returns a list of fields.
     */
    public static <T> Map<String, Object> getFiledsInfos(T obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        Map infoMap = new HashMap(CommonUtil.collectionSize(LayoutConstants.SIXTEEN));
        for (int index = 0; index < fields.length; index++) {
            Optional<Object> fieldName = getFieldValueByName(fields[index].getName(), obj);
            if (fieldName.isPresent()) {
                infoMap.put(fields[index].getName(), fieldName.get());
            }
        }
        return infoMap;
    }

    /**
     * Gets attribute values by attribute name.
     *
     * @param obj Indicates the object.
     * @param <T> <T>
     * @return Returns a list of attribute names.
     */
    public static <T> List<String> getObjectAttributeNames(T obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        List list = new ArrayList();
        for (int index = 0; index < fields.length; index++) {
            list.add(fields[index].getName());
        }
        return list;
    }

    /**
     * Get the attribute value by attribute name.
     *
     * @param <T> <T>
     * @param fieldName fieldName
     * @param object Indicates the object.
     * @return Object Returns the object value.
     */
    private static <T> Optional getFieldValueByName(String fieldName, T object) {
        String firstLetter = fieldName.substring(0, 1).toUpperCase(Locale.ENGLISH);
        String getter = "get" + firstLetter + fieldName.substring(1);
        if ("getSerialVersionUID".equals(getter)) {
            return Optional.empty();
        }
        Method method = null;
        Object value = null;
        try {
            method = object.getClass().getMethod(getter, new Class[] {});
            value = method.invoke(object, new Object[] {});
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException exception) {
            LOGGER.error(exception.getMessage());
        }

        return Optional.of(value);
    }

    /**
     * Gets the value of a given object.
     *
     * @param object object
     * @param <T> <T>
     * @return List <String>
     */
    public static <T> List<String> getObjectValue(T object) {
        if (object != null && object instanceof Serializable) {
            Class<?> objectClass = object.getClass();
            Field[] declaredFields = objectClass.getDeclaredFields();
            ArrayList<String> list = new ArrayList<>();
            for (Field field : declaredFields) {
                Type type = field.getGenericType();
                String colName = field.getName();
                if ("class java.lang.String".equals(type.toString())) {
                    list.add(colName + " varchar(100)");
                }
                if ("class java.lang.Integer".equals(type.toString()) || "int".equals(type.toString())) {
                    list.add(colName + " int");
                }
                if ("class java.lang.Long".equals(type.toString()) || "long".equals(type.toString())) {
                    list.add(colName + " long");
                }
                if ("class java.lang.Double".equals(type.toString()) || "double".equals(type.toString())) {
                    list.add(colName + " double");
                }
                if ("class java.lang.Boolean".equals(type.toString()) || "boolean".equals(type.toString())) {
                    list.add(colName + " boolean");
                }
                if ("class java.util.Date".equals(type.toString())) {
                    list.add(colName + " date");
                }
            }
            return list;
        }
        return new ArrayList<>();
    }

    /**
     * Get the object name.
     *
     * @param object Indicates the object.
     * @param <T> <T>
     * @return String
     */
    public static <T> String getObjectName(T object) {
        if (object != null) {
            return object.getClass().getSimpleName();
        }
        return "";
    }
}
