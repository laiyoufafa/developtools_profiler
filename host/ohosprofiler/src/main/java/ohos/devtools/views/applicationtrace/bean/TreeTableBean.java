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

import com.intellij.ui.JBColor;
import ohos.devtools.views.applicationtrace.DataPanel;
import ohos.devtools.views.applicationtrace.analysis.AnalysisEnum;
import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.perftrace.PerfColorUtil;
import ohos.devtools.views.perftrace.bean.PrefSample;
import ohos.devtools.views.trace.AbstractNode;
import ohos.devtools.views.trace.Common;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.Utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TreeTableBean
 *
 * @since 2021/5/26 15:38
 */
public class TreeTableBean extends AbstractNode {
    /**
     * PERF_FLAME_VENDOR color
     */
    public static final Color PERF_FLAME_VENDOR = new JBColor(0xFFC56F, 0xFFC56F);

    private static final int PADDING_LEFT = 5;
    private static final int PADDING_RIGHT = 5;
    private long stackId;
    private long parentStackId;
    private String prefStackId;
    private String prefParentStackId;
    private String name;
    private String total;
    private long totalNum = 0L;
    private double totalPercentNum = 0D;
    private long selfNum = 0L;
    private String selfPercent = "0";
    private double selfPercentNum = 0D;
    private long childrenNum = 0L;
    private double childrenPercentNum = 0D;
    private long childrenNS;
    private String childrenPercent;
    private boolean isUserWrite = false;
    private int containType = 0; // 0 OK 1 There are keywords 2 children there are keywords 3 there are no keywords
    private List<Integer> childrens = new ArrayList<>();
    private long threadDur = 0L;
    private String bloodId = "";
    private String parentBloodId = "";

    /**
     * constructor
     */
    public TreeTableBean() {
        this(0);
    }

    /**
     * constructor
     *
     * @param threadDur duration
     */
    public TreeTableBean(final long threadDur) {
        this.threadDur = threadDur;
    }

    /**
     * set current time
     *
     * @param timeBean timeBean
     */
    public void setTime(TreeTableBean timeBean) {
        setTotalNum(timeBean.getTotalNum());
        setSelfNum(0);
        setChildrenNum(timeBean.getTotalNum());
    }

    /**
     * merge current time
     *
     * @param timeBean timeBean
     */
    public void mergeTime(TreeTableBean timeBean) {
        setTotalNum(getTotalNum() + timeBean.getTotalNum());
        setSelfNum(getSelfNum() + timeBean.getSelfNum());
        setChildrenNum(getChildrenNum() + timeBean.getChildrenNum());
    }

    /**
     * get pref stack id
     *
     * @return id
     */
    public String getPrefStackId() {
        return prefStackId;
    }

    /**
     * set stack id
     *
     * @param param stack id
     */
    public void setPrefStackId(final String param) {
        this.prefStackId = param;
    }

    /**
     * get parent stack id
     *
     * @return id
     */
    public String getPrefParentStackId() {
        return prefParentStackId;
    }

    /**
     * set parent stack id
     *
     * @param param id
     */
    public void setPrefParentStackId(final String param) {
        this.prefParentStackId = param;
    }

    /**
     * is user write
     *
     * @return boolean
     */
    public boolean isUserWrite() {
        return isUserWrite;
    }

    /**
     * set user write
     *
     * @param param user write
     */
    public void setUserWrite(final boolean param) {
        isUserWrite = param;
    }

    /**
     * get contain type
     *
     * @return type
     */
    public int getContainType() {
        return containType;
    }

    /**
     * set contain type
     *
     * @param param type
     */
    public void setContainType(final int param) {
        this.containType = param;
    }

    /**
     * get thread duration
     *
     * @return duration
     */
    public long getThreadDur() {
        return threadDur;
    }

    /**
     * set thread duration
     * 此方法需要在set total children self之前设置 否则百分数数据不会计算
     *
     * @param param duration
     */
    public void setThreadDur(final long param) {
        this.threadDur = param;
    }

    /**
     * get total percent
     *
     * @return percent
     */
    public double getTotalPercentNum() {
        return totalPercentNum;
    }

    /**
     * get self percent
     *
     * @return percent
     */
    public double getSelfPercentNum() {
        return selfPercentNum;
    }

    /**
     * get child percent
     *
     * @return percent
     */
    public double getChildrenPercentNum() {
        return childrenPercentNum;
    }

    /**
     * get total num
     *
     * @return num
     */
    public long getTotalNum() {
        return totalNum;
    }

    /**
     * set total num
     *
     * @param param num
     */
    public void setTotalNum(final long param) {
        this.totalNum = param;
        if (threadDur != 0) {
            totalPercentNum = param * 1.0 / threadDur * 100;
        }
    }

    /**
     * get self num
     *
     * @return num
     */
    public long getSelfNum() {
        return selfNum;
    }

    /**
     * set self num
     *
     * @param param num
     */
    public void setSelfNum(final long param) {
        this.selfNum = param;
        if (threadDur != 0) {
            selfPercentNum = param * 1.0 / threadDur * 100;
        }
    }

    /**
     * get child num
     *
     * @return num
     */
    public long getChildrenNum() {
        return childrenNum;
    }

    /**
     * set children num
     *
     * @param param num
     */
    public void setChildrenNum(final long param) {
        this.childrenNum = param;
        if (threadDur != 0) {
            childrenPercentNum = param * 1.0 / threadDur * 100;
        }
    }

    /**
     * get children
     *
     * @return children list
     */
    public List<Integer> getChildrens() {
        return childrens;
    }

    /**
     * set children list
     *
     * @param param list
     */
    public void setChildrens(final List<Integer> param) {
        this.childrens = param;
    }

    /**
     * Gets the value of childrenNS .
     *
     * @return the value of long
     */
    public long getChildrenNS() {
        return childrenNS;
    }

    /**
     * Sets the childrenNS .
     * <p>You can use getChildrenNS() to get the value of childrenNS</p>
     *
     * @param param childrenNS
     */
    public void setChildrenNS(final long param) {
        this.childrenNS = param;
    }

    /**
     * get total
     *
     * @return total
     */
    public String getTotal() {
        return total;
    }

    /**
     * set total
     *
     * @param param total
     */
    public void setTotal(final String param) {
        this.total = param;
    }

    /**
     * get name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * set name
     *
     * @param param name
     */
    public void setName(final String param) {
        this.name = param;
    }

    /**
     * Gets the value of stackId .
     *
     * @return the value of java.lang.Integer
     */
    public long getStackId() {
        return stackId;
    }

    /**
     * Sets the stackId .
     * <p>You can use getStackId() to get the value of stackId</p>
     *
     * @param param id
     */
    public void setStackId(final long param) {
        this.stackId = param;
    }

    /**
     * Gets the value of parentStackId .
     *
     * @return the value of java.lang.Integer
     */
    public long getParentStackId() {
        return parentStackId;
    }

    /**
     * Sets the parentStackId .
     * <p>You can use getParentStackId() to get the value of parentStackId</p>
     *
     * @param param id
     */
    public void setParentStackId(final long param) {
        this.parentStackId = param;
    }

    /**
     * get the self percent
     *
     * @return percent
     */
    public String getSelfPercent() {
        return selfPercent;
    }

    /**
     * set self percent
     *
     * @param param percent
     */
    public void setSelfPercent(final String param) {
        this.selfPercent = param;
    }

    /**
     * get children percent
     *
     * @return percent
     */
    public String getChildrenPercent() {
        return childrenPercent;
    }

    /**
     * set children percent
     *
     * @param param percent
     */
    public void setChildrenPercent(final String param) {
        this.childrenPercent = param;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void draw(Graphics2D paint) {
        if (containType == 3) {
            Common.setAlpha(paint, 0.2F);
        } else {
            if (isMouseIn) {
                Common.setAlpha(paint, 0.7F);
            } else {
                Common.setAlpha(paint, 1.0F);
            }
        }
        if (DataPanel.analysisEnum.equals(AnalysisEnum.APP)) {
            paint.setColor(PerfColorUtil.PERF_FLAME_VENDOR);
        } else {
            if (name.contains("(") || name.contains(PrefSample.KERNEL) || name.contains(".so")) {
                paint.setColor(PerfColorUtil.getPerfMethod(this));
            } else if (name.contains(".")) {
                paint.setColor(PerfColorUtil.getJavaMethod(this));
            } else {
                paint.setColor(PerfColorUtil.getPerfMethod(this));
            }
        }
        paint.fillRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
        if (rect.width > 1) {
            paint.setColor(JBColor.background().darker());
            paint.drawRect(Utils.getX(rect), Utils.getY(rect), rect.width, rect.height);
        }
        Common.setAlpha(paint, 1.0f);
        if (containType == 1) {
            paint.setFont(new Font(Final.FONT_NAME, Font.BOLD, Final.NORMAL_FONT_SIZE));
        } else {
            paint.setFont(new Font(Final.FONT_NAME, Font.PLAIN, Final.NORMAL_FONT_SIZE));
        }
        Common.drawStringMiddleHeight(paint, name,
            new Rectangle(Utils.getX(rect) + PADDING_LEFT, Utils.getY(rect), rect.width - PADDING_LEFT - PADDING_RIGHT,
                rect.height));
    }

    @Override
    public List<String> getStringList(String time) {
        return Arrays.asList(
            "" + getName(),
            "",
            "Total: " + TimeUtils.getTimeWithUnit(totalNum * 1000)
        );
    }

    /**
     * get BloodId
     *
     * @return BloodId
     */
    public String getBloodId() {
        return bloodId;
    }

    /**
     * set BloodId
     *
     * @param bloodId bloodId
     */
    public void setBloodId(String bloodId) {
        this.bloodId = bloodId;
    }

    /**
     * get parentBloodId
     *
     * @return parentBloodId
     */
    public String getParentBloodId() {
        return parentBloodId;
    }

    /**
     * set BloodId
     *
     * @param parentBloodId parentBloodId
     */
    public void setParentBloodId(String parentBloodId) {
        this.parentBloodId = parentBloodId;
    }
}
