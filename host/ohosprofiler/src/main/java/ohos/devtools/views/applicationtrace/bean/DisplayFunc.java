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

package ohos.devtools.views.applicationtrace.bean;

import com.intellij.util.ui.JBUI;
import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.trace.Common;
import ohos.devtools.views.trace.util.Utils;

import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;

/**
 * DisplayFunc
 *
 * @version 1.0
 * @date: 2021/5/13 13:22
 */
public class DisplayFunc extends Func {
    /**
     * Construction
     *
     * @param func function class
     */
    public DisplayFunc(Func func) {
        setCategory(func.getCategory());
        setDur(func.getDur());
        setDepth(func.getDepth());
        setFuncName(func.getFuncName());
        setTid(func.getTid());
        setParentId(func.getParentId());
        setStackId(func.getTrackId());
        setParentStackId(func.getParentStackId());
        setThreadName(func.getThreadName());
        setId(func.getId());
        setStartTs(func.getStartTs());
    }

    @Override
    public void draw(Graphics2D graphics) {
        if (isMouseIn) {
            Common.setAlpha(graphics, 0.7F);
        } else {
            Common.setAlpha(graphics, 1.0F);
        }
        graphics.setColor(JBUI.CurrentTheme.Label.foreground());
        graphics.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
        Common.setAlpha(graphics, 1.0F);
    }

    @Override
    public List<String> getStringList(String time) {
        return Arrays.asList(
            time,
            "" + getFuncName(),
            "",
            TimeUtils.getTimeWithUnit(getDur())
        );
    }

}
