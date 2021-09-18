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

import ohos.devtools.views.trace.util.TimeUtils;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JComponent;
import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.Graphics2D;

import static ohos.devtools.views.trace.component.AnalystPanel.DURATION;

/**
 * Time axis scale
 *
 * @date 2021/4/22 12:25
 */
public class TopFragment extends AbstractFragment {
    private final Font smallFont = new Font("宋体", Font.ITALIC, 10);

    /**
     * constructor
     *
     * @param root parent component
     */
    public TopFragment(final JComponent root) {
        this.setRoot(root);
        final int leftW = 200;
        final int height = 18;
        getRect().setBounds(leftW, 0, root.getWidth(), height);
    }

    /**
     * Drawing method
     *
     * @param graphics graphics
     */
    @Override
    public void draw(final Graphics2D graphics) {
        int width = getRoot().getWidth() - Utils.getX(getRect());
        final int height = 18;
        final double sq = 10.00; // 10 equal parts
        double wid = width / sq;
        double sqWidth = wid / sq;
        graphics.setFont(getRoot().getFont());
        graphics.setColor(getRoot().getForeground());
        final AlphaComposite alpha50 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f);
        graphics.setComposite(alpha50);
        int yAxis = 0;
        graphics.drawLine(Utils.getX(getRect()), yAxis, Utils.getX(getRect()) + width, yAxis);
        final int num = 10;
        long second = DURATION / num;
        final AlphaComposite alpha100 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
        for (int index = 0; index <= num; index++) {
            int tx = (int) (index * wid) + Utils.getX(getRect());
            graphics.setColor(getRoot().getForeground());
            graphics.setComposite(alpha50);
            graphics.drawLine(tx, yAxis, tx, height);
            String str = TimeUtils.getSecondFromNSecond(second * index);
            graphics.setColor(getRoot().getForeground());
            graphics.setComposite(alpha100);
            final int offset = 3;
            graphics.drawString(str, tx + offset, height);
            for (int numIndex = 1; numIndex < num; numIndex++) {
                int side = (int) (numIndex * sqWidth) + tx;
                graphics.setColor(getRoot().getForeground());
                graphics.drawLine(side, yAxis, side, height / offset);
            }
        }
    }
}
