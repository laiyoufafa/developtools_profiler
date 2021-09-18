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

import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.trace.Common;
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

/**
 * Func
 *
 * @date: 2021/5/20 15:24
 */
public class Func extends AppFunc {
    /**
     * parent blood id
     */
    protected String parentBloodId = "";
    @DField(name = "stack_id") private long stackId;
    @DField(name = "parent_stack_id") private long parentStackId;
    @DField(name = "id") private Integer id;
    @DField(name = "parent_id") private Integer parentId;
    @DField(name = "is_main_thread") private Integer isMainThread;
    @DField(name = "track_id") private Integer trackId;
    @DField(name = "funName") private String funcName = "";
    @DField(name = "tid") private Integer tid;
    @DField(name = "depth") private Integer depth = 0;
    @DField(name = "threadName") private String threadName = "";
    @DField(name = "startTs") private long startTs;
    @DField(name = "dur") private long dur;
    private String category;
    private long running;
    private long idle;
    private boolean isSelected; // Whether to be selected

    /**
     * Gets the value of parentStackId .
     *
     * @return the value of int
     */
    public long getParentStackId() {
        return parentStackId;
    }

    /**
     * Sets the parentStackId .
     * <p>You can use getParentStackId() to get the value of parentStackId</p>
     *
     * @param parentStackId parentStackId
     */
    public void setParentStackId(long parentStackId) {
        this.parentStackId = parentStackId;
    }

    /**
     * Gets the value of stackId .
     *
     * @return the value of int
     */
    public long getStackId() {
        return stackId;
    }

    /**
     * Sets the stackId .
     * <p>You can use getStackId() to get the value of stackId</p>
     *
     * @param stackId stackId
     */
    public void setStackId(Integer stackId) {
        this.stackId = stackId;
    }

    /**
     * Gets the value of id .
     *
     * @return the value of int
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id .
     * <p>You can use getId() to get the value of id</p>
     *
     * @param id id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the value of parentId .
     *
     * @return the value of int
     */
    public Integer getParentId() {
        return parentId;
    }

    /**
     * Sets the parentId .
     * <p>You can use getParentId() to get the value of parentId</p>
     *
     * @param parentId parentId
     */
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    /**
     * Gets the value of isMainThread .
     *
     * @return the value of int
     */
    public Integer getIsMainThread() {
        return isMainThread;
    }

    /**
     * Sets the isMainThread .
     * <p>You can use getIsMainThread() to get the value of isMainThread</p>
     *
     * @param mainThread mainThread
     */
    public void setIsMainThread(final Integer mainThread) {
        this.isMainThread = mainThread;
    }

    /**
     * Gets the value of trackId .
     *
     * @return the value of int
     */
    public Integer getTrackId() {
        return trackId;
    }

    /**
     * Sets the trackId .
     * <p>You can use getTrackId() to get the value of trackId</p>
     *
     * @param id id
     */
    public void setTrackId(final Integer id) {
        this.trackId = id;
    }

    /**
     * Gets the value of category .
     *
     * @return the value of java.lang.String
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category .
     * <p>You can use getCategory() to get the value of category</p>
     *
     * @param cate cate
     */
    public void setCategory(final String cate) {
        this.category = cate;
    }

    /**
     * Gets the value of isSelected .
     *
     * @return the value of boolean
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Sets the isSelected .
     * <p>You can use getSelected() to get the value of isSelected</p>
     *
     * @param selected selected
     */
    public void setSelected(final boolean selected) {
        this.isSelected = selected;
    }

    /**
     * get depth
     *
     * @return depth
     */
    public Integer getDepth() {
        return depth;
    }

    /**
     * Set the depth .
     *
     * @param depth depth
     */
    public void setDepth(final Integer depth) {
        this.depth = depth;
    }

    /**
     * get the thread name
     *
     * @return thread name
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * set the thread name
     *
     * @param threadName thread name
     */
    public void setThreadName(final String threadName) {
        this.threadName = threadName;
    }

    /**
     * get start time
     *
     * @return start time
     */
    public long getStartTs() {
        return startTs;
    }

    /**
     * set start ts
     *
     * @param startTs start ts
     */
    public void setStartTs(final long startTs) {
        this.startTs = startTs;
    }

    /**
     * get parent blood id
     *
     * @return parent blood id
     */
    public String getParentBloodId() {
        return parentBloodId;
    }

    /**
     * set the parent blood id
     *
     * @param parentBloodId parent blood id
     */
    public void setParentBloodId(final String parentBloodId) {
        this.parentBloodId = parentBloodId;
    }

    /**
     * get the endTs
     *
     * @return endTs endTs
     */
    public long getEndTs() {
        return endTs;
    }

    /**
     * set the endTs
     *
     * @param endTs endTs
     */
    public void setEndTs(final long endTs) {
        this.endTs = endTs;
        dur = endTs - startTs;
    }

    /**
     * get the funcName
     *
     * @return funcName funcName
     */
    public String getFuncName() {
        return funcName;
    }

    /**
     * set the funcName
     *
     * @param funcName funcName
     */
    public void setFuncName(final String funcName) {
        this.funcName = funcName;
    }

    /**
     * get duration
     *
     * @return duration
     */
    public long getDur() {
        return dur;
    }

    /**
     * set duration
     *
     * @param dur duration
     */
    public void setDur(final long dur) {
        this.dur = dur;
    }

    /**
     * get thread id
     *
     * @return thread id
     */
    public Integer getTid() {
        return tid;
    }

    /**
     * set thread id
     *
     * @param tid thread id
     */
    public void setTid(final Integer tid) {
        this.tid = tid;
    }

    /**
     * Draw the corresponding shape according to the brush
     *
     * @param graphics graphics
     */
    @Override
    public void draw(final Graphics2D graphics) {
        if (depth == -1) {
            return;
        }
        if (isMouseIn) {
            Common.setAlpha(graphics, 0.7F);
        } else {
            Common.setAlpha(graphics, 1.0F);
        }
        if (isSelected) {
            graphics.setColor(Color.black);
            graphics.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
            graphics.setColor(ColorUtils.FUNC_COLOR[depth % ColorUtils.FUNC_COLOR.length]);
            graphics.fillRect(Utils.getX(rect) + 1, Utils.getY(rect) + 1, rect.width - 2, rect.height - 2);
            graphics.setColor(Color.white);
            Rectangle rectangle = new Rectangle();
            rectangle.setRect(rect.getX() + 1, rect.getY() + 1, rect.getWidth() - 2, rect.getHeight() - 2);
            Common.drawStringCenter(graphics, funcName, rectangle);
        } else {
            graphics.setColor(ColorUtils.FUNC_COLOR[depth % ColorUtils.FUNC_COLOR.length]);
            graphics.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
            graphics.setColor(Color.white);
            Common.drawStringCenter(graphics, funcName, rect);
        }
        Common.setAlpha(graphics, 1.0F);
    }

    @Override
    public List<String> getStringList(final String time) {
        return Arrays.asList(
            time,
            "" + getFuncName(),
            "Running: " + TimeUtils.getTimeWithUnit(running),
            "Idle: " + TimeUtils.getTimeWithUnit(idle),
            "Total: " + TimeUtils.getTimeWithUnit(dur)
        );
    }

    /**
     * create StackId by parentStackId、funcName and depth
     */
    @Override
    public void createBloodId() {
        if (depth == 0) {
            bloodId = Utils.md5String(threadName + funcName + depth);
            parentBloodId = Utils.md5String(threadName);
        } else {
            bloodId = Utils.md5String(parentBloodId + funcName + depth);
        }
    }

    /**
     * Gets the value of idle .
     *
     * @return the value of long
     */
    public long getIdle() {
        return idle;
    }

    /**
     * Sets the setIdle .
     * <p>You can use setIdle() to get the value of setIdle</p>
     *
     * @param idle idle
     */
    public void setIdle(final long idle) {
        this.idle = idle;
        running = dur - idle;
    }

    /**
     * get running time
     *
     * @return running time
     */
    public long getRunning() {
        return running;
    }

    /**
     * set running time
     *
     * @param running running time
     */
    public void setRunning(final long running) {
        this.running = running;
    }

    @Override
    public void onClick(MouseEvent event) {
        super.onClick(event);
        if (depth != -1) {
            EventDispatcher.dispatcherClickListener(this);
        }
    }
}
