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

package ohos.devtools.views.trace.component;

import com.intellij.ui.components.JBBox;
import ohos.devtools.views.trace.bean.WakeupBean;
import ohos.devtools.views.trace.listener.IScrollSliceLinkListener;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.TimeUtils;
import ohos.devtools.views.trace.util.Utils;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * bottom Slice panel
 *
 * @since 2021/04/20 12:24
 */
public class ScrollSlicePanel extends BottomScrollPanel {
    private Font font = getFont();
    private List<LineComponent> comps = new ArrayList<>();
    private List<SliceData> dataSource;
    private WakeupBean wakeupBean;
    private IScrollSliceLinkListener listener;

    /**
     * create table line data
     *
     * @param key key
     * @param value value
     * @param linkable link
     * @return SliceData data
     */
    public static SliceData createSliceData(String key, String value, boolean linkable) {
        SliceData data = new SliceData();
        data.key = key;
        data.value = value;
        data.linkable = linkable;
        return data;
    }

    /**
     * set component data source
     *
     * @param title table title
     * @param dataSource table data source
     * @param wakeupBean wakeup data information
     */
    public void setData(String title, List<SliceData> dataSource, WakeupBean wakeupBean) {
        this.dataSource = dataSource;
        this.wakeupBean = wakeupBean;
        box.removeAll();
        JBBox b1 = JBBox.createVerticalBox();
        b1.add(new TitlePanel(title));
        comps.clear();
        if (dataSource != null) {
            for (SliceData sliceData : dataSource) {
                LineComponent lc = new LineComponent(sliceData.key, sliceData.value == null ? "" : sliceData.value,
                    sliceData.linkable);
                lc.setListener(listener);
                comps.add(lc);
                b1.add(lc);
            }
        }
        b1.add(JBBox.createVerticalGlue());
        JBBox b2 = JBBox.createVerticalBox();
        b2.add(new RightPanel());
        b2.add(JBBox.createVerticalGlue());
        box.add(b1);
        box.add(b2);
        revalidate();
    }

    /**
     * set link click listener
     *
     * @param listener listener
     */
    public void setScrollSliceLinkListener(IScrollSliceLinkListener listener) {
        this.listener = listener;
        for (LineComponent comp : comps) {
            if (comp.linkable) {
                comp.setListener(listener);
            }
        }
    }

    /**
     * SliceData
     *
     * @since 2021/04/20 12:24
     */
    public static class SliceData {
        /**
         * parameter key
         */
        public String key;

        /**
         * parameter value
         */
        public String value;

        /**
         * parameter linkable
         */
        public boolean linkable;
    }

    /**
     * RightPanel
     *
     * @since 2021/04/20 12:24
     */
    class RightPanel extends ChildPanel {
        private final int[][] pointX = {{11, 2, 11, 20}, {13, 13, 18, 20, 15, 20, 18}, {103, 103, 98, 96, 101, 96, 98}};
        private final int[][] pointY = {{50, 58, 66, 58}, {120, 124, 129, 127, 122, 117, 115}};
        private final int strStartX = 30;
        private final int lineCharSize = 60;

        /**
         * RightPanel constructor
         */
        public RightPanel() {
            super(0, 222);
        }

        /**
         * paint
         *
         * @param graphics graphics
         */
        @Override
        public void paint(Graphics graphics) {
            super.paint(graphics);
            setFont(Final.NORMAL_FONT);
            if (wakeupBean == null || wakeupBean.getWakeupProcess() == null) {
                return;
            }
            graphics.setFont(font.deriveFont(13f));
            if (graphics instanceof Graphics2D) {
                ((Graphics2D) graphics)
                    .setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                ((Graphics2D) graphics)
                    .setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            graphics.drawString("Scheduling Latency", 10, 20);
            graphics.fillRect(10, 30, 3, 130);
            graphics.fillPolygon(pointX[0], pointY[0], pointX[0].length);
            graphics.fillPolygon(pointX[1], pointY[1], pointX[1].length);
            graphics.fillPolygon(pointX[2], pointY[1], pointX[2].length);
            graphics.fillRect(13, 121, 90, 3);
            graphics.setFont(font.deriveFont(11f));
            graphics.drawString(
                "Wakeup @" + TimeUtils.getTimeString(wakeupBean.getWakeupTime()) + " on CPU " + wakeupBean
                    .getWakeupCpu() + " by", strStartX, 55);
            graphics
                .drawString("P:" + wakeupBean.getWakeupProcess() + " [ " + wakeupBean.getWakeupPid() + " ]", strStartX,
                    75);
            graphics
                .drawString("T:" + wakeupBean.getWakeupThread() + " [ " + wakeupBean.getWakeupTid() + " ]", strStartX,
                    95);
            graphics.drawString("Scheduling latency:" + TimeUtils.getTimeString(wakeupBean.getSchedulingLatency()), 115,
                125);
            graphics.setFont(font.deriveFont(9f));
            graphics.setColor(new Color(0x88, 0x88, 0x88));
            int lines = wakeupBean.getSchedulingDesc().length() % lineCharSize == 0
                ? wakeupBean.getSchedulingDesc().length() / lineCharSize
                : wakeupBean.getSchedulingDesc().length() / lineCharSize + 1;
            for (int index = 0; index < lines; index++) {
                String str = "";
                if (index == lines - 1) {
                    str = wakeupBean.getSchedulingDesc().substring(index * lineCharSize);
                } else {
                    str = wakeupBean.getSchedulingDesc().substring(index * lineCharSize, (index + 1) * lineCharSize);
                }
                graphics.drawString(str, 115, 142 + index * 14);
            }
            setFont(Final.NORMAL_FONT);
        }
    }

    /**
     * TitlePanel
     *
     * @since 2021/04/20 12:24
     */
    class TitlePanel extends ChildLineComponent {
        private String title;

        /**
         * set title
         *
         * @param title title
         */
        public TitlePanel(String title) {
            this.title = title;
            setFont(Final.NORMAL_FONT);
        }

        /**
         * paint
         *
         * @param graphics graphics
         */
        @Override
        public void paint(Graphics graphics) {
            super.paint(graphics);
            graphics.setFont(font.deriveFont(13f));
            graphics.drawString(title, 10, 20);
        }
    }

    /**
     * LineComponent
     *
     * @since 2021/04/20 12:24
     */
    class LineComponent extends ChildLineComponent {
        /**
         * linkable
         */
        public boolean linkable;

        private final int leftW = 200;
        private String key;
        private String value;
        private IScrollSliceLinkListener listener;
        private Rectangle linkRect;

        /**
         * LineComponent constructor
         *
         * @param key key
         * @param value value
         * @param linkable linkable
         */
        public LineComponent(String key, String value, boolean linkable) {
            super();
            this.key = key;
            this.value = value;
            this.linkable = linkable;
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(final MouseEvent event) {
                    if (Utils.pointInRect(linkRect, event.getX(), event.getY())) {
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            });
        }

        /**
         * set link listener
         *
         * @param listener listener
         */
        public void setListener(IScrollSliceLinkListener listener) {
            this.listener = listener;
        }

        /**
         * child mouse clicked
         *
         * @param event event
         */
        @Override
        public void childMouseClicked(final MouseEvent event) {
            if (Utils.pointInRect(linkRect, event.getX(), event.getY())) {
                if (listener != null) {
                    listener.linkClick(value);
                }
            }
        }

        /**
         * paint
         *
         * @param graphics graphics
         */
        @Override
        public void paint(Graphics graphics) {
            super.paint(graphics);
            graphics.setFont(font.deriveFont(11f));
            if (graphics instanceof Graphics2D) {
                ((Graphics2D) graphics)
                    .setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            if (mouseIn) {
                if (graphics instanceof Graphics2D) {
                    graphics.setColor(getForeground());
                    Composite originalComposite = ((Graphics2D) graphics).getComposite();
                    ((Graphics2D) graphics).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
                    graphics.fillRect(0, 0, leftW - 2, lineHeight);
                    graphics.fillRect(leftW, 0, getWidth() - leftW, lineHeight);
                    ((Graphics2D) graphics).setComposite(originalComposite);
                }
            }
            graphics.setColor(getForeground());
            graphics.drawString(key, 10, lineHeight / 2 + 4);
            int sw = getWidth() - (leftW + 2);
            int chars = sw / 7;
            if (value.length() > chars && chars > 0) {
                String vs1 = value.substring(0, chars);
                String vs2 = value.substring(chars, value.length() - 1);
                graphics.drawString(vs1, leftW + 2, lineHeight / 3);
                graphics.drawString(vs2, leftW + 2, (lineHeight * 2) / 3);
            } else {
                graphics.drawString(value, leftW, lineHeight / 2 + 4);
            }
            if (linkable) {
                try {
                    if (value != null && value.length() > 0) {
                        Rectangle2D bounds = graphics.getFontMetrics(font).getStringBounds(value, graphics);
                        linkRect = new Rectangle((int) (202 + bounds.getWidth() + 5), (lineHeight - 20) / 2, 20, 20);
                        Image link = ImageIO.read(getClass().getResourceAsStream("/assets/link.png"));
                        graphics.drawImage(link, Utils.getX(linkRect) + 2, Utils.getY(linkRect) + 3, 15, 15, null);
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}
