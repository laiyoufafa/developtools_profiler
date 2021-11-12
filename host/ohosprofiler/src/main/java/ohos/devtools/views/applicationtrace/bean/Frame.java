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
import ohos.devtools.views.trace.AbstractNode;
import ohos.devtools.views.trace.Common;
import ohos.devtools.views.trace.util.Utils;

import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Frame chart node
 *
 * @since 2021/5/18 23:18
 */
public class Frame extends AbstractNode {
    /**
     * one frame time
     */
    public static final long ONE_FRAME_TIME = 16L;

    private long startNs = 0L;
    private long dur = 0L;
    private long mainThreadCpu = 0L;
    private long mainThreadWall = 0L;
    private long renderThreadWall = 0L;
    private long renderThreadCpu = 0L;
    private long total = 0L;
    private List<Func> renderList;

    /**
     * structure
     *
     * @param func func
     */
    public Frame(Func func) {
        startNs = func.getStartTs();
        dur = func.getDur();
        total = dur;
        mainThreadWall = func.getDur();
        mainThreadCpu = func.getRunning();
    }

    /**
     * Gets the value of startNs .
     *
     * @return the value of startNs .
     */
    public long getStartNs() {
        return startNs;
    }

    /**
     * Sets the startNs .
     * <p>You can use getStartNs() to get the value of startNs.</p>
     *
     * @param param .
     */
    public void setStartNs(final long param) {
        this.startNs = param;
    }

    /**
     * Gets the value of dur .
     *
     * @return the value of dur .
     */
    public long getDur() {
        return dur;
    }

    /**
     * Sets the dur .
     * <p>You can use getDur() to get the value of dur.</p>
     *
     * @param param .
     */
    public void setDur(final long param) {
        this.dur = param;
    }

    /**
     * Gets the value of mainThreadCpu .
     *
     * @return the value of mainThreadCpu .
     */
    public long getMainThreadCpu() {
        return mainThreadCpu;
    }

    /**
     * Sets the mainThreadCpu .
     * <p>You can use getMainThreadCpu() to get the value of mainThreadCpu.</p>
     *
     * @param param .
     */
    public void setMainThreadCpu(final long param) {
        this.mainThreadCpu = param;
    }

    /**
     * Gets the value of mainThreadWall .
     *
     * @return the value of mainThreadWall .
     */
    public long getMainThreadWall() {
        return mainThreadWall;
    }

    /**
     * Sets the mainThreadWall .
     * <p>You can use getMainThreadWall() to get the value of mainThreadWall.</p>
     *
     * @param param .
     */
    public void setMainThreadWall(final long param) {
        this.mainThreadWall = param;
    }

    /**
     * Gets the value of renderThreadWall .
     *
     * @return the value of renderThreadWall .
     */
    public long getRenderThreadWall() {
        return renderThreadWall;
    }

    /**
     * Sets the renderThreadWall .
     * <p>You can use getRenderThreadWall() to get the value of renderThreadWall.</p>
     *
     * @param param .
     */
    public void setRenderThreadWall(final long param) {
        this.renderThreadWall = param;
    }

    /**
     * Gets the value of renderThreadCpu .
     *
     * @return the value of renderThreadCpu .
     */
    public long getRenderThreadCpu() {
        return renderThreadCpu;
    }

    /**
     * Sets the renderThreadCpu .
     * <p>You can use getRenderThreadCpu() to get the value of renderThreadCpu.</p>
     *
     * @param param .
     */
    public void setRenderThreadCpu(final long param) {
        this.renderThreadCpu = param;
    }

    /**
     * Gets the value of total .
     *
     * @return the value of total .
     */
    public long getTotal() {
        return total;
    }

    /**
     * Sets the total .
     * <p>You can use getTotal() to get the value of total.</p>
     *
     * @param param .
     */
    public void setTotal(final long param) {
        this.total = param;
    }

    /**
     * setRenderList
     *
     * @return List of Func
     */
    public List<Func> getRenderList() {
        return renderList;
    }

    /**
     * setRenderList
     *
     * @param renderList renderList
     */
    public void setRenderList(final List<Func> renderList) {
        this.renderList = renderList;
        long renderTotal = renderList.stream().filter(
                func -> !(startNs <= func.getStartTs() && (startNs + dur) >= (func.getStartTs() + func.getDur())))
            .mapToLong(Func::getDur).sum();
        if (renderTotal > 0) {
            long renderRuning = renderList.stream().mapToLong(Func::getRunning).sum();
            total = dur + renderTotal;
            renderThreadWall = renderTotal;
            renderThreadCpu = renderRuning;
        }
    }

    @Override
    public void draw(Graphics2D graphics) {
        if (isMouseIn) {
            Common.setAlpha(graphics, 0.7F);
        } else {
            Common.setAlpha(graphics, 1.0F);
        }
        if (TimeUnit.NANOSECONDS.toMillis(total) >= ONE_FRAME_TIME) {
            graphics.setColor(JBUI.CurrentTheme.Label.foreground());
            Common.drawStringCenter(graphics, TimeUtils.getTimeWithUnit(total), rect);
            graphics.setColor(JBUI.CurrentTheme.Validator.errorBackgroundColor());
        } else {
            graphics.setColor(JBUI.CurrentTheme.Label.foreground());
        }
        graphics.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
        Common.setAlpha(graphics, 1.0F);
    }

    @Override
    public List<String> getStringList(String time) {
        if (renderThreadWall == 0L && renderThreadCpu == 0L) {
            return Arrays.asList(time,
                "Main Thread",
                "CPU Time: " + TimeUtils.getTimeWithUnit(mainThreadCpu),
                "Wall Time: " + TimeUtils.getTimeWithUnit(mainThreadWall));
        } else {
            return Arrays.asList(time,
                "Total Time: " + TimeUtils.getTimeWithUnit(total),
                "",
                "Main Thread", "CPU Time: " + TimeUtils.getTimeWithUnit(mainThreadCpu),
                "Wall Time: " + TimeUtils.getTimeWithUnit(mainThreadWall),
                "",
                "Render Thread",
                "CPU Time: " + TimeUtils.getTimeWithUnit(renderThreadCpu),
                "Wall Time: " + TimeUtils.getTimeWithUnit(renderThreadWall));
        }
    }
}
