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

package ohos.devtools.views.layout.chartview.memory.heapdump;

/**
 * ContainType 0 OK 1 There are keywords 2 children there are keywords 3 there are no keywords
 *
 * @since 2021/11/22
 */
public enum ContainType {
    OK(0), CONTAIN(1), CHILDREN(2), NOKEY(3);
    private final int containType;

    /**
     * ContainType
     *
     * @param containType containType
     */
    ContainType(int containType) {
        this.containType = containType;
    }

    /**
     * getContainType
     *
     * @return int
     */
    public int getContainType() {
        return containType;
    }
}
