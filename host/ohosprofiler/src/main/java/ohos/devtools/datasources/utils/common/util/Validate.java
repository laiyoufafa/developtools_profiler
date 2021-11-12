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

/**
 * Validate
 */
public abstract class Validate {
    /**
     * validate
     *
     * @param obj obj
     * @param <T> <T>
     * @return <T>
     */
    public abstract <T> boolean validate(T obj);

    /**
     * addToList
     *
     * @param obj obj
     * @param <T> <T>
     */
    public abstract <T> void addToList(T obj);

    /**
     * batchInsertToDb
     */
    public abstract void batchInsertToDb();
}
