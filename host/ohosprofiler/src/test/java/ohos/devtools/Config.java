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

package ohos.devtools;

/**
 * Config
 *
 * @since 2021/09/03 11:25
 */
public class Config {
    /**
     * TRACE SYS
     */
    public static final String TRACE_SYS = Config.class.getResource("/trace_db/trace_sys.db").getFile();

    /**
     * TRACE APP
     */
    public static final String TRACE_APP = Config.class.getResource("/trace_db/trace_app.db").getFile();

    /**
     * TRACE CPU
     */
    public static final String TRACE_CPU = Config.class.getResource("/trace_db/trace_cpu.db").getFile();

    /**
     * TRACE PREF
     */
    public static final String TRACE_PREF = Config.class.getResource("/trace_db/trace_pref.db").getFile();

    /**
     * TRACE DISTRIBUTED A
     */
    public static final String TRACE_DISTRIBUTED_A = Config
        .class.getResource("/trace_db/trace_distributed_deviceA.db").getFile();

    /**
     * TRACE DISTRIBUTED B
     */
    public static final String TRACE_DISTRIBUTED_B = Config
        .class.getResource("/trace_db/trace_distributed_deviceB.db").getFile();
}
