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

package ohos.devtools.views.distributed.bean;

import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.distributed.component.DistributedTracePanel;
import ohos.devtools.views.distributed.util.DistributedCommon;
import ohos.devtools.views.trace.AbstractNode;
import ohos.devtools.views.trace.DField;
import ohos.devtools.views.trace.EventDispatcher;
import ohos.devtools.views.trace.util.ColorUtils;
import ohos.devtools.views.trace.util.Utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * DistributedFuncBean
 *
 * @since 2021/5/19 16:39
 */
public class DistributedFuncBean extends AbstractNode {
    /**
     * currentSelectedFunc current select func
     */
    public static DistributedFuncBean currentSelectedFunc;

    @DField(name = "id")
    private Integer id;
    @DField(name = "parent_id")
    private Integer parentId;
    @DField(name = "is_main_thread")
    private Integer isMainThread;
    @DField(name = "track_id")
    private Integer trackId;
    @DField(name = "funName")
    private String funcName = "";
    @DField(name = "tid")
    private Integer tid;
    @DField(name = "depth")
    private Integer depth = 0;
    @DField(name = "threadName")
    private String threadName = "";
    @DField(name = "startTs")
    private long startTs;
    @DField(name = "dur")
    private long dur;
    @DField(name = "chainId")
    private String chainId;
    @DField(name = "spanId")
    private Integer spanId;
    @DField(name = "parentSpanId")
    private Integer parentSpanId;
    @DField(name = "flag")
    private String flag;
    private Long delay;
    private BeanDataType currentType;
    private boolean isSelected = false; // Whether to be selected
    @DField(name = "args")
    private String args;

    /**
     * isSelected
     *
     * @return boolean isSelected
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * setSelected
     *
     * @param selected isSelected
     */
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    /**
     * getSpanId
     *
     * @return spanId spanId
     */
    public Integer getSpanId() {
        return spanId;
    }

    /**
     * setSpanId
     *
     * @param spanId spanId
     */
    public void setSpanId(Integer spanId) {
        this.spanId = spanId;
    }

    /**
     * getParentSpanId
     *
     * @return parentSpanId parentSpanId
     */
    public Integer getParentSpanId() {
        return parentSpanId;
    }

    /**
     * getParentSpanId
     *
     * @param parentSpanId parentSpanId
     */
    public void setParentSpanId(Integer parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    /**
     * getFlag
     *
     * @return flag flag
     */
    public String getFlag() {
        return flag;
    }

    /**
     * setFlag
     *
     * @param flag flag
     */
    public void setFlag(String flag) {
        this.flag = flag;
    }

    /**
     * getArgs
     *
     * @return args args
     */
    public String getArgs() {
        return args;
    }

    /**
     * setArgs
     *
     * @param args args
     */
    public void setArgs(String args) {
        this.args = args;
    }

    /**
     * getId
     *
     * @return id id
     */
    public Integer getId() {
        return id;
    }

    /**
     * setId
     *
     * @param id id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * getParentId
     *
     * @return parentId parentId
     */
    public Integer getParentId() {
        return parentId;
    }

    /**
     * setParentId
     *
     * @param parentId parentId
     */
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    /**
     * getIsMainThread
     *
     * @return isMainThread isMainThread
     */
    public Integer getIsMainThread() {
        return isMainThread;
    }

    /**
     * setIsMainThread
     *
     * @param isMainThread isMainThread
     */
    public void setIsMainThread(Integer isMainThread) {
        this.isMainThread = isMainThread;
    }

    /**
     * getTrackId
     *
     * @return trackId trackId
     */
    public Integer getTrackId() {
        return trackId;
    }

    /**
     * setTrackId
     *
     * @param trackId trackId
     */
    public void setTrackId(Integer trackId) {
        this.trackId = trackId;
    }

    /**
     * getFuncName
     *
     * @return funcName funcName
     */
    public String getFuncName() {
        return funcName;
    }

    /**
     * setFuncName
     *
     * @param funcName funcName
     */
    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    /**
     * getTid
     *
     * @return tid tid
     */
    public Integer getTid() {
        return tid;
    }

    /**
     * setTid
     *
     * @param tid tid
     */
    public void setTid(Integer tid) {
        this.tid = tid;
    }

    /**
     * getDepth
     *
     * @return depth depth
     */
    public Integer getDepth() {
        return depth;
    }

    /**
     * setDepth
     *
     * @param depth depth
     */
    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    /**
     * getThreadName
     *
     * @return threadName threadName
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * setThreadName
     *
     * @param threadName threadName
     */
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    /**
     * getStartTs
     *
     * @return startTs startTs
     */
    public long getStartTs() {
        return startTs;
    }

    /**
     * setStartTs
     *
     * @param startTs startTs
     */
    public void setStartTs(long startTs) {
        this.startTs = startTs;
    }

    /**
     * getDur
     *
     * @return dur dur
     */
    public Long getDur() {
        return dur;
    }

    /**
     * setDur
     *
     * @param dur dur
     */
    public void setDur(Long dur) {
        this.dur = dur;
    }

    /**
     * getChainId
     *
     * @return chainId chainId
     */
    public String getChainId() {
        return chainId;
    }

    /**
     * setChainId
     *
     * @param chainId chainId
     */
    public void setChainId(String chainId) {
        this.chainId = chainId;
    }

    /**
     * getCurrentType
     *
     * @return currentType currentType
     */
    public BeanDataType getCurrentType() {
        return currentType;
    }

    /**
     * setCurrentType
     *
     * @param currentType currentType
     */
    public void setCurrentType(BeanDataType currentType) {
        this.currentType = currentType;
    }

    /**
     * getDelay
     *
     * @return delay delay
     */
    public Long getDelay() {
        return delay;
    }

    /**
     * setDelay
     *
     * @param delay delay
     */
    public void setDelay(Long delay) {
        this.delay = delay;
    }

    /**
     * getEndTs
     *
     * @return startTs + dur
     */
    public long getEndTs() {
        if (startTs != 0 && dur != 0) {
            return startTs + dur;
        }
        return 0;
    }

    @Override
    public void draw(Graphics2D graphics) {
        if (depth == -1) {
            return;
        }
        if (isMouseIn) {
            DistributedCommon.setAlpha(graphics, 0.7F);
        } else {
            DistributedCommon.setAlpha(graphics, 1.0F);
        }
        if (isSelected) {
            graphics.setColor(Color.black);
            graphics.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
            graphics.setColor(ColorUtils.FUNC_COLOR[depth % ColorUtils.FUNC_COLOR.length]);
            graphics.fillRect(Utils.getX(rect) + 2, Utils.getY(rect) + 2, rect.width - 4, rect.height - 4);
            graphics.setColor(Color.white);
            Rectangle rectangle = new Rectangle();
            rectangle.setRect(rect.getX() + 2, rect.getY() + 2, rect.getWidth() - 4, rect.getHeight() - 4);
            DistributedCommon.drawStringCenter(graphics, funcName, rectangle);
        } else {
            graphics.setColor(ColorUtils.FUNC_COLOR[depth % ColorUtils.FUNC_COLOR.length]);
            graphics.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
            graphics.setColor(Color.white);
            DistributedCommon.drawStringCenter(graphics, funcName, rect);
        }
        DistributedCommon.setAlpha(graphics, 1.0F);
    }

    @Override
    public List<String> getStringList(String time) {
        return Arrays.asList(time, "Start：" + TimeUtils.getTimeWithUnit(getStartTs()),
            "End：" + TimeUtils.getTimeWithUnit(getEndTs()), "Dur：" + TimeUtils.getTimeWithUnit(getDur()),
            "" + getFuncName());
    }

    @Override
    public void onClick(MouseEvent event) {
        super.onClick(event);
        if (Objects.nonNull(currentSelectedFunc)) {
            currentSelectedFunc.setSelected(false);
        }
        this.setSelected(true);
        currentSelectedFunc = this;
        DistributedTracePanel.root.repaint();
        EventDispatcher.dispatcherClickListener(this);
    }

    /**
     * enum BeanDataType A or B
     */
    public enum BeanDataType {
        TYPE_A, TYPE_B
    }
}
