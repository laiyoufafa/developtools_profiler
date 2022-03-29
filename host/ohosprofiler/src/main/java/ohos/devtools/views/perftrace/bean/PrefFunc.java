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

package ohos.devtools.views.perftrace.bean;

import com.intellij.ui.JBColor;
import ohos.devtools.views.applicationtrace.bean.AppFunc;
import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.perftrace.PerfColorUtil;
import ohos.devtools.views.perftrace.PerfData;
import ohos.devtools.views.trace.Common;
import ohos.devtools.views.trace.EventDispatcher;
import ohos.devtools.views.trace.util.Utils;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PrefFunc c++ function object
 *
 * @since 2021/5/12 16:34
 */
public class PrefFunc extends AppFunc {
    /**
     * selected pref func
     */
    private static PrefFunc selectedPrefFunc;

    private PrefFunc parentNode;
    private List<PrefFunc> childrenNodes = new ArrayList<>();
    private long fileId;
    private long symbolId;
    private long vaddrInFile = -1L;
    private boolean isFirstMerage = true;
    private boolean isUserWrite = false;
    private boolean isSelected = false;

    /**
     * PrefFunc
     */
    public PrefFunc() {
        super();
    }

    /**
     * PrefFunc
     *
     * @param sample sample
     */
    public PrefFunc(PrefSample sample) {
        this();
        merageSample(sample);
    }

    /**
     * get the isUserWrite
     *
     * @return boolean fileName
     */
    public boolean isUserWrite() {
        return isUserWrite;
    }

    /**
     * set the isUserWrite
     *
     * @param userWrite userWrite
     */
    public void setUserWrite(boolean userWrite) {
        isUserWrite = userWrite;
    }

    /**
     * get the parentNode
     *
     * @return parentNode parentNode
     */
    public PrefFunc getParentNode() {
        return parentNode;
    }

    /**
     * set the parentNode
     *
     * @param parentNode parentNode
     */
    public void setParentNode(PrefFunc parentNode) {
        this.parentNode = parentNode;
    }

    /**
     * get the ChildrenNodes
     *
     * @return childrenNodes
     */
    public List<PrefFunc> getChildrenNodes() {
        return childrenNodes;
    }

    /**
     * set the ChildrenNodes
     *
     * @param childrenNodes childrenNodes
     */
    public void setChildrenNodes(List<PrefFunc> childrenNodes) {
        this.childrenNodes = childrenNodes;
    }

    /**
     * get the fileId
     *
     * @return fileId fileId
     */
    public long getFileId() {
        return fileId;
    }

    /**
     * set the fileId
     *
     * @param fileId fileId
     */
    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    /**
     * get the symbolId
     *
     * @return symbolId symbolId
     */
    public long getSymbolId() {
        return symbolId;
    }

    /**
     * set the symbolId
     *
     * @param symbolId symbolId
     */
    public void setSymbolId(long symbolId) {
        this.symbolId = symbolId;
    }

    /**
     * get the vaddrInFile
     *
     * @return vaddrInFile vaddrInFile
     */
    public long getVaddrInFile() {
        return vaddrInFile;
    }

    /**
     * set the vaddrInFile
     *
     * @param vaddrInFile vaddrInFile
     */
    public void setVaddrInFile(long vaddrInFile) {
        this.vaddrInFile = vaddrInFile;
    }

    /**
     * setSelectedPrefFunc
     *
     * @param selectedPrefFunc selectedPrefFunc
     */
    public static void setSelectedPrefFunc(PrefFunc selectedPrefFunc) {
        PrefFunc.selectedPrefFunc = selectedPrefFunc;
    }

    /**
     * get the startTs
     *
     * @return startTs startTs
     */
    public long getStartTs() {
        return startTs;
    }

    /**
     * set the startTs
     *
     * @param startTs startTs
     */
    public void setStartTs(long startTs) {
        this.startTs = startTs;
    }

    /**
     * get the dur
     *
     * @return startTs startTs
     */
    public long getDur() {
        return dur;
    }

    /**
     * set the dur
     *
     * @param dur dur
     */
    public void setDur(long dur) {
        this.dur = dur;
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
    public void setEndTs(long endTs) {
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
    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    /**
     * get the isFirstMerage
     *
     * @return isFirstMerage
     */
    public boolean isFirstMerage() {
        return isFirstMerage;
    }

    /**
     * merage two same name Sample
     *
     * @param sample sample
     */
    public void merageSample(PrefSample sample) {
        if (isFirstMerage) {
            isFirstMerage = false;
            startTs = sample.getTs();
            tid = Long.valueOf(sample.getThreadId()).intValue();
            fileId = sample.getFileId();
            symbolId = sample.getSymbolId();
            vaddrInFile = sample.getVaddrInFile();
            funcName = sample.getName();
            isUserWrite = sample.isUserWrite();
            threadName = PerfData.getThreadNames().get(Long.valueOf(sample.getThreadId()).intValue());
        } else {
            if (endTs == 0) {
                setEndTs(sample.getTs());
            }
        }
    }

    /**
     * update ParentEndTime
     *
     * @param index index
     * @param ts ts
     */
    public void updateParentEndTime(int index, long ts) {
        if (parentNode != null) {
            if (depth > index) {
                if (endTs == 0) {
                    setEndTs(ts);
                }
                parentNode.updateParentEndTime(index, ts);
            }
        } else {
            setEndTs(ts);
        }
    }

    @Override
    public void draw(Graphics2D graphics) {
        if (depth == -1) {
            return;
        }
        if (isMouseIn || this == selectedPrefFunc) {
            Common.setAlpha(graphics, 0.7F);
        } else {
            Common.setAlpha(graphics, 1.0F);
        }
        if (funcName.contains("(") || funcName.contains(PrefSample.KERNEL) || funcName.contains(".so")) {
            graphics.setColor(PerfColorUtil.getPerfMethod(this));
        } else if (funcName.contains(".")) {
            graphics.setColor(PerfColorUtil.getJavaMethod(this));
        } else {
            graphics.setColor(PerfColorUtil.getPerfMethod(this));
        }
        graphics.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height + 1);
        Common.setAlpha(graphics, 1.0F);
        graphics.setColor(JBColor.border().darker());
        Common.drawStringCenter(graphics, funcName, rect);
        if (rect.height > 2) {
            graphics.setColor(JBColor.border().darker());
            graphics.drawLine(Utils.getX(rect), Utils.getY(rect) + rect.height, Utils.getX(rect) + rect.width,
                Utils.getY(rect) + rect.height);
            if (rect.width > 1) {
                graphics.drawLine(Utils.getX(rect), Utils.getY(rect), Utils.getX(rect), Utils.getY(rect) + rect.height);
            }
        }
    }

    @Override
    public List<String> getStringList(String time) {
        return Arrays.asList(time, funcName, "Thread:" + threadName, "Tid:" + tid, "depth:" + depth,
            "Running: " + TimeUtils.getTimeWithUnit(dur), "idle:0μs", "Total: " + TimeUtils.getTimeWithUnit(dur));
    }

    @Override
    public void onClick(MouseEvent event) {
        super.onClick(event);
        selectedPrefFunc = this;
        event.getComponent().repaint();
        EventDispatcher.dispatcherClickListener(this);
    }
}
