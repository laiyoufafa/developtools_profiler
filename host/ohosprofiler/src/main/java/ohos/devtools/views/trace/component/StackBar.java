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

import ohos.devtools.views.trace.bean.TabThreadStatesBean;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StackBar
 *
 * @date: 2021/5/27 12:03
 */
public class StackBar extends JPanel {
    private List<StateValue> source = new ArrayList<>();
    private long totalWallDuration;
    private StateValue current;
    private BigDecimal charW = BigDecimal.valueOf(getFontMetrics(getFont().deriveFont(11.0f)).charWidth('a') )
        .subtract(new BigDecimal( 0.2));
    private int drawX = 0;

    /**
     * structure function
     */
    public StackBar() {
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(final MouseEvent event) {
            }

            @Override
            public void mouseMoved(final MouseEvent event) {
                for (int index = 0, size = source.size(); index < size; index++) {
                    if (Utils.pointInRect(source.get(index).drawRect, event.getX(), event.getY())) {
                        if (current != source.get(index)) {
                            current = source.get(index);
                            repaint();
                        }
                        break;
                    }
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(final MouseEvent event) {
                super.mouseMoved(event);
                current = null;
            }
        });
    }

    /**
     * set data source
     *
     * @param data data
     */
    public void setSource(final List<TabThreadStatesBean> data) {
        source.clear();
        if (data != null) {
            Map<String, List<TabThreadStatesBean>> collect =
                data.stream().collect(Collectors.groupingBy(TabThreadStatesBean::getState));
            for (String state : collect.keySet()) {
                long sum = 0;
                for (TabThreadStatesBean bean : collect.get(state)) {
                    sum += bean.getWallDuration();
                }
                if ("".equals(state)) {
                    totalWallDuration = sum;
                } else {
                    StateValue sv = new StateValue();
                    sv.color = getStateColor(state);
                    sv.state = Utils.getEndState(state) + " : " + Utils.transformTimeToMs(sum) + "ms";
                    sv.value = sum;
                    source.add(sv);
                }
            }
            source = source.stream().sorted(Comparator.comparing(StateValue::getValue)).collect(Collectors.toList());
        }
        repaint();
    }

    @Override
    public void paint(final Graphics graphics) {
        drawX = 0;
        if (graphics instanceof Graphics2D) {
            ((Graphics2D) graphics)
                .setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        for (StateValue sv : source) {
            graphics.setFont(getFont().deriveFont(11.0f));
            graphics.setColor(sv.color);
            int trueW;
            if (sv == current && sv.drawRect.width < charW.doubleValue() * sv.state.length()) {
                trueW = (int) (charW.doubleValue() * sv.state.length() + 1);
            } else {
                trueW = (int) ((sv.value * 1.0 / totalWallDuration) * getWidth() + 1);
            }
            if (trueW + drawX > getWidth()) {
                trueW = getWidth() - drawX - 5;
            }
            graphics.fillRect(drawX, 0, trueW, getHeight());
            sv.drawRect = new Rectangle(drawX, 0, trueW, getHeight());

            // 计算单个字符所占宽度
            if (sv.drawRect.width > charW.doubleValue()) {
                if (sv.state.startsWith("Sleeping")) {
                    graphics.setColor(Color.gray);
                } else {
                    graphics.setColor(Color.white);
                }
                int chars = (int) (sv.drawRect.width / charW.doubleValue());
                if (chars < sv.state.length()) {
                    graphics.drawString(sv.state.substring(0, chars), drawX + 1, 13);
                } else {
                    graphics.drawString(sv.state, drawX + 1, 13);
                }
            }
            drawX = drawX + trueW + 1;
        }
    }

    private Color getStateColor(String state) {
        switch (state) {
            case "Running":
                return new Color(Final.RUNNING_COLOR);
            case "D":
                return new Color(Final.UNINTERRUPTIBLE_SLEEP_COLOR);
            case "S":
                return new Color(Final.S_COLOR);
            case "R":
            case "R+":
            default:
                return new Color(Final.R_COLOR);
        }
    }

    private class StateValue {
        private String state;
        private long value;
        private Color color;
        private Rectangle drawRect;

        private long getValue() {
            return value;
        }
    }
}
