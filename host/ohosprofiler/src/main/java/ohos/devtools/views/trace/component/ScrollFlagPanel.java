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

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.trace.bean.FlagBean;
import ohos.devtools.views.trace.listener.IFlagListener;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.TimeUtils;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * flag component
 *
 * @since 2021/04/20 12:24
 */
public class ScrollFlagPanel extends JBPanel {
    private FlagBean flag;
    private JBLabel title = new JBLabel("Annotation at ");
    private JBLabel changeColor = new JBLabel("Change Color ");
    private JBTextField input;
    private ColorBT colorBt = new ColorBT(Color.magenta);
    private JButton remove = new JButton("Remove");
    private IFlagListener flagListener;

    /**
     * construct
     *
     * @param flag flag object
     */
    public ScrollFlagPanel(FlagBean flag) {
        super();
        setLayout(new MigLayout("insets 10", "[][grow,fill][][][]", "[65][grow,push]"));
        setFont(Final.NORMAL_FONT);
        this.flag = flag;
        if (this.flag != null) {
            this.flag = new FlagBean();
        }
        input = new JBTextField();
        add(title);
        add(input);
        add(changeColor);
        add(colorBt);
        add(remove, "");
        add(new JBLabel());
        input.setText(flag.getName());
        colorBt.setCurrentColor(flag.getColor());
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (flagListener != null) {
                        flag.setName(input.getText());
                        flagListener.flagChange(flag);
                    }
                    transferFocus();
                }
            }
        });
        remove.addActionListener(actionEvent -> {
            if (flagListener != null) {
                flagListener.flagRemove(flag);
            }
        });
        colorBt.addActionListener(event -> {
            colorBt.selectColor();
        });
        setData(flag);
    }

    /**
     * set flag object
     *
     * @param flag flag object
     */
    public void setData(FlagBean flag) {
        this.flag = flag;
        title.setText("Annotation at " + TimeUtils.getTimeString(flag.getNs()));
        input.setText(flag.getName());
        colorBt.setCurrentColor(flag.getColor() == null ? Color.pink : flag.getColor());
    }

    /**
     * set flag listener
     *
     * @param listener listener
     */
    public void setFlagListener(IFlagListener listener) {
        this.flagListener = listener;
    }

    /**
     * ColorPanel
     *
     * @since 2021/04/20 12:24
     */
    class ColorBT extends JButton {
        private Color currentColor;
        private JColorChooser colorChooser;
        private int lineHeight = 25;
        private int lineWidth = 60;

        /**
         * ColorBT
         *
         * @param color color
         */
        public ColorBT(Color color) {
            this.currentColor = color;
            colorChooser = new JColorChooser(color);
            AbstractColorChooserPanel[] cps = colorChooser.getChooserPanels();
            for (AbstractColorChooserPanel cp : cps) {
                colorChooser.removeChooserPanel(cp);
            }
            colorChooser.addChooserPanel(cps[3]);
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension dimension = super.getMinimumSize();
            if (this.lineWidth != 0) {
                dimension.width = lineWidth;
            }
            dimension.height = lineHeight;
            return dimension;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension dimension = super.getPreferredSize();
            if (this.lineWidth != 0) {
                dimension.width = lineWidth;
            }
            dimension.height = lineHeight;
            return dimension;
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension dimension = super.getMaximumSize();
            if (this.lineWidth != 0) {
                dimension.width = lineWidth;
            }
            dimension.height = lineHeight;
            return dimension;
        }

        /**
         * set current color
         *
         * @param currentColor color
         */
        public void setCurrentColor(Color currentColor) {
            this.currentColor = currentColor;
            flag.setColor(currentColor);
            if (flagListener != null) {
                flagListener.flagChange(flag);
            }
            repaint();
        }

        /**
         * select flag color
         */
        public void selectColor() {
            JDialog dialog = JColorChooser.createDialog(getRootPane(), "Choose Color", true, colorChooser,
                actionEvent -> setCurrentColor(colorChooser.getColor()), null);
            dialog.setVisible(true);
        }

        @Override
        public void paint(Graphics graphics) {
            graphics.setColor(getForeground());
            graphics.drawRect(0, 0, lineWidth - 1, lineHeight - 1);
            graphics.setColor(currentColor);
            graphics.fillRect(10, 6, 40, lineHeight - 12);
        }
    }

}
