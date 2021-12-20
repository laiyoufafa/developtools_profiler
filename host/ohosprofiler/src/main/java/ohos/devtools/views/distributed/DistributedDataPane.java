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

package ohos.devtools.views.distributed;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.Consumer;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.distributed.bean.DistributedFuncBean;
import ohos.devtools.views.distributed.component.SettingDialog;
import ohos.devtools.views.trace.EventDispatcher;
import ohos.devtools.views.trace.util.Utils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Optional;

/**
 * DistributedDataPane
 *
 * @since 2021/08/05 16:03
 */
public class DistributedDataPane extends JBPanel {
    private static final Logger LOGGER = LogManager.getLogger(DistributedDataPane.class);

    private MyTabPanel tabbedPane;
    private DistributedDataDetailPane detailPane;
    private DistributedDataCallTreePane callTreePane;
    private DistributedDataStatisticsPane statisticsPane;
    private DistributedDataEventsPane eventsPane;
    private int currentIndex = 0;
    private SettingDialog dialog = new SettingDialog(() -> {
        detailPane.freshTreeData();
    });
    private JButton settingBtn = new JButton();
    private Consumer onCloseHandler;
    private int barHeight;
    private Consumer<Rectangle> boundsChangeListener;

    /**
     * DistributedDataPane
     *
     * @param onCloseHandler onCloseHandler
     * @param boundsChangeListener boundsChangeListener
     */
    public DistributedDataPane(Consumer onCloseHandler, Consumer<Rectangle> boundsChangeListener) {
        this.boundsChangeListener = boundsChangeListener;
        setLayout(new MigLayout("insets 0 0 0 0", "[grow,fill]", "[grow,fill]"));
        tabbedPane = new MyTabPanel();
        detailPane = new DistributedDataDetailPane();
        callTreePane = new DistributedDataCallTreePane();
        statisticsPane = new DistributedDataStatisticsPane();
        eventsPane = new DistributedDataEventsPane();
        this.onCloseHandler = onCloseHandler;
        tabbedPane.addTab("Detail", detailPane);
        tabbedPane.addTab("Call Tree", callTreePane);
        tabbedPane.addTab("Statistics", statisticsPane);
        add(tabbedPane, "push,grow");
        EventDispatcher.addClickListener(it -> {
            if (it instanceof DistributedFuncBean) {
                tabbedPane.setSelectedIndex(1);
                currentIndex = 1;
            }
        });
        EventDispatcher.addThreadRangeListener((startNS, endNS, threadIds) -> {
            tabbedPane.setSelectedIndex(0);
            currentIndex = 0;
        });
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
                int index = tabbedPane.getSelectedIndex();
                if (index != 2) {
                    if (currentIndex == 0 && index == 1) {
                        callTreePane.freshFuncSelectData();
                    }
                }
                currentIndex = index;
            }
        });
    }

    /**
     * getBarHeight
     *
     * @return barHeight barHeight
     */
    public int getBarHeight() {
        return barHeight;
    }

    private void showDialog() {
        dialog.setVisible(true);
    }

    private class MyTabPanel extends JBTabbedPane implements MouseListener {
        private Rectangle setting = new Rectangle();
        private Rectangle cancel = new Rectangle();
        // private Point startPoint;
        // private Point endPoint;
        // private Rectangle srcBounds;

        /**
         * MyTabPanel
         */
        public MyTabPanel() {
            addMouseListener(this);
            //            addMouseMotionListener(new MouseMotionListener() {
            //                @Override
            //                public void mouseDragged(MouseEvent event) {
            //                    if (getCursor().getType() == Cursor.N_RESIZE_CURSOR) {
            //                        endPoint = SwingUtilities.convertPoint(MyTabPanel.this, event.getPoint(),
            //                            MyTabPanel.this.getRootPane().getLayeredPane());
            //                        int yPosition = Utils.getY(endPoint) - Utils.getY(startPoint);
            //                        if (srcBounds.height - yPosition < barHeight) {
            //                            return;
            //                        } else if (srcBounds.height - yPosition
            //                            > MyTabPanel.this.getRootPane().getLayeredPane().getHeight() - barHeight) {
            //                            return;
            //                        } else {
            //                            DistributedDataPane.this.setBounds(Utils.getX(srcBounds),
            //                            Utils.getY(srcBounds) + yPosition,
            //                                srcBounds.width,
            //                                srcBounds.height - yPosition);
            //                            DistributedDataPane.this.revalidate();
            //                            if (boundsChangeListener != null) {
            //                                boundsChangeListener.consume(DistributedDataPane.this.getBounds());
            //                            }
            //                        }
            //                    }
            //                }
            //
            //                @Override
            //                public void mouseMoved(MouseEvent event) {
            //                    int xNum = 0;
            //                    if (getTabCount() > 0) {
            //                        Rectangle rect = getUI().getTabBounds(MyTabPanel.this, getTabCount() - 1);
            //                        xNum = rect.width + Utils.getX(rect) + 10;
            //                    }
            //                    if (event.getY() > 0 && event.getY() < barHeight && event.getX() > xNum) {
            //                        if (setting.contains(event.getPoint()) || cancel.contains(event.getPoint())) {
            //                            setCursor(new Cursor(Cursor.HAND_CURSOR));
            //                        } else {
            //                            setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
            //                        }
            //                    } else {
            //                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            //                    }
            //                }
            //            });
        }

        @Override
        public void paint(Graphics graphics) {
            super.paint(graphics);
            if (getTabCount() > 0) {
                Rectangle tabBounds = getUI().getTabBounds(this, 0);
                barHeight = tabBounds.height;
            }
            setting.setLocation(this.getWidth() - 50,
                (int) (this.getBoundsAt(0).getHeight() - AllIcons.Actions.InlayGear.getIconHeight()) / 2);
            setting.width = AllIcons.Actions.InlayGear.getIconWidth();
            setting.height = AllIcons.Actions.InlayGear.getIconHeight();
            cancel.setLocation(this.getWidth() - 25,
                (int) (this.getBoundsAt(0).getHeight() - AllIcons.Actions.Cancel.getIconHeight()) / 2);
            cancel.width = AllIcons.Actions.Cancel.getIconWidth();
            cancel.height = AllIcons.Actions.Cancel.getIconHeight();
            AllIcons.Actions.InlayGear.paintIcon(this, graphics, Utils.getX(setting), Utils.getY(setting));
            AllIcons.Actions.Cancel.paintIcon(this, graphics, Utils.getX(cancel), Utils.getY(cancel));
        }

        @Override
        public void mouseClicked(MouseEvent event) {
            if (setting.contains(event.getX(), event.getY())) {
                showDialog();
            } else if (cancel.contains(event.getX(), event.getY())) {
                Optional.ofNullable(onCloseHandler).ifPresent(it -> it.consume(event));
            } else {
                if (ProfilerLogManager.isDebugEnabled()) {
                    LOGGER.debug("mouseClicked point error");
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent event) {
            // srcBounds = DistributedDataPane.this.getBounds()
            // startPoint = SwingUtilities
            // .convertPoint(MyTabPanel.this, event.getPoint(), MyTabPanel.this.getRootPane().getLayeredPane())
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        @Override
        public void mouseEntered(MouseEvent event) {
        }

        @Override
        public void mouseExited(MouseEvent event) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
}
