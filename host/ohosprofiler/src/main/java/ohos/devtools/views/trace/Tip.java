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

package ohos.devtools.views.trace;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Tip
 *
 * @since 2021/5/28 19:32
 */
public class Tip extends JBPanel {
    private static final int LEFT_PADDING = 10;
    private static final int RIGHT_PADDING = 10;
    private static final int BOTTOM_PADDING = 20;
    private static final int TOP_PADDING = 20;
    private static final int ROW_HEIGHT = 20;
    private static final int MEGA_BYTES = 10241024;
    private static Tip tip = new Tip();
    private JLayeredPane layeredPane;
    private List<String> stringList;

    private Tip() {
        setLayout(new MigLayout(""));
        setBorder(new LineBorder(JBColor.background().darker()));
        setBackground(JBColor.background().brighter());
    }

    /**
     * get tip Instance
     *
     * @return Tip
     */
    public static Tip getInstance() {
        return tip;
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        if (graphics instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON); // Set anti-aliasing
            if (Objects.nonNull(stringList)) {
                int startY = TOP_PADDING;
                for (String str : stringList) {
                    g2.setPaint(JBColor.foreground());
                    g2.drawString(str, LEFT_PADDING, startY);
                    g2.setPaint(JBColor.foreground());
                    g2.drawString(str, LEFT_PADDING, startY);
                    startY += ROW_HEIGHT;
                }
            }
        }
    }

    /**
     * setJLayeredPane
     *
     * @param layeredPane layeredPane
     */
    public void setJLayeredPane(JLayeredPane layeredPane) {
        this.layeredPane = layeredPane;
        if (Arrays.stream(layeredPane.getComponents()).allMatch(component -> component.getClass() != Tip.class)) {
            layeredPane.add(this);
        }
    }

    /**
     * setJLayeredPane
     *
     * @param source source
     * @param point point
     * @return Point
     */
    public Point getPoint(Component source, Point point) {
        return SwingUtilities.convertPoint(source, point, layeredPane);
    }

    /**
     * display current tip
     *
     * @param source source
     * @param point point
     * @param stringList stringList
     */
    public void display(Component source, Point point, List<String> stringList) {
        if (Objects.isNull(stringList)) {
            return;
        }
        if (Objects.nonNull(layeredPane)) {
            this.stringList = stringList;
            this.setVisible(true);
            Point point1 = SwingUtilities.convertPoint(source, point, layeredPane);
            String maxString = stringList.stream().max(Comparator.comparingInt(String::length)).orElse("");
            int maxWidth = SwingUtilities.computeStringWidth(getFontMetrics(getFont()), maxString);
            setBounds(Utils.getX(point1), Utils.getY(point1), maxWidth + LEFT_PADDING + RIGHT_PADDING,
                stringList.size() * ROW_HEIGHT + BOTTOM_PADDING);
            layeredPane.setLayer(this, 1000);
        }
    }

    /**
     * display current tip
     *
     * @param event event
     * @param stringList stringList
     */
    public void display(MouseEvent event, List<String> stringList) {
        display(event.getComponent(), event.getPoint(), stringList);
    }

    /**
     * display current tip
     */
    public void hidden() {
        if (Objects.nonNull(layeredPane)) {
            this.setVisible(false);
            layeredPane.setLayer(this, -1);
        }
    }
}
