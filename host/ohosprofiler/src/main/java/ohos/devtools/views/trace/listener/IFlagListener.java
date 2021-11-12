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

package ohos.devtools.views.trace.listener;

import ohos.devtools.views.trace.bean.FlagBean;

/**
 * Small flag change callback
 *
 * @since 2021/04/22 12:25
 */
public interface IFlagListener {
    /**
     * Remove the small flag callback.
     *
     * @param flag flag
     */
    void flagRemove(FlagBean flag);

    /**
     * The color of the small flag and the name change callback.
     *
     * @param flag flag
     */
    void flagChange(FlagBean flag);
}
