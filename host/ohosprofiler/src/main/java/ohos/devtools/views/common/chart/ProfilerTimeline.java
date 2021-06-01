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

package ohos.devtools.views.common.chart;

import ohos.devtools.datasources.utils.datahandler.datapoller.MemoryDataConsumer;
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.charts.utils.ChartUtils;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.OperationUtils;
import ohos.devtools.views.common.chart.treetable.AgentDataModel;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.swing.LevelTablePanel;
import ohos.devtools.views.layout.swing.TaskScenePanelChart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static ohos.devtools.views.common.ColorConstants.RULER;
import static ohos.devtools.views.common.ColorConstants.TIMELINE_BG;
import static ohos.devtools.views.common.ColorConstants.TIMELINE_SCALE;
import static ohos.devtools.views.common.LayoutConstants.DEVICES_WIDTH;
import static ohos.devtools.views.common.LayoutConstants.LABEL_NAME_WIDTH;
import static ohos.devtools.views.common.ViewConstants.INITIAL_VALUE;
import static ohos.devtools.views.common.ViewConstants.NUM_10;
import static ohos.devtools.views.common.ViewConstants.NUM_1000;
import static ohos.devtools.views.common.ViewConstants.NUM_2;
import static ohos.devtools.views.common.ViewConstants.NUM_5;
import static ohos.devtools.views.common.ViewConstants.NUM_6;
import static ohos.devtools.views.common.ViewConstants.NUM_8;
import static ohos.devtools.views.common.ViewConstants.TIMELINE_FONT_SIZE;
import static ohos.devtools.views.common.ViewConstants.TIMELINE_MARK_COUNTS;
import static ohos.devtools.views.common.ViewConstants.ZOOM_IN_MIN;
import static ohos.devtools.views.common.ViewConstants.ZOOM_OUT_MAX;
import static ohos.devtools.views.common.ViewConstants.ZOOM_TIME;

/**
 * Profiler的时间线组件
 *
 * @since 2021/2/1 10:34
 */
public class ProfilerTimeline extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    private static final Logger LOGGER = LogManager.getLogger(MemoryDataConsumer.class);

    /**
     * 最底层面板
     */
    private final ProfilerChartsView bottomPanel;

    /**
     * taskScenePanel界面
     */
    private final TaskScenePanelChart taskScenePanelChart;

    private final JPanel jpanelSupen;

    /**
     * 时间线上可以展示的最大时间
     */
    private int maxDisplayTime;

    /**
     * 时间线刻度线上的最小刻度间隔
     */
    private int minMarkInterval;

    /**
     * Timeline的右侧
     */
    private int right;

    /**
     * Timeline的顶部
     */
    private int top;

    /**
     * 绘图时时间线的起始时间
     */
    private int startTime;

    /**
     * 绘图时时间线的结束时间
     */
    private int endTime;

    /**
     * 绘制Timeline时的坐标轴X0点
     *
     * @see "是日常中绘图习惯的坐标轴X0点，非Swing绘图的坐标轴原点"
     */
    private int x0;

    /**
     * 绘制Timeline时的坐标轴Y0点
     *
     * @see "是日常中绘图习惯的坐标轴Y0点，非Swing绘图的坐标轴原点"
     */
    private int y0;

    /**
     * 当时间线铺满面板后，绘制刻度时的起始时间偏移量
     */
    private int offsetTime = 0;

    /**
     * X轴起始绘图的坐标，因为动态Timeline和Chart从右往左出现
     */
    private int startCoordinate;

    /**
     * 每个X轴的时间单位占用的像素数
     */
    private BigDecimal pixelPerTime;

    /**
     * 拖拽框选时的锚点，即不动的点
     */
    private int dragAnchorPoint = INITIAL_VALUE;

    /**
     * 拖拽框选时的起点
     */
    private int dragStartPoint = INITIAL_VALUE;

    /**
     * 拖拽框选时的终点
     */
    private int dragEndPoint = INITIAL_VALUE;

    /**
     * 层级Table的Jpanel
     */
    private LevelTablePanel levelTablePanel = null;

    private boolean canDragged = true;

    /**
     * 是否正在拖拽
     */
    private boolean isDragging = false;

    /**
     * 构造函数
     *
     * @param bottomPanel         最底层面板
     * @param width               宽
     * @param height              高
     * @param taskScenePanelChart taskScenePanelChart
     */
    public ProfilerTimeline(ProfilerChartsView bottomPanel, int width, int height,
        TaskScenePanelChart taskScenePanelChart) {
        this.taskScenePanelChart = taskScenePanelChart;
        this.jpanelSupen = taskScenePanelChart.getJpanelSupen();
        this.bottomPanel = bottomPanel;
        this.setPreferredSize(new Dimension(width, height));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        this.setBackground(TIMELINE_BG);
        jpanelSupen.setBackground(ColorConstants.TRACE_TABLE_COLOR);
        jpanelSupen.setOpaque(true);
    }

    /**
     * paintComponent
     *
     * @param graphics graphics
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        // 初始化坐标和比例尺等信息
        initPoint();
        // 绘制时间线坐标轴
        drawAxis(graphics);
        // 绘制框选时的时间线上的倒三角
        drawSelectedMark(graphics);
        // 更新标尺的坐标点
        updateRulerPoint();
        // 把当前组件标记为已绘制，刷新其他组件的标尺（Timeline上默认不显示标尺，只是标尺会跟随Timeline上的鼠标移动）
        this.bottomPanel.compRulerDrawn(this);
        this.bottomPanel.refreshCompRuler();
    }

    /**
     * 初始化坐标和比例尺等信息
     */
    private void initPoint() {
        // 确定绘制出的坐标轴的原点
        x0 = this.getX();
        right = x0 + this.getWidth();
        top = this.getY();
        y0 = top + this.getHeight() - 1;
        if (right == 0 || maxDisplayTime == 0) {
            return;
        }
        // 计算出1个时间单位多少像素
        pixelPerTime = OperationUtils.divide(right, maxDisplayTime);
        // 如果当前时间大于最大时间，要计算偏移量
        if (endTime > maxDisplayTime && minMarkInterval != 0) {
            startCoordinate = x0;
            // 判断是否有偏移时间
            if (endTime % minMarkInterval == 0) {
                offsetTime = 0;
            } else {
                // 如果当前时间和minMarkInterval取余不为0，则计算偏移量：最小间隔 - 当前时间 % 最小间隔
                offsetTime = minMarkInterval - endTime % minMarkInterval;
            }
        } else {
            // 如果当前时间小于最大时间，则需要从中间绘制时间轴，偏移量为0
            offsetTime = 0;
            startCoordinate = x0 + right - OperationUtils.multiply(pixelPerTime, endTime);
        }
    }

    /**
     * 绘制时间线的坐标轴
     *
     * @param graphics Graphics
     */
    private void drawAxis(Graphics graphics) {
        graphics.setColor(TIMELINE_SCALE);
        // 绘制Timeline左侧的竖线
        graphics.drawLine(x0, top, x0, y0);
        // 绘制Timeline的横线
        graphics.drawLine(x0, y0, right, y0);
        // 时间轴是否绘满界面
        boolean flag = endTime > maxDisplayTime;
        // 从offsetTime开始绘制时间线（其实是画刻度），绘制时间范围其实也是从0到max
        for (int drawTime = offsetTime; drawTime <= maxDisplayTime; drawTime += this.minMarkInterval) {
            int x = startCoordinate + ChartUtils.multiply(pixelPerTime, drawTime);
            // 计算出实际要展示的时间
            int showTime;
            if (flag) {
                showTime = startTime + drawTime;
            } else {
                showTime = drawTime;
            }
            double result = (showTime / this.minMarkInterval) % TIMELINE_MARK_COUNTS;
            // 每隔N个minMarkInterval绘制坐标轴数字和大刻度
            if (result == 0) {
                // 绘制长刻度
                graphics.setColor(TIMELINE_SCALE);
                graphics.drawLine(x, y0, x, top);
                graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TIMELINE_FONT_SIZE));
                // 转换后的时间
                String str = millisecondToTime(showTime);
                graphics.setColor(RULER);
                graphics.drawString(str, x + NUM_5, y0 - NUM_10);
            } else {
                graphics.setColor(TIMELINE_SCALE);
                graphics.drawLine(x, top, x, top + NUM_6);
            }
        }
    }

    /**
     * 绘制时间线的坐标数字
     *
     * @param time time
     * @return String
     */
    private String millisecondToTime(int time) {
        String timeStr;
        int hour;
        int minute;
        int second;
        int millisecond;
        int num60 = NUM_6 * NUM_10;
        if (time <= 0) {
            return "0s";
        } else {
            second = time / NUM_1000;
            minute = second / num60;
            millisecond = time % NUM_1000;
            if (second < num60) {
                timeStr = secondFormat(second) + "." + millisecondFormat(millisecond) + "s";
            } else if (minute < num60) {
                second = second % num60;
                timeStr =
                    secondFormat(minute) + ":" + secondFormat(second) + "." + millisecondFormat(millisecond) + "s";
            } else {
                hour = minute / num60;
                minute = minute % num60;
                int num3600 = num60 * num60;
                second = second - hour * num3600 - minute * num60;
                timeStr = secondFormat(hour) + ":" + secondFormat(minute) + ":" + secondFormat(second) + "."
                    + millisecondFormat(millisecond) + "s";
            }
        }
        return timeStr;
    }

    /**
     * 时分秒的格式转换
     *
     * @param secondTime secondTime
     * @return String
     */
    private String secondFormat(int secondTime) {
        String retStr;
        if (secondTime == 0) {
            retStr = "00";
        } else if (secondTime > 0 && secondTime < NUM_10) {
            retStr = Integer.toString(secondTime);
        } else {
            retStr = "" + secondTime;
        }
        return retStr;
    }

    /**
     * 毫秒的格式转换
     *
     * @param millisecondTime millisecondTime
     * @return String
     */
    private String millisecondFormat(int millisecondTime) {
        String retStr;
        if (millisecondTime == 0) {
            retStr = "000";
        } else if (millisecondTime > 0 && millisecondTime < NUM_10) {
            retStr = Integer.toString(millisecondTime);
        } else if (millisecondTime >= NUM_10 && millisecondTime < NUM_10 * NUM_10) {
            retStr = Integer.toString(millisecondTime);
        } else {
            retStr = "" + millisecondTime;
        }
        return retStr;
    }

    /**
     * 绘制Timeline框选的标记
     *
     * @param graphics Graphics
     */
    private void drawSelectedMark(Graphics graphics) {
        ChartDataRange selectedRange = this.bottomPanel.getObserver().getStandard().getSelectedRange();
        if (selectedRange == null) {
            return;
        }
        if (selectedRange.getStartTime() != Integer.MIN_VALUE) {
            int selectStartX =
                startCoordinate + OperationUtils.multiply(pixelPerTime, selectedRange.getStartTime() - startTime);
            drawInvertedTriangle(selectStartX, graphics);
        }
        if (selectedRange.getEndTime() != Integer.MAX_VALUE) {
            int selectEndX =
                startCoordinate + OperationUtils.multiply(pixelPerTime, selectedRange.getEndTime() - startTime);
            drawInvertedTriangle(selectEndX, graphics);
        }
    }

    /**
     * 画一个倒三角形
     *
     * @param bottomVertexX 倒三角形下方的顶点
     * @param graphics      Graphics
     */
    private void drawInvertedTriangle(int bottomVertexX, Graphics graphics) {
        // 创建一个多边形对象
        Polygon polygon = new Polygon();
        polygon.addPoint(bottomVertexX, y0);
        polygon.addPoint(bottomVertexX - NUM_5, y0 - NUM_5);
        polygon.addPoint(bottomVertexX + NUM_5, y0 - NUM_5);
        polygon.addPoint(bottomVertexX, y0);
        graphics.fillPolygon(polygon);
    }

    /**
     * 更新标尺的坐标点
     */
    private void updateRulerPoint() {
        // Timeline上不画标尺，但是鼠标移动时，要更新坐标点，通知其他组件绘制标尺
        Point mousePoint = getMousePosition();
        if (mousePoint != null) {
            this.bottomPanel.setRulerXCoordinate((int) mousePoint.getX());
        }
    }

    /**
     * mouseClicked
     *
     * @param mouseEvent mouseEvent
     */
    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        // 鼠标点击事件，单击后停止刷新数据，等待用户框选数据
        int button = mouseEvent.getButton();
        int mouseX = mouseEvent.getX();
        // 如果左键点击的点小于起始点，则不更新
        if (button == MouseEvent.BUTTON1 && mouseX < startCoordinate) {
            return;
        }
        // 目前仅左键单击触发框选场景
        if (button == MouseEvent.BUTTON1) {
            this.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
            // start和end同时更新
            mouseLeftClick(mouseX);
        } else {
            // 右键取消选择，清空框选范围
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            mouseRightClick();
        }
        // 鼠标点击后，也需要组件的标尺需要重新绘制（主要是将Chart绘制为半透明状态）
        this.bottomPanel.resetRulerDrawStatus();
    }

    private void mouseLeftClick(int mouseX) {
        dragStartPoint = mouseX;
        dragEndPoint = mouseX;
        this.bottomPanel.getObserver().getStandard().updateSelectedStart(getTimeByMouseX(mouseX));
        this.bottomPanel.getObserver().getStandard().updateSelectedEnd(getTimeByMouseX(mouseX));
        checkLevelTablePanel();
        // 停止刷新数据，手动刷新界面，否则界面不会变暗
        doNotifyRefresh(bottomPanel.isChartLevel());
        bottomPanel.setAbleUnfoldTable(true);
        // 增加chart点击和暂停开始按钮的事件联动
        // 修改OhosProfiler应用任务停止后连续左击和右击时间线时“暂停/恢复”按钮会相应变化
        if (!bottomPanel.isStopFlag()) {
            taskScenePanelChart.getjButtonStop()
                .setIcon(new ImageIcon(ProfilerTimeline.class.getClassLoader().getResource("images/suspended.png")));
        }
        if (!bottomPanel.isFlagDown() && bottomPanel.isChartLevel()) {
            taskScenePanelChart.getjButtonBottom().setIcon(
                new ImageIcon(ProfilerTimeline.class.getClassLoader().getResource("images/button_bottom_bar.png")));
        }
    }

    private void checkLevelTablePanel() {
        if (levelTablePanel == null) {
            long sessionId = this.bottomPanel.getSessionId();
            levelTablePanel = new LevelTablePanel(jpanelSupen, sessionId);
            levelTablePanel.setPreferredSize(new Dimension(DEVICES_WIDTH, LABEL_NAME_WIDTH));
        }
    }

    void mouseRightClick() {
        dragStartPoint = INITIAL_VALUE;
        dragEndPoint = INITIAL_VALUE;
        this.bottomPanel.getObserver().getStandard().clearSelectedRange();
        removeTablePanel();
        // 恢复刷新数据
        this.bottomPanel.getObserver().restartRefresh();
        bottomPanel.setAbleUnfoldTable(false);
        // 修改OhosProfiler应用任务停止后连续左击和右击时间线时“暂停/恢复”按钮会相应变化
        if (!bottomPanel.isStopFlag()) {
            taskScenePanelChart.getjButtonStop()
                .setIcon(new ImageIcon(ProfilerTimeline.class.getClassLoader().getResource("images/button_stop.png")));
        }
        taskScenePanelChart.getjButtonBottom().setIcon(
            new ImageIcon(ProfilerTimeline.class.getClassLoader().getResource("images/button_bottom_bar_grey.png")));
        bottomPanel.setFlagDown(false);
        // 手动刷新界面，否则界面不会变亮
        ChartDataRange range = bottomPanel.getObserver().getStandard().getDisplayRange();
        bottomPanel.getObserver().notifyRefresh(range.getStartTime(), range.getEndTime());
    }

    /**
     * 鼠标左键点击事件
     */
    public void addTablePanel() {
        checkLevelTablePanel();
        this.bottomPanel.add(levelTablePanel, BorderLayout.SOUTH);
        this.bottomPanel.setFlagDown(false);
    }

    /**
     * 鼠标右键点击事件
     */
    public void removeTablePanel() {
        if (levelTablePanel != null) {
            // 点击右键去除层级table
            this.bottomPanel.remove(levelTablePanel);
            levelTablePanel = null;
        }
        jpanelSupen.setVisible(false);
        this.bottomPanel.setFlagDown(true);
    }

    /**
     * doNotifyRefresh 停止刷新数据，手动刷新界面，否则界面不会变暗
     *
     * @param secondFlag secondFlag
     */
    private void doNotifyRefresh(boolean secondFlag) {
        if (secondFlag) {
            this.bottomPanel.add(levelTablePanel, BorderLayout.SOUTH);
            this.bottomPanel.setFlagDown(false);
        }
        // 停止刷新数据
        this.bottomPanel.getObserver().pauseRefresh();
        // 手动刷新界面，否则界面不会变暗
        ChartDataRange range = bottomPanel.getObserver().getStandard().getDisplayRange();
        bottomPanel.getObserver().notifyRefresh(range.getStartTime(), range.getEndTime());
    }

    /**
     * 通过鼠标的X坐标，计算当前坐标对应的Timeline的X轴时间
     *
     * @param mouseX 鼠标的X坐标
     * @return 对应的Timeline的X轴时间
     * @see "注意这里算出来的时间，Timeline的X轴上对应的时间，这个时间点不一定在绘制Chart的dataMap的keyset里，
     * <p>
     * 有可能是出于某2个值中间，使用时需要找到与dataMap的keyset最接近的值"
     */
    private int getTimeByMouseX(int mouseX) {
        // 计算公式为：鼠标上对应的时间 = (鼠标X坐标 - 绘制Chart的X起始坐标) / X轴1个单位对应的像素数 + 起始时间
        return OperationUtils.divide(mouseX - startCoordinate, pixelPerTime) + startTime;
    }

    /**
     * 通过时间，计算当前时间对应的Timeline的X轴坐标
     *
     * @param time 当前时间
     * @return 对应的Timeline的X轴坐标值
     */
    private int getPointXByTime(int time) {
        // 计算公式为：鼠标上对应的坐标 = X轴1个单位对应的像素数 * (当前时间 - 绘制Chart的X起始时间) + 绘制Chart的X起始坐标
        return OperationUtils.multiply(pixelPerTime, time - startTime) + startCoordinate;
    }

    /**
     * 鼠标按下事件
     *
     * @param mouseEvent MouseEvent
     * @see "drag之前会先触发一次press，press时就可以确定拖拽时的锚点（即拖拽时不动的那一条竖线标尺）"
     */
    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        ChartDataRange selectedRange = this.bottomPanel.getObserver().getStandard().getSelectedRange();
        int mouseX = mouseEvent.getX();
        // dragStartPoint在滚动条拖动后  数值可能发生了变化  优化： 将最后框选的时间确定下来  不按照坐标去得到dragStartPoint，而是按照最后框选
        // 时刻的时间作为标准 只是在最终将标准时间又转为坐标去判断
        if (selectedRange != null) {
            dragStartPoint = getPointXByTime(selectedRange.getStartTime());
            dragEndPoint = getPointXByTime(selectedRange.getEndTime());
        }
        // selectedRange为null，说明是没有click，直接开始拖拽，暂时不考虑这种场景
        if (Math.abs(mouseX - dragStartPoint) > NUM_10 && Math.abs(mouseX - dragEndPoint) > NUM_10) {
            canDragged = false;
        }
        if (selectedRange == null) {
            return;
        }
        boolean isCloseToStart = Math.abs(mouseX - dragStartPoint) < NUM_10;
        boolean isCloseToEnd = Math.abs(mouseX - dragEndPoint) < NUM_10;

        if (isCloseToStart) {
            dragAnchorPoint = dragEndPoint;
            canDragged = true;
        }
        if (isCloseToEnd) {
            dragAnchorPoint = dragStartPoint;
            canDragged = true;
        }
    }

    /**
     * mouseReleased
     *
     * @param mouseEvent mouseEvent
     */
    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        // 非左键的release事件不处理
        if (mouseEvent.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        /*
         *  拖拽的触发顺序为：press -> drag -> release，而点击为：press -> release -> click
         *  拖拽时SelectRange在drag步骤更新，所以release时拿到的range是更新后的
         *  点击时SelectRange在click步骤更新，release时还未更新，所以这里需要手动set一下 ↓↓↓
         */
        if (!isDragging) {
            int mouseX = mouseEvent.getX();
            this.bottomPanel.getObserver().getStandard().updateSelectedStart(getTimeByMouseX(mouseX));
            this.bottomPanel.getObserver().getStandard().updateSelectedEnd(getTimeByMouseX(mouseX));
        }
        if (bottomPanel.isChartLevel()) {
            new SwingWorker<AgentDataModel, Object>() {
                @Override
                protected AgentDataModel doInBackground() {
                    // treeTable这时还未初始化完成，这里等待一下
                    while (levelTablePanel == null || levelTablePanel.getTreeTable() == null) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(10);
                        } catch (InterruptedException exception) {
                            LOGGER.error("Drag swing work done error.");
                        }
                    }
                    long sessionId = bottomPanel.getSessionId();
                    return levelTablePanel.getTreeDataModel(sessionId);
                }

                @Override
                protected void done() {
                    try {
                        AgentDataModel model = get();
                        if (model == null) {
                            return;
                        }
                        if (levelTablePanel == null || levelTablePanel.getTreeTable() == null) {
                            return;
                        }
                        levelTablePanel.getTreeTable().setTreeTableModel(model);
                    } catch (InterruptedException | ExecutionException | NullPointerException exception) {
                        LOGGER.error("Drag swing work done error.", exception);
                    }
                }
            }.execute();
        }
        isDragging = false;
    }

    /**
     * mouseEntered
     *
     * @param mouseEvent mouseEvent
     */
    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
        // 当前组件需要绘制标尺，鼠标进入，则更新底层父级panel的currentEntered
        this.bottomPanel.setCurrentEntered(this);
    }

    /**
     * mouseExited
     *
     * @param mouseEvent mouseEvent
     */
    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        // 当前组件需要绘制标尺，鼠标退出，则更新底层父级panel的currentEntered为null
        this.bottomPanel.setCurrentEntered(null);
        // 这里需要手动调用重绘一下所有Panel，否则会残留有之前的ruler
        this.bottomPanel.resetRulerDrawStatus();
        this.bottomPanel.refreshCompRuler();
    }

    /**
     * mouseDragged
     *
     * @param mouseEvent mouseEvent
     */
    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        isDragging = true;
        ChartDataRange selectedRange = this.bottomPanel.getObserver().getStandard().getSelectedRange();
        if (selectedRange == null || !canDragged) {
            return;
        }
        int mouseX = mouseEvent.getX();
        if (mouseX > dragAnchorPoint) {
            // 鼠标位置大于锚点位置，说明是在锚点右侧拖拽，更新end为鼠标的点即可
            dragEndPoint = mouseX;
            if (dragAnchorPoint != INITIAL_VALUE) {
                dragStartPoint = dragAnchorPoint;
                this.bottomPanel.getObserver().getStandard().updateSelectedStart(getTimeByMouseX(dragAnchorPoint));
            }
            this.bottomPanel.getObserver().getStandard().updateSelectedEnd(getTimeByMouseX(mouseX));
        } else {
            // 鼠标位置小于锚点位置，说明是在锚点左侧拖拽，则更新end为锚点，start为鼠标的点
            dragStartPoint = mouseX;
            if (dragAnchorPoint != INITIAL_VALUE) {
                dragEndPoint = dragAnchorPoint;
                this.bottomPanel.getObserver().getStandard().updateSelectedEnd(getTimeByMouseX(dragAnchorPoint));
            }
            this.bottomPanel.getObserver().getStandard().updateSelectedStart(getTimeByMouseX(mouseX));
        }
        // 鼠标移动，则所有组件的标尺需要重新绘制
        this.bottomPanel.resetRulerDrawStatus();
        // 如果鼠标在框选的标尺附近，样式要变为可拉伸
        checkCursorStyle(mouseEvent.getX(), mouseEvent.getY());
        this.repaint();
        this.revalidate();
    }

    /**
     * mouseMoved
     *
     * @param mouseEvent mouseEvent
     */
    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        // 鼠标移动，则所有组件的标尺需要重新绘制
        this.bottomPanel.resetRulerDrawStatus();
        // 如果鼠标在框选的标尺附近，样式要变为可拉伸
        checkCursorStyle(mouseEvent.getX(), mouseEvent.getY());
        this.repaint();
        this.revalidate();
    }

    /**
     * mouseWheelMoved
     *
     * @param mouseWheelEvent mouseWheelEvent
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
        if (true) {
            return;
        }
        ChartDataRange zoomRange = this.bottomPanel.getObserver().getStandard().getDisplayRange();
        if (zoomRange == null || startTime == 0) {
            return;
        }
        // 缩放的char动静判断
        boolean moveDecide = false;
        // 缩放前的的开始和结束时间
        int startTimed = zoomRange.getStartTime();
        int endTimed = zoomRange.getEndTime();
        // 计算x轴缩放前显示的时间长度
        int lengthX = endTimed - startTimed;
        // 计算比例尺
        BigDecimal divide = OperationUtils.divide(lengthX, maxDisplayTime);
        // 是否允许缩放的判断
        boolean zoomAllow = true;
        // 缩放的判断
        int zoomDecide = mouseWheelEvent.getWheelRotation();
        if (zoomDecide == 1) {
            moveDecide = true;
            if (maxDisplayTime < ZOOM_IN_MIN) {
                zoomAllow = false;
            }
            if (zoomAllow) {
                // zoomIn向后放大 放大增加判断：放大到一定程度玖不能放大了
                maxDisplayTime = maxDisplayTime - ZOOM_TIME;
            }
        } else if (-zoomDecide == 1) {
            moveDecide = false;
            if (maxDisplayTime > ZOOM_OUT_MAX) {
                zoomAllow = false;
            }
            if (zoomAllow) {
                // zoomOut 向前缩小 缩小增加判断：缩小到一定程度玖不能放大了
                maxDisplayTime = maxDisplayTime + ZOOM_TIME;
            }
        } else {
            zoomAllow = false;
        }
        // x轴缩放后的总时间 之后计算鼠标落点的比例
        int multiply = OperationUtils.multiply(divide, maxDisplayTime);
        // 缩放的增减时间长度
        int changeLength = OperationUtils.divideInt(multiply - lengthX, NUM_2);
        // 调用界面刷新
        zoomRefresh(changeLength, startTimed, endTimed, moveDecide, zoomDecide);
    }

    /**
     * 根据鼠标的坐标，更新鼠标样式
     *
     * @param changeLength 改变的刻度值
     * @param startTimed   新的开始时间
     * @param endTimed     新的结束时间
     * @param moveDecide   是否可以缩放判断
     * @param zoomDecide   滚轮缩放的程度值
     */
    private void zoomRefresh(int changeLength, int startTimed, int endTimed, boolean moveDecide, int zoomDecide) {
        // 缩放后的起始和结束时间
        int newStartTime = 0;
        int newEndTime = 0;
        // 根据缩放选择获取开始结束时间
        if (zoomDecide == 1) {
            // 放大后的起始和结束时间
            newStartTime = startTimed - changeLength;
            newEndTime = endTimed + changeLength;

        } else if (-zoomDecide == 1) {
            // 缩小后的起始和结束时间
            newStartTime = startTimed + changeLength;
            newEndTime = endTimed - changeLength;
        } else {
            newStartTime = startTimed;
            newEndTime = endTimed;
        }
        ChartStandard standard = this.bottomPanel.getObserver().getStandard();
        int currentTime = (int) (standard.getLastTimestamp() - standard.getFirstTimestamp());
        // 当前展示的时间大于最大展示时间，则不刷新
        int maxDisplay = this.bottomPanel.getObserver().getStandard().getMaxDisplayMillis();
        if (currentTime <= maxDisplay) {
            return;
        }
        // 缩放变化放入观察者去刷新
        this.bottomPanel.getObserver().charZoom(newStartTime, newEndTime, maxDisplayTime);
        if (moveDecide) {
            // 调用观察者的停止刷新方法
            this.bottomPanel.getObserver().stopRefresh(false);
        } else {
            // 恢复刷新数据
            this.bottomPanel.getObserver().restartRefresh();
        }
        // 调用重绘方法
        this.repaint();
        this.revalidate();
    }

    /**
     * 根据鼠标的坐标，更新鼠标样式
     *
     * @param crtMouseX 鼠标X轴坐标
     * @param crtMouseY 鼠标Y轴坐标
     */
    private void checkCursorStyle(int crtMouseX, int crtMouseY) {
        ChartDataRange selectedRange = this.bottomPanel.getObserver().getStandard().getSelectedRange();
        if (selectedRange == null) {
            return;
        }
        // Y轴是否也接近倒三角的标记点
        boolean isCloseToCoordinateY = Math.abs(this.getHeight() - crtMouseY) < NUM_8;
        int selectStart =
            startCoordinate + OperationUtils.multiply(pixelPerTime, selectedRange.getStartTime() - startTime);
        int selectEnd = startCoordinate + OperationUtils.multiply(pixelPerTime, selectedRange.getEndTime() - startTime);
        // X轴和Y轴坐标，都靠近倒三角的标记点时，鼠标变为resize状态
        if (Math.abs(selectStart - crtMouseX) < NUM_5 && isCloseToCoordinateY) {
            this.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
        } else if (Math.abs(selectEnd - crtMouseX) < NUM_5 && isCloseToCoordinateY) {
            this.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
        } else {
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * setMaxDisplayTime
     *
     * @param maxDisplayTime maxDisplayTime
     */
    public void setMaxDisplayTime(int maxDisplayTime) {
        this.maxDisplayTime = maxDisplayTime;
    }

    /**
     * setMinMarkInterval
     *
     * @param minMarkInterval minMarkInterval
     */
    public void setMinMarkInterval(int minMarkInterval) {
        this.minMarkInterval = minMarkInterval;
    }

    /**
     * getStartTime
     *
     * @return int
     */
    public int getStartTime() {
        return startTime;
    }

    /**
     * setStartTime
     *
     * @param startTime startTime
     */
    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    /**
     * getEndTime
     *
     * @return int
     */
    public int getEndTime() {
        return endTime;
    }

    /**
     * setEndTime
     *
     * @param endTime endTime
     */
    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    /**
     * getLevelTablePanel
     *
     * @return LevelTablePanel
     */
    public LevelTablePanel getLevelTablePanel() {
        return levelTablePanel;
    }
}
