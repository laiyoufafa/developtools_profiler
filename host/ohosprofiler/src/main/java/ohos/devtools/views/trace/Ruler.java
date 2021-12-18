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
import ohos.devtools.views.trace.util.TimeUtils;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * PrefSample from db file
 *
 * @since 2021/5/12 16:34
 */
public class Ruler extends JBPanel {
    private long duration;
    private int degreeCount = 55;

    /**
     * Ruler constructor
     *
     * @param duration duration
     */
    public Ruler(long duration) {
        this.duration = duration;
    }

    private String[] getDegreeText(long ns) {
        String[] dtArr = new String[6];
        dtArr[0] = "0";
        for (int index = 1; index < 6; index++) {
            dtArr[index] = TimeUtils.getSecondFromNSecond(index * 10 * ns);
        }
        return dtArr;
    }

    /**
     * refresh time ruler
     *
     * @param duration time
     */
    public void refreshTimeRuler(long duration) {
        this.duration = duration;
        repaint();
    }

    @Override
    public void paint(final Graphics graphics) {
        int width = getWidth();
        int height = getHeight();
        int range = width / degreeCount;
        String[] dtArr = getDegreeText((duration / width) * range);
        if (graphics instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setFont(getFont().deriveFont(11.0f));
            int xx = 0;
            g2d.setColor(JBColor.background().darker());
            g2d.drawLine(xx, 0, xx, height);
            g2d.setColor(getForeground());
            g2d.drawString(dtArr[0], xx + 3, 15);
            int model = width % degreeCount;
            for (int index = 1; index <= degreeCount; index++) {
                xx = index < model ? xx + range + 1 : xx + range;
                if ((index % 10) == 0) {
                    g2d.setColor(JBColor.background().darker());
                    g2d.drawLine(xx, 0, xx, height);
                    g2d.setColor(getForeground());
                    g2d.drawString(dtArr[index / 10], xx + 3, 15);
                } else {
                    if (xx <= width) {
                        g2d.setColor(JBColor.background().darker());
                        g2d.drawLine(xx, 0, xx, 5);
                    }
                }
            }
            g2d.setColor(JBColor.background().darker());
            g2d.drawLine(0, 0, getWidth(), 0);
        }
    }
}
