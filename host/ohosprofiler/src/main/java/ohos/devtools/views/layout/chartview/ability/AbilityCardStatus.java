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

package ohos.devtools.views.layout.chartview.ability;

import java.awt.Color;

import static ohos.devtools.views.common.ColorConstants.ABILITY_ACTIVE_COLOR;
import static ohos.devtools.views.common.ColorConstants.ABILITY_COLOR;
import static ohos.devtools.views.common.ColorConstants.ABILITY_INITIAL_COLOR;

/**
 * AbilityCard Status Enum
 *
 * @since 2021/10/25
 */
public enum AbilityCardStatus {
    /**
     * initial
     */
    INITIAL(" - stopped - saved", ABILITY_INITIAL_COLOR),

    /**
     * inactive
     */
    INACTIVE(" - destroyed", ABILITY_COLOR),

    /**
     * active
     */
    ACTIVE("", ABILITY_ACTIVE_COLOR);

    private final String status;
    private final Color color;

    AbilityCardStatus(String status, Color color) {
        this.status = status;
        this.color = color;
    }

    public String getStatus() {
        return status;
    }

    public Color getColor() {
        return color;
    }
}
