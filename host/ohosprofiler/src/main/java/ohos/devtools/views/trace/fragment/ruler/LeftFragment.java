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

package ohos.devtools.views.trace.fragment.ruler;

import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.util.TimeUtils;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JComponent;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * The left part of the timeline display area
 *
 * @since 2021/04/22 12:25
 */
public class LeftFragment extends AbstractFragment {
    private String startTimeS = "0.0s";
    private long startNS;
    private int extendHeight;
    private String logString;

    /**
     * @param root Component
     */
    public LeftFragment(final JComponent root) {
        this.setRoot(root);
        final int width = 200;
        final int height = 134;
        getRect().setBounds(0, 0, width, height);
    }

    /**
     * Gets the value of startTimeS .
     *
     * @return the value of java.lang.String
     */
    public String getStartTimeS() {
        return startTimeS;
    }

    /**
     * Sets the startTimeS .
     * <p>You can use getStartTimeS() to get the value of startTimeS</p>
     *
     * @param time time
     */
    public void setStartTimeS(final String time) {
        this.startTimeS = time;
    }

    /**
     * Gets the value of extendHeight .
     *
     * @return the value of int
     */
    public int getExtendHeight() {
        return extendHeight;
    }

    /**
     * Sets the extendHeight .
     * <p>You can use getExtendHeight() to get the value of extendHeight</p>
     *
     * @param height height
     */
    public void setExtendHeight(final int height) {
        this.extendHeight = height;
    }

    /**
     * Gets the value of logString .
     *
     * @return the value of java.lang.String
     */
    public String getLogString() {
        return logString;
    }

    /**
     * Sets the logString .
     * <p>You can use getLogString() to get the value of logString</p>
     *
     * @param log log
     */
    public void setLogString(final String log) {
        this.logString = log;
    }

    /**
     * Drawing method
     *
     * @param graphics graphics
     */
    @Override
    public void draw(final Graphics2D graphics) {
        final int yaxis = 94;
        final float alpha50 = 0.5f;
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha50));
        graphics.setColor(getRoot().getForeground());
        graphics.drawLine(0, yaxis, getRect().width, yaxis);
        graphics.drawLine(getRect().width, 0, getRect().width, getRect().height + extendHeight);
        final int pad = 4;
        graphics.drawLine(0, getRect().height, getRoot().getWidth(), getRect().height);
        graphics.drawLine(0, 0, 0, getRect().height + extendHeight);
        graphics.drawRect(Utils.getX(getRect()), Utils.getY(getRect()), getRect().width, getRect().height);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        Rectangle2D rectangle2D = graphics.getFontMetrics().getStringBounds(startTimeS, graphics);
        int strHeight = (int) rectangle2D.getHeight();
        int strWidth = (int) rectangle2D.getWidth() + pad;
        graphics.drawString(TimeUtils.getSecondFromNSecond(AnalystPanel.getDURATION()), 2, yaxis + strHeight);
        graphics.drawString(startTimeS, getRect().width - strWidth, yaxis + strHeight);
        if (logString != null && !logString.isEmpty()) {
            int index = 0;
            for (String str : logString.split(System.getProperty("line.separator"))) {
                graphics.drawString(str, 1, index * 12 + 10);
                index++;
            }
        }
    }

    /**
     * set Start Time
     *
     * @param leftNS leftNS
     */
    public void setStartTime(final long leftNS) {
        this.startNS = leftNS;
        startTimeS = TimeUtils.getSecondFromNSecond(leftNS);
        repaint();
    }

    /**
     * get Start Time
     *
     * @return long Starting time
     */
    public long getStartNS() {
        return this.startNS;
    }

    /**
     * Sets the startNS .
     * <p>You can use getStartNS() to get the value of startNS</p>
     *
     * @param ns Starting time
     */
    public void setStartNS(final long ns) {
        this.startNS = ns;
    }
}
