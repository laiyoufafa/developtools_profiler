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
import com.intellij.ui.components.JBLayeredPane;
import com.intellij.ui.components.JBTextField;
import ohos.devtools.views.trace.bean.FlagBean;
import ohos.devtools.views.trace.listener.IFlagListener;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.TimeUtils;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * flag component
 *
 * @since 2021/04/20 12:24
 */
public class ScrollFlagPanel extends BottomScrollPanel {
    private FlagBean flag;
    private JBLabel title = new JBLabel("Annotation at ");
    private JBLabel changeColor = new JBLabel("Change Color ");
    private JBTextField input;
    private ColorPanel colorPanel = new ColorPanel(Color.magenta);
    private JButton remove = new JButton("Remove");
    private JBLayeredPane layer;
    private IFlagListener flagListener;

    /**
     * construct
     *
     * @param flag flag object
     */
    public ScrollFlagPanel(FlagBean flag) {
        super();
        setFont(Final.NORMAL_FONT);
        this.flag = flag;
        if (this.flag != null) {
            this.flag = new FlagBean();
        }
        setFocusable(true);
        layer = new JBLayeredPane();
        setViewportView(layer);
        input = new JBTextField();
        box.add(title);
        box.add(input);
        box.add(changeColor);
        box.add(colorPanel);
        box.add(remove);
        layer.add(box);
        input.setText(flag.getName());
        input.transferFocus();
        colorPanel.setCurrentColor(flag.getColor());
        layer.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                super.componentResized(componentEvent);
                Rectangle rootBounds = layer.getBounds();
                box.setBounds(Utils.getX(rootBounds) + 10, Utils.getY(rootBounds) + 10, rootBounds.width - 20, 40);
            }
        });
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (flagListener != null) {
                        flag.setName(input.getText());
                        flagListener.flagChange(flag);
                    }
                }
            }
        });
        remove.addActionListener(actionEvent -> {
            if (flagListener != null) {
                flagListener.flagRemove(flag);
            }
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
        colorPanel.setCurrentColor(flag.getColor() == null ? Color.pink : flag.getColor());
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
    class ColorPanel extends ChildPanel {
        private Color currentColor;
        private JColorChooser colorChooser;
        private Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);

        public ColorPanel(Color color) {
            super(60, 24);
            this.currentColor = color;
            colorChooser = new JColorChooser(color);
            AbstractColorChooserPanel[] cps = colorChooser.getChooserPanels();
            for (AbstractColorChooserPanel cp : cps) {
                colorChooser.removeChooserPanel(cp);
            }
            colorChooser.addChooserPanel(cps[3]);
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent mouseEvent) {
                    selectColor();
                }

                @Override
                public void mouseEntered(final MouseEvent mouseEvent) {
                    setCursor(handCursor);
                }

                @Override
                public void mouseExited(final MouseEvent mouseEvent) {
                    setCursor(handCursor);
                }
            });
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
            super.paint(graphics);
            graphics.setColor(getForeground());
            graphics.drawRect(0, 0, 60, lineHeight);
            graphics.setColor(currentColor);
            graphics.fillRect(10, 6, 40, lineHeight - 12);
        }
    }

}
