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

package ohos.devtools.datasources.databases.databasepool;

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data results help
 *
 * @since 2021/10/22 17:00
 */
public class DataBaseRsHelp<T> {
    private static final Logger LOGGER = LogManager.getLogger(DataBaseRsHelp.class);

    /**
     * Get the database table name by SQL.
     *
     * @param instanceClass generic paradigm
     * @param rs result set
     * @return List <T> T
     * @throws IllegalAccessException IllegalAccessException
     * @throws InstantiationException InstantiationException
     * @throws NoSuchFieldException NoSuchFieldException
     */
    public List<T> util(T instanceClass, ResultSet rs)
        throws IllegalAccessException, InstantiationException, NoSuchFieldException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("util");
        }
        List<T> list = new ArrayList<T>();
        Class aClass = instanceClass.getClass();
        Field[] fs = aClass.getDeclaredFields();
        if (rs != null) {
            while (true) {
                try {
                    if (!rs.next()) {
                        break;
                    }
                    T instance = (T) aClass.newInstance();
                    for (int index = 0; index < fs.length; index++) {
                        Field declaredField = instance.getClass().getDeclaredField(fs[index].getName());
                        declaredField.setAccessible(true);
                        if (declaredField.getType().getName().equals(String.class.getName())) {
                            declaredField.set(instance, rs.getString(fs[index].getName()));
                        } else if (declaredField.getType().getName().equals(int.class.getName())) {
                            declaredField.set(instance, rs.getInt(fs[index].getName()));
                        } else {
                            continue;
                        }
                    }
                    list.add(instance);
                } catch (SQLException throwAbles) {
                    LOGGER.error(throwAbles.getMessage());
                }
            }
        }
        // Return results
        return list;
    }

}
