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
import ohos.devtools.views.trace.bean.CPUProcessBean;
import ohos.devtools.views.trace.bean.CPUThreadBean;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.Utils;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Scroll table component
 *
 * @date 2021/04/20 12:24
 */
@Deprecated
public class ScrollTablePanel extends BottomScrollPanel {
    private String[] columns;
    private List<Object> dataSource = new ArrayList<>();

    /**
     * Constructor
     *
     * @param columns columns
     * @param dataSource dataSource
     */
    public ScrollTablePanel(String[] columns, ArrayList<Object> dataSource) {
        super();
        setFont(Final.NORMAL_FONT);
        box = JBBox.createVerticalBox();
        setViewportView(box);
        setColumnsAndData(columns, dataSource);
    }

    /**
     * Set up columns and data sources
     *
     * @param columns columns
     * @param dataSource dataSource
     */
    public void setColumnsAndData(String[] columns, ArrayList<Object> dataSource) {
        this.columns = columns;
        this.dataSource = dataSource;
        box.removeAll();
        if (columns != null) {
            box.add(new TitleComponent());
        }
        if (dataSource != null) {
            for (Object obj : dataSource) {
                box.add(new LineComponent(obj, columns.length));
            }
        }
    }

    /**
     * TitleComponent
     *
     * @date 2021/04/20 12:24
     */
    class TitleComponent extends ChildLineComponent {
        private Rectangle[] rects = new Rectangle[columns.length];
        private int[] filters = {-1, -1};

        @Override
        public void childMouseClicked(final MouseEvent event) {
            for (int index = 0; index < rects.length; index++) {
                if (Utils.pointInRect(rects[index], event.getX(), event.getY())) {
                    if (filters[0] == index) {
                        filters[1] = filters[1] == 0 ? 1 : 0;
                    } else {
                        filters[0] = index;
                        filters[1] = 0;
                    }
                    repaint();
                    return;
                }
            }
        }

        @Override
        public void paint(Graphics graphics) {
            if (columns.length == 0) {
                return;
            }
            graphics.setColor(getForeground());
            graphics.clearRect(0, 0, getWidth(), lineHeight);
            graphics.setFont(getFont().deriveFont(Font.BOLD, 12f));
            int width = (getWidth() - 20) / columns.length;
            if (mouseIn) {
                if (graphics instanceof Graphics2D) {
                    Composite originalComposite = ((Graphics2D) graphics).getComposite();
                    ((Graphics2D) graphics).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
                    graphics.setColor(getForeground());
                    graphics.fillRect(0, 0, getWidth(), lineHeight);
                    ((Graphics2D) graphics).setComposite(originalComposite);
                }
                graphics.setColor(getBackground());
                for (int index = 0; index < columns.length; index++) {
                    if (rects[index] == null) {
                        rects[index] = new Rectangle(10 + index * width - 4, 0, width - 2, lineHeight);
                    }
                    if (index > 0) {
                        graphics.fillRect(Utils.getX(rects[index]), Utils.getY(rects[index]), 2, rects[index].height);
                    }
                }
            }
            graphics.setColor(getForeground());
            for (int index = 0; index < columns.length; index++) {
                Rectangle2D bounds = graphics.getFontMetrics().getStringBounds(columns[index], graphics);
                graphics.drawString(columns[index], 10 + index * width, lineHeight / 2 + 4);
                if (index == filters[0]) {
                    try {
                        int xVal = (int) (10 + index * width + bounds.getWidth() + 10);
                        Image img = ImageIO.read(
                            getClass().getResourceAsStream(filters[1] == 0 ? "/assets/down.png" : "/assets/up.png"));
                        graphics.drawImage(img, xVal, (lineHeight - 20) / 2, 20, 20, null);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * LineComponent
     *
     * @date 2021/04/20 12:24
     */
    class LineComponent extends ChildLineComponent {
        private final int xAxis = 10;
        private Object data;
        private int columnSize = 7;

        /**
         * Construction
         *
         * @param data Bean of Cpu
         * @param columnSize size of column size
         */
        public LineComponent(Object data, int columnSize) {
            super();
            this.data = data;
            this.columnSize = columnSize;
        }

        @Override
        public void paint(Graphics graphics) {
            graphics.setColor(getForeground());
            graphics.clearRect(0, 0, getWidth(), lineHeight);
            graphics.setFont(getFont().deriveFont(11f));
            int width = (getWidth() - 20) / columnSize;
            if (mouseIn) {
                graphics.setColor(getForeground());
                Composite originalComposite = ((Graphics2D) graphics).getComposite();
                ((Graphics2D) graphics).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
                graphics.fillRect(0, 0, getWidth(), lineHeight);
                ((Graphics2D) graphics).setComposite(originalComposite);
                graphics.setColor(getBackground());
                for (int index = 0; index < columns.length; index++) {
                    if (index > 0) {
                        graphics.fillRect(xAxis + index * width - 4, 0, 2, lineHeight);
                    }
                }
            }
            graphics.setColor(getForeground());
            int yNum = lineHeight / 2 + 4;
            if (data instanceof CPUThreadBean) {
                drawString(graphics, xAxis, yNum, width - 2, ((CPUThreadBean) data).getProcess());
                drawString(graphics, xAxis + 1 * width, yNum, width - 2, ((CPUThreadBean) data).getPid());
                drawString(graphics, xAxis + 2 * width, yNum, width - 2, ((CPUThreadBean) data).getThread());
                drawString(graphics, xAxis + 3 * width, yNum, width - 2, ((CPUThreadBean) data).getTid());
                drawString(graphics, xAxis + 4 * width, yNum, width - 2, ((CPUThreadBean) data).getWallDuration() + "");
                drawString(graphics, xAxis + 5 * width, yNum, width - 2, ((CPUThreadBean) data).getAvgDuration() + "");
                drawString(graphics, xAxis + 6 * width, yNum, width - 2, ((CPUThreadBean) data).getOccurrences());
            }
            if (data instanceof CPUProcessBean) {
                drawString(graphics, xAxis, yNum, width - 2, ((CPUProcessBean) data).getProcess());
                drawString(graphics, xAxis + 1 * width, yNum, width - 2, ((CPUProcessBean) data).getPid());
                drawString(graphics, xAxis + 2 * width, yNum, width - 2,
                    ((CPUProcessBean) data).getWallDuration() + "");
                drawString(graphics, xAxis + 3 * width, yNum, width - 2, ((CPUProcessBean) data).getAvgDuration() + "");
                drawString(graphics, xAxis + 4 * width, yNum, width - 2, ((CPUProcessBean) data).getOccurrences());
            }
        }

        /**
         * draw String
         *
         * @param graphics graphics
         * @param xAxis xAxis
         * @param yAxis yAxis
         * @param width width
         * @param str string
         */
        private void drawString(Graphics graphics, int xAxis, int yAxis, int width, String str) {
            int size = width / 7;
            if (str.length() > size) {
                graphics.drawString(str.substring(0, size) + "...", xAxis, yAxis);
            } else {
                graphics.drawString(str, xAxis, yAxis);
            }
        }
    }
}
