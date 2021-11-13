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

package ohos.devtools.views.distributed.component;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import ohos.devtools.views.trace.util.TimeUtils;
import ohos.devtools.views.trace.util.Utils;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * DistributedRuler
 *
 * @since 2021/08/18 20:19
 */
public class DistributedRuler extends JBPanel {
    private final long dur;

    public DistributedRuler(long dur) {
        this.dur = dur;
    }

    @Override
    public void paint(final Graphics graphics) {
        if (graphics instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) graphics;
            Rectangle rect = new Rectangle(0, 0, getBounds().width, getBounds().height);
            int width = (int) (getBounds().getWidth() - Utils.getX(rect));
            final int height = 18;
            final double sq = 10.00D; // 10 equal parts
            double wid = width / sq;
            double sqWidth = wid / sq;
            g2.setFont(getFont());
            g2.setColor(JBColor.background().darker());
            final AlphaComposite alpha50 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f);
            g2.setComposite(alpha50);
            int yAxis = 0;
            g2.drawLine(Utils.getX(rect), yAxis, Utils.getX(rect) + width, yAxis);
            final int num = 10;
            long second = dur / num;
            final AlphaComposite alpha100 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
            for (int index = 0; index <= num; index++) {
                int tx = (int) (index * wid) + Utils.getX(rect);
                g2.setColor(JBColor.background().darker());
                g2.setComposite(alpha50);
                g2.drawLine(tx, yAxis, tx, height);
                String str = TimeUtils.getSecondFromNSecond(second * index);
                g2.setColor(JBColor.foreground().darker());
                g2.setComposite(alpha100);
                final int offset = 3;
                g2.drawString(str, tx + offset, height);
                for (int numIndex = 1; numIndex < num; numIndex++) {
                    int side = (int) (numIndex * sqWidth) + tx;
                    g2.setColor(JBColor.background().darker());
                    g2.drawLine(side, yAxis, side, height / offset);
                }
            }
        }
    }
}
