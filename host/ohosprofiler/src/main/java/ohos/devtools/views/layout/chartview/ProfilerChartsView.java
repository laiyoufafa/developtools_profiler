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

package ohos.devtools.views.layout.chartview;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.layout.chartview.ability.ProfilerAppAbility;
import ohos.devtools.views.layout.chartview.observer.AbilityObserver;
import ohos.devtools.views.layout.chartview.observer.ProfilerChartsViewPublisher;
import ohos.devtools.views.layout.chartview.observer.TimelineObserver;

import javax.swing.Icon;
import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static ohos.devtools.views.layout.chartview.utils.ChartViewConstants.TIMELINE_HEIGHT;
import static ohos.devtools.views.layout.chartview.utils.ChartViewConstants.TIMELINE_WIDTH;

/**
 * Profiler Chart view main panel
 *
 * @since: 2021/10/25
 */
public class ProfilerChartsView extends JBPanel {
    /**
     * Map of saving Session id and ProfilerChartsView
     *
     * @see "Key: Session id, value: ProfilerChartsView"
     */
    public static Map<Long, ProfilerChartsView> sessionMap = new HashMap<>();

    /**
     * Session id
     */
    private final long sessionId;

    /**
     * Event publisher of charts display view
     */
    private final ProfilerChartsViewPublisher publisher;

    /**
     * Panel to save timeline and monitor items
     */
    private final JBPanel mainPanel;

    /**
     * Saves the parent panel of the current view
     */
    private TaskScenePanelChart taskScenePanelChart;

    /**
     * Panel to save all monitor items
     */
    private ItemsView itemsView;

    /**
     * User-defined horizontal scroll bar
     */
    private ProfilerScrollbar horizontalBar;

    /**
     * User-defined timeline
     */
    private ProfilerTimeline timeline;

    /**
     * User-defined loading panel
     */
    private JBPanel loadingPanel;

    /**
     * ability Slice
     */
    private ProfilerAppAbility abilitySlice;

    private JBScrollPane itemsScroll;

    /**
     * Sign of pause
     */
    private boolean pause = false;

    /**
     * Sign of stop
     */
    private boolean stop = false;

    /**
     * Sign of add item
     */
    private boolean addItemFlag = false;

    /**
     * Is chart loading (waiting for database to process data during initialization)
     */
    private boolean loading = false;

    /**
     * Constructor
     *
     * @param sessionId Session id
     * @param traceFile Is track file static import mode
     * @param taskScenePanelChart Saves the parent panel of the current view
     */
    public ProfilerChartsView(long sessionId, boolean traceFile, TaskScenePanelChart taskScenePanelChart) {
        super(true);
        this.setOpaque(true);
        this.setLayout(new BorderLayout());
        this.sessionId = sessionId;
        this.taskScenePanelChart = taskScenePanelChart;
        this.mainPanel = new JBPanel(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
        this.publisher = new ProfilerChartsViewPublisher(this, traceFile);
        initTimeline();
        JBPanel timeAbilityPanel = new JBPanel(new BorderLayout());
        timeAbilityPanel.add(timeline, BorderLayout.NORTH);
        this.mainPanel.add(timeAbilityPanel, BorderLayout.NORTH);
        initScrollPane();
        sessionMap.put(this.sessionId, this);
        addResizedListener();
    }

    /**
     * init Ability Pane
     *
     * @return JBPanel
     */
    private JBPanel initAbilityPane() {
        abilitySlice = new ProfilerAppAbility();
        // Save chart standard for timeline
        abilitySlice.setMaxDisplayTime(publisher.getStandard().getMaxDisplayMillis());
        abilitySlice.setMinMarkInterval(publisher.getStandard().getMinMarkInterval());
        abilitySlice.setLastTimestamp(publisher.getStandard().getLastTimestamp());
        abilitySlice.setName(UtConstant.UT_PROFILER_ABILITY_SLICE);
        // Create the observer for timeline and register to the current view
        AbilityObserver abilityObserver = new AbilityObserver(abilitySlice, sessionId, pause, stop);
        publisher.attach(abilityObserver);
        return abilitySlice;
    }

    /**
     * Add listener for component size change
     */
    private void addResizedListener() {
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                // Adjust the size and position of the scroll bar when the component size changes
                if (horizontalBar != null) {
                    horizontalBar.resizeAndReposition();
                    // Scroll bar move to refresh the ability Slice
                    int lastTime = (int) (publisher.getStandard().getLastTimestamp() - publisher.getStandard()
                        .getFirstTimestamp());
                    if (abilitySlice != null) {
                        abilitySlice.setLastTimestamp(lastTime);
                    }
                }
            }
        });
    }

    private void initTimeline() {
        timeline = new ProfilerTimeline(TIMELINE_WIDTH, TIMELINE_HEIGHT);
        // Save chart standard for timeline
        timeline.setMaxDisplayTime(publisher.getStandard().getMaxDisplayMillis());
        timeline.setMinMarkInterval(publisher.getStandard().getMinMarkInterval());
        // Create the observer for timeline and register to the current view
        TimelineObserver timelineObserver = new TimelineObserver(timeline);
        publisher.attach(timelineObserver);
    }

    private void initScrollPane() {
        this.itemsView = new ItemsView(this);
        itemsScroll = new JBScrollPane(itemsView, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
        itemsScroll.getVerticalScrollBar().setUnitIncrement(LayoutConstants.SCROLL_UNIT_INCREMENT);
        itemsScroll.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                itemsView.updateShowHeight(event.getComponent().getHeight());
            }
        });
        this.mainPanel.add(itemsScroll, BorderLayout.CENTER);
    }

    /**
     * Initialize horizontal scroll bar
     */
    public void initScrollbar() {
        this.horizontalBar = new ProfilerScrollbar(this);
        this.mainPanel.add(horizontalBar, BorderLayout.SOUTH);
        this.publisher.setDisplayScrollbar(true);
    }

    /**
     * Remove horizontal scroll bar
     */
    public void removeScrollbar() {
        this.publisher.setDisplayScrollbar(false);
        if (horizontalBar != null) {
            this.mainPanel.remove(horizontalBar);
            this.horizontalBar = null;
        }
    }

    /**
     * Show Loading panel
     */
    public void showLoading() {
        loadingPanel = new LoadingPanel();
        loading = true;
        this.remove(mainPanel);
        this.add(loadingPanel, BorderLayout.CENTER);
        // Check the loading result: start the refresh after loading
        publisher.checkLoadingResult();
    }

    /**
     * Hide Loading panel and show main panel
     */
    public void hideLoading() {
        if (loadingPanel != null) {
            this.remove(loadingPanel);
        }
        loading = false;
        this.add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Add a monitor item view
     *
     * @param item 指标项枚举类
     * @throws InvocationTargetException InvocationTargetException
     * @throws NoSuchMethodException NoSuchMethodException
     * @throws InstantiationException InstantiationException
     * @throws IllegalAccessException IllegalAccessException
     */
    public void addMonitorItemView(ProfilerMonitorItem item)
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        itemsView.addMonitorItemView(item);
    }

    /**
     * Set pause status
     *
     * @param pause pause/resume
     */
    public void setPause(boolean pause) {
        this.pause = pause;
        Icon icon;
        String text;
        if (pause) {
            icon = AllIcons.Process.ProgressResumeHover;
            text = "Start";
        } else {
            icon = AllIcons.Process.ProgressPauseHover;
            text = "Suspend";
            if (itemsView != null) {
                // Update the pop-up state of the table event after the left-click on all charts
                itemsView.updateShowTableView();
            }
        }

        if (taskScenePanelChart != null) {
            taskScenePanelChart.getjButtonStop().setIcon(icon);
            taskScenePanelChart.getjButtonStop().setToolTipText(text);
        }
    }

    public ProfilerChartsViewPublisher getPublisher() {
        return publisher;
    }

    public JBPanel getMainPanel() {
        return mainPanel;
    }

    public ProfilerScrollbar getHorizontalBar() {
        return horizontalBar;
    }

    public long getSessionId() {
        return sessionId;
    }

    public boolean isPause() {
        return pause;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public boolean isAddItemFlag() {
        return addItemFlag;
    }

    public void setAddItemFlag(boolean addItemFlag) {
        this.addItemFlag = addItemFlag;
    }

    public ProfilerTimeline getTimeline() {
        return timeline;
    }

    public TaskScenePanelChart getTaskScenePanelChart() {
        return taskScenePanelChart;
    }

    public boolean isLoading() {
        return loading;
    }

    public ItemsView getItemsView() {
        return itemsView;
    }

    public ProfilerAppAbility getAbilitySlice() {
        return abilitySlice;
    }

    public JBScrollPane getItemsScroll() {
        return itemsScroll;
    }
}
