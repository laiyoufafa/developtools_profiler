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

import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;

import static ohos.devtools.views.common.ViewConstants.INITIAL_VALUE;
import static ohos.devtools.views.common.OperationUtils.divide;
import static ohos.devtools.views.common.OperationUtils.multiply;

/**
 * 自定义滚动条
 *
 * @see "Chart面板上的水平滚动条"
 * @see "实现思路：控件是一个自定义布局的JPanel，滚动条是一个填充了背景色的子JPanel，拖动滚动条时，手动计算移动距离并setBound"
 * @since 2021/2/20 14:18
 */
public class ProfilerScrollbar extends JPanel {
    /**
     * 滚动条的高度
     */
    private static final int BAR_HEIGHT = 8;

    /**
     * 滚动条的最小宽度
     */
    private static final int MIN_WIDTH = 20;

    /**
     * 窗口的父面板，即要悬浮在哪个面板上
     */
    private final ProfilerChartsView parent;

    /**
     * 滚动条可移动范围的宽度
     */
    private int moveScopeWidth;

    /**
     * 拖动时的滚动条控件
     */
    private JPanel bar;

    /**
     * 拖动滚动条时，滚动条起点坐标和鼠标点之间的偏移量
     */
    private int offsetPixel = INITIAL_VALUE;

    /**
     * 构造函数
     *
     * @param parent 父面板
     */
    public ProfilerScrollbar(ProfilerChartsView parent) {
        this.parent = parent;
        initBar();
        addListener();
    }

    /**
     * 初始化ScrollBar
     */
    private void initBar() {
        if (parent == null) {
            return;
        }
        this.setVisible(true);
        this.setLayout(null);
        // 初始化用于拖动的bar
        bar = new JPanel();
        // 滚动条颜色
        bar.setBackground(ColorConstants.SCROLLBAR);
        // 第一次出现时表明Chart刚刚铺满界面，此时滚动条应和整个Chart一样长，后面随着时间推移，滚动条慢慢缩小
        moveScopeWidth = parent.getWidth();
        bar.setPreferredSize(new Dimension(moveScopeWidth, BAR_HEIGHT));
        bar.setBounds(0, 0, bar.getPreferredSize().width, bar.getPreferredSize().height);
        // 滚动条底板颜色设置
        this.setBackground(ColorConstants.CHART_BG);
        this.add(bar);
        this.setPreferredSize(new Dimension(moveScopeWidth, BAR_HEIGHT));
    }

    /**
     * 添加监听器
     */
    private void addListener() {
        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                dragEvent(mouseEvent);
            }
        });
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                pressEvent(mouseEvent);
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                releaseEvent(mouseEvent);
            }
        });
    }

    /**
     * 自定义鼠标按下事件
     *
     * @param mouseEvent MouseEvent
     */
    private void pressEvent(MouseEvent mouseEvent) {
        // 按下时计算滚动条起点坐标和鼠标点之间的偏移量
        offsetPixel = mouseEvent.getX() - bar.getX();
    }

    /**
     * 自定义鼠标释放事件
     *
     * @param mouseEvent MouseEvent
     */
    private void releaseEvent(MouseEvent mouseEvent) {
        // 恢复偏移量为初始值
        offsetPixel = INITIAL_VALUE;
    }

    /**
     * 自定义拖拽事件
     *
     * @param mouseEvent MouseEvent
     */
    private void dragEvent(MouseEvent mouseEvent) {
        int newLocationX = getNewLocationX(mouseEvent);
        int barWidth = bar.getWidth();
        if (moveScopeWidth == (barWidth + newLocationX) && !parent.isStopFlag()) {
            setRestartRefresh();
            return;
        }
        // 拖拽时停止刷新Chart
        if (!parent.isStopFlag()) {
            setPauseRefresh();
        }
        // 计算拖动后的应显示的时间区域
        calcTimeRange(newLocationX);
        // 更新滚动条位置
        bar.setBounds(newLocationX, 0, bar.getWidth(), BAR_HEIGHT);
    }

    /**
     * 设置重启状态
     */
    private void setRestartRefresh() {
        parent.getTaskScenePanelChart().getjButtonStop()
            .setIcon(new ImageIcon(ProfilerScrollbar.class.getClassLoader().getResource("images/button_stop.png")));
        parent.getTaskScenePanelChart().getjButtonStop().setToolTipText("暂停");
        parent.getObserver().restartRefresh();
        parent.setFlagEnd(false);
        // 拉到最右边重新开始刷新，这里也要清掉agent table等信息，调用时间线上的右击方法即可
        parent.getTimeline().mouseRightClick();
    }

    /**
     * 设置暂停状态
     */
    private void setPauseRefresh() {
        parent.getTaskScenePanelChart().getjButtonStop()
            .setIcon(new ImageIcon(ProfilerScrollbar.class.getClassLoader().getResource("images/suspended.png")));
        parent.getTaskScenePanelChart().getjButtonStop().setToolTipText("开始");
        parent.getObserver().pauseRefresh();
        parent.setFlagEnd(true);
    }

    /**
     * 通过拖动距离计算时间范围的变化
     *
     * @param newLocationX 滚动条新位置的X坐标
     */
    private void calcTimeRange(int newLocationX) {
        // 空白宽度：组件宽度 - 滚动条宽度
        int emptyWidth = moveScopeWidth - bar.getWidth();
        if (emptyWidth == 0) {
            return;
        }
        // 隐藏掉的时间：lastTime - displayTime
        ChartStandard standard = parent.getObserver().getStandard();
        int lastTime = (int) (standard.getLastTimestamp() - standard.getFirstTimestamp());
        int hideTime = lastTime - standard.getMaxDisplayMillis();
        // 计算未占用的地方，1px代表多少隐藏时间，得到一个比例
        BigDecimal scale = divide(hideTime, emptyWidth);
        // 拿到滚动条移动了多少px：新位置减去bar当前位置
        int movePixel = newLocationX - bar.getX();
        // 计算出时间应该偏移多少
        int timeOffset = multiply(scale, movePixel);
        ChartDataRange oldRange = standard.getDisplayRange();
        int newStart = oldRange.getStartTime() + timeOffset;
        // 如果newStart不能小于0
        if (newStart < 0) {
            newStart = 0;
        }
        // 如果拖至最左边和最右边，需要特殊处理
        if (newLocationX == 0) {
            newStart = 0;
        }
        if (newLocationX == moveScopeWidth - bar.getWidth()) {
            newStart = lastTime - standard.getMaxDisplayMillis();
        }
        int newEnd = newStart + standard.getMaxDisplayMillis();
        parent.getObserver().notifyRefresh(newStart, newEnd);
    }

    /**
     * 鼠标移动后，获取新的滚动条X坐标
     *
     * @param mouseEvent MouseEvent
     * @return int
     */
    private int getNewLocationX(MouseEvent mouseEvent) {
        // 拖拽后滚动条的起点X坐标即为鼠标位置-偏移量
        int newLocationX = mouseEvent.getX() - offsetPixel;
        // 如果起始坐标从左侧超出了父Panel，需要限制一下newLocationX
        newLocationX = Math.max(newLocationX, 0);
        // 同理，结束坐标超出了父Panel的宽度，也要限制newLocationX，不能超出
        int newLocationEndX = newLocationX + bar.getWidth();
        if (newLocationEndX >= parent.getWidth()) {
            newLocationX = parent.getWidth() - bar.getWidth();
        }
        return newLocationX;
    }

    /**
     * 计算并调整滚动条调整后的宽度和位置
     */
    public void resizeAndReposition() {
        ChartStandard standard = parent.getObserver().getStandard();
        int lastTime = (int) (standard.getLastTimestamp() - standard.getFirstTimestamp());
        // 最后的时间小于最大展示时间，则不需要滚动条
        int maxDisplay = parent.getObserver().getStandard().getMaxDisplayMillis();
        if (lastTime <= maxDisplay) {
            return;
        }
        // 每次调整位置和长度时，需要更新moveScopeWidth，因为界面可能缩放
        moveScopeWidth = parent.getWidth();
        /* ------- 用户可能把滚动条拖动值中间，然后触发当前方法，所以要计算进度条前面的空白长度来确定滚动条位置 ------- */
        // 计算进度条前面的空白长度
        int startTime = standard.getDisplayRange().getStartTime();
        BigDecimal leftEmptyRatio = divide(startTime, lastTime);
        int leftEmptyWidth = multiply(leftEmptyRatio, moveScopeWidth);
        // 计算进度条的比例：当前展示时间/本次任务最后的时间
        BigDecimal barRatio = divide(maxDisplay, lastTime);
        int barNewWidth = multiply(barRatio, moveScopeWidth);
        // 保证宽度不能超过最小值，否则会滚动条会缩小到看不清
        if (barNewWidth < MIN_WIDTH) {
            barNewWidth = MIN_WIDTH;
            // 保证宽度不能超过最小值后，滚动条的比例和空白的比例已经不一样了，需要重新计算，比例和宽度均减去maxDisplay(barNewWidth)
            leftEmptyRatio = divide(startTime, lastTime - maxDisplay);
            leftEmptyWidth = multiply(leftEmptyRatio, moveScopeWidth - barNewWidth);
        }
        bar.setPreferredSize(new Dimension(barNewWidth, BAR_HEIGHT));
        // Chart再刷新时，滚动条一定在最右边，由于上面的运算会丢失精度，会导致滚动条在闪烁，这里做个运算保证滚动条一直在右侧
        if (parent.getObserver().isRefreshing()) {
            leftEmptyWidth = moveScopeWidth - barNewWidth;
        }
        // 更新进度条位置
        bar.setBounds(leftEmptyWidth, 0, barNewWidth, BAR_HEIGHT);
        this.repaint();
        this.revalidate();
    }
}
