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

package ohos.devtools.views.layout;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.datasources.utils.common.Constant;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.service.MultiDeviceManager;
import ohos.devtools.datasources.utils.plugin.entity.AnalysisType;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.process.service.ProcessManager;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.common.customcomp.CustomJLabel;
import ohos.devtools.views.common.customcomp.SelectedTextFileLister;
import ohos.devtools.views.layout.chartview.TaskScenePanelChart;
import ohos.devtools.views.layout.dialog.SampleDialog;
import ohos.devtools.views.layout.utils.EventTrackUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import static ohos.devtools.views.common.Constant.DEVICE_REFRESH;

/**
 * ApplicationConfigPanel
 *
 * @since : 2021/10/25
 */
public class ApplicationConfigPanel extends JBPanel implements MouseListener, ItemListener {
    private static final Logger LOGGER = LogManager.getLogger(ApplicationConfigPanel.class);
    private static final String SCENE_TITLE_STR = "Devices & Applications";
    private static final String SCENE_DES_STR = "Task scene: Application tuning";
    private static final String LAST_STEP_BTN_STR = "Last Step";
    private static final String START_TASK_BTN_STR = "Start Task";

    private TaskPanel taskPanel;
    private JBPanel northPanel;
    private JBLabel sceneTitlePanel;
    private JBLabel sceneDesPanel;
    private JBPanel centerPanel;
    private JBPanel southPanel;
    private JButton lastStepBtn;
    private JButton startTaskBtn;
    private DistributedDeviceProcessPanel deviceProcess;

    /**
     * TaskScenePanel
     *
     * @param taskPanel taskPanel
     */
    public ApplicationConfigPanel(TaskPanel taskPanel) {
        EventTrackUtils.getInstance().trackApplicationConfigPage();
        this.taskPanel = taskPanel;
        initComponents();
        addEventListener();
    }

    /**
     * initComponents
     */
    private void initComponents() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initComponents");
        }
        this.setLayout(new MigLayout("insets 0", "15[grow,fill]",
                "15[fill,fill]"));
        // init northPanel
        initNorthPanelItems();
        // init centerPanel
        initCenterPanelItems();
        // init southPanel
        initSouthPanelItems();
        // add the panel
        addPanels();
    }

    /**
     * initCenterPanelItems
     */
    private void initNorthPanelItems() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initNorthPanelItems");
        }
        northPanel = new JBPanel(new MigLayout("insets 0"));
        sceneTitlePanel = new JBLabel(SCENE_TITLE_STR);
        sceneTitlePanel.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
        sceneTitlePanel.setForeground(JBColor.foreground().brighter());
        sceneDesPanel = new JBLabel(SCENE_DES_STR);
        sceneDesPanel.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        northPanel.setOpaque(false);
        northPanel.add(sceneTitlePanel);
        northPanel.add(sceneDesPanel, "gap 5");
    }

    /**
     * initCenterPanelItems
     */
    private void initCenterPanelItems() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initCenterPanelItems");
        }
        centerPanel = new JBPanel(new MigLayout("insets 0", "[grow,fill]20",
            "[fill,fill]"));
        List<DeviceIPPortInfo> deviceInfoList = MultiDeviceManager.getInstance().getOnlineDeviceInfoList();
        if (deviceInfoList.isEmpty()) {
            deviceProcess = new DistributedDeviceProcessPanel(1);
        } else {
            List<ProcessInfo> processInfoList = ProcessManager.getInstance().getProcessList(deviceInfoList.get(0));
            if (processInfoList.isEmpty()) {
                deviceProcess = new DistributedDeviceProcessPanel(1);
            } else {
                deviceProcess = new DistributedDeviceProcessPanel(1, deviceInfoList.get(0), processInfoList.get(0));
            }
        }
        deviceProcess.setForeground(JBColor.foreground().brighter());
        centerPanel.setOpaque(true);
        deviceProcess.setForeground(JBColor.foreground().brighter());
        centerPanel.add(deviceProcess, "span");
    }

    /**
     * southPanelItems
     */
    private void initSouthPanelItems() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initSouthPanelItems");
        }
        southPanel = new JBPanel(new MigLayout("insets 0", "push[]20[]20",
            "[fill,fill]"));
        southPanel.setPreferredSize(new Dimension(1200, 40));
        lastStepBtn = new JButton(LAST_STEP_BTN_STR);
        lastStepBtn.setName(LAST_STEP_BTN_STR);
        lastStepBtn.setFont(new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.OPTION_FONT));
        lastStepBtn.setFocusPainted(false);
        lastStepBtn.setOpaque(false);
        lastStepBtn.setPreferredSize(new Dimension(140, 40));
        startTaskBtn = new JButton(START_TASK_BTN_STR);
        startTaskBtn.setName(UtConstant.UT_TASK_SCENE_PANE_START);
        startTaskBtn.setFont(new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.OPTION_FONT));
        startTaskBtn.setOpaque(false);
        startTaskBtn.setFocusPainted(false);
        startTaskBtn.setPreferredSize(new Dimension(140, 40));
        southPanel.setOpaque(false);
        southPanel.add(lastStepBtn);
        southPanel.add(startTaskBtn);
    }

    /**
     * add panels
     */
    private void addPanels() {
        this.add(northPanel, "wrap");
        this.add(centerPanel, "wrap");
        this.add(southPanel, "gaptop 100");
        this.setBackground(JBColor.background().darker());
        this.setOpaque(true);
    }

    /**
     * addEventListener
     */
    private void addEventListener() {
        lastStepBtn.addMouseListener(this);
        startTaskBtn.addMouseListener(this);
        deviceProcess.getSearchComBox().setSelectedTextFileListener(new SelectedTextFileLister() {
            @Override
            public void textFileClick() {
                DeviceIPPortInfo item = deviceProcess.getDeviceComboBox().getItem();
                ProcessManager instance = ProcessManager.getInstance();
                if (!instance.isRequestProcess()) {
                    new SwingWorker<List<String>, Integer>() {
                        @Override
                        protected List<String> doInBackground() {
                            List<ProcessInfo> processInfos = ProcessManager.getInstance().getProcessList(item);
                            List<String> processNames = new ArrayList<>();
                            for (int index = 0; index < processInfos.size(); index++) {
                                ProcessInfo processInfo = processInfos.get(index);
                                processNames
                                    .add(processInfo.getProcessName() + "(" + processInfo.getProcessId() + ")");
                            }
                            return processNames;
                        }

                        @Override
                        protected void done() {
                            try {
                                List<String> vector = get();
                                deviceProcess.getSearchComBox().refreshProcess(vector);
                            } catch (InterruptedException | ExecutionException exception) {
                                exception.printStackTrace();
                            }
                        }
                    }.execute();
                }
            }
        });
        deviceProcess.getDeviceComboBox().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                deviceProcess.getSearchComBox().clearSelectedName();
            }
        });
        addRefreshDeviceList();
    }

    /**
     * obtainMap
     *
     * @param taskPanel taskPanel
     * @param deviceInfo deviceInfo
     * @param processName processName
     * @return List <HosJLabel>
     */
    public List<CustomJLabel> startCollecting(TaskPanel taskPanel, DeviceIPPortInfo deviceInfo, String processName) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("startCollecting");
        }
        ArrayList<CustomJLabel> hosJLabels = new ArrayList<>();
        SessionManager sessionManager = SessionManager.getInstance();
        String pid = processName.substring(processName.lastIndexOf("(") + 1, processName.lastIndexOf(")"));
        ProcessInfo process = new ProcessInfo();
        process.setProcessName(processName.substring(0, processName.lastIndexOf("(")));
        process.setProcessId(Integer.parseInt(pid));
        Long localSessionID = sessionManager.createSession(deviceInfo, process, AnalysisType.APPLICATION_TYPE);
        if (localSessionID.equals(Constant.ABNORMAL)) {
            return new ArrayList<>();
        }
        taskPanel.setLocalSessionId(localSessionID);
        CustomJLabel hosJLabel = new CustomJLabel();
        hosJLabel.setSessionId(localSessionID);
        hosJLabel.setDeviceName(deviceInfo.getDeviceName());
        hosJLabel.setProcessName(processName);
        hosJLabel.setConnectType(deviceInfo.getConnectType());
        // start session
        sessionManager.startSession(localSessionID, false);
        // get the data
        sessionManager.fetchData(localSessionID);
        hosJLabels.add(hosJLabel);
        return hosJLabels;
    }

    /**
     * addRefreshDeviceList
     */
    private void addRefreshDeviceList() {
        QuartzManager.getInstance().addExecutor(DEVICE_REFRESH, new Runnable() {
            @Override
            public void run() {
                List<DeviceIPPortInfo> deviceInfos = MultiDeviceManager.getInstance().getOnlineDeviceInfoList();
                if (!deviceInfos.isEmpty()) {
                    Vector<DeviceIPPortInfo> items = new Vector<DeviceIPPortInfo>();
                    for (DeviceIPPortInfo deviceIPPortInfo : deviceInfos) {
                        items.add(deviceIPPortInfo);
                    }
                    Vector<DeviceIPPortInfo> oldDevice = deviceProcess.getVector();
                    if (!compareVector(oldDevice, items)) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                deviceProcess.refreshDeviceItem(items);
                            }
                        });
                    }
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Vector<DeviceIPPortInfo> items = new Vector<>();
                            deviceProcess.refreshDeviceItem(items);
                            deviceProcess.getSearchComBox().clearSelectedName();
                        }
                    });
                }
            }
        });
        QuartzManager.getInstance().startExecutor(DEVICE_REFRESH, 0, LayoutConstants.THOUSAND);
    }

    /**
     * compareVector
     *
     * @param first first
     * @param second second
     * @return boolean
     */
    private boolean compareVector(Vector<DeviceIPPortInfo> first, Vector<DeviceIPPortInfo> second) {
        if (Objects.isNull(first) && Objects.isNull(second)) {
            return true;
        }
        if (Objects.isNull(first) || Objects.isNull(second)) {
            return false;
        }
        if (first.size() == second.size()) {
            return compareWithVector(first, second);
        } else {
            return false;
        }
    }

    /**
     * compareWithVector
     *
     * @param oldVector oldVector
     * @param newVector newVector
     * @return boolean
     */
    private boolean compareWithVector(Vector<DeviceIPPortInfo> oldVector, Vector<DeviceIPPortInfo> newVector) {
        StringBuilder builder = new StringBuilder();
        Iterator<DeviceIPPortInfo> iterator = oldVector.iterator();
        while (iterator.hasNext()) {
            DeviceIPPortInfo deviceIPPortInfo = iterator.next();
            builder.append(deviceIPPortInfo.getDeviceName()).append("_").append(deviceIPPortInfo.getDeviceID());
        }
        int count = 0;
        String firstString = builder.toString();
        Iterator<DeviceIPPortInfo> newIterator = newVector.iterator();
        while (newIterator.hasNext()) {
            DeviceIPPortInfo deviceIPPortInfo = newIterator.next();
            String map1KeyVal = deviceIPPortInfo.getDeviceName() + "_" + deviceIPPortInfo.getDeviceID();
            boolean contains = firstString.contains(map1KeyVal);
            if (contains) {
                count++;
            }
        }
        if (newVector.size() == count) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        String name = event.getComponent().getName();
        if (name.equals(LAST_STEP_BTN_STR)) {
            taskPanel.getTabContainer().remove(this);
            taskPanel.getTabItem().setVisible(true);
            taskPanel.getTabContainer().repaint();
            QuartzManager.getInstance().deleteExecutor(DEVICE_REFRESH);
        }
        if (name.equals(START_TASK_BTN_STR)) {
            startTaskBtn.dispatchEvent(new FocusEvent(startTaskBtn, FocusEvent.FOCUS_GAINED, true));
            startTaskBtn.requestFocusInWindow();
            DeviceIPPortInfo item = deviceProcess.getDeviceComboBox().getItem();
            if (Objects.isNull(item)) {
                new SampleDialog("prompt", "Device list is empty !").show();
                return;
            }
            String processName = deviceProcess.getSearchComBox().getSelectedProcessName();
            if (StringUtils.isBlank(processName)) {
                new SampleDialog("prompt", "The selection process cannot be empty !").show();
                return;
            }
            // get the process map
            List<CustomJLabel> hosJLabels = startCollecting(taskPanel, item, processName);
            if (!hosJLabels.isEmpty()) {
                taskPanel.getTabContainer().removeAll();
                QuartzManager.getInstance().deleteExecutor(DEVICE_REFRESH);
                taskPanel.getTabContainer().add(new TaskScenePanelChart(taskPanel, hosJLabels));
                taskPanel.getTabContainer().setOpaque(true);
                taskPanel.getTabContainer().setBackground(JBColor.background());
                taskPanel.getTabContainer().repaint();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent event) {
    }

    @Override
    public void mouseReleased(MouseEvent event) {
    }

    @Override
    public void mouseEntered(MouseEvent event) {
    }

    @Override
    public void mouseExited(MouseEvent event) {
    }

    @Override
    public void itemStateChanged(ItemEvent event) {
    }
}
