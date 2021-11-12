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
import com.intellij.ui.components.JBScrollPane;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.service.MultiDeviceManager;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.process.service.ProcessManager;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.services.distribute.DistributeDevice;
import ohos.devtools.services.distribute.DistributedManager;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.customcomp.SelectedTextFileLister;
import ohos.devtools.views.layout.dialog.SampleDialog;
import ohos.devtools.views.layout.dialog.TraceRecordDialog;
import ohos.devtools.views.layout.utils.EventTrackUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import static ohos.devtools.views.common.Constant.DISTRIBUTED_REFRESH;

/**
 * DistributedConfigPanel
 *
 * @since : 2021/10/25
 */
public class DistributedConfigPanel extends JBPanel {
    private static final Logger LOGGER = LogManager.getLogger(DistributedConfigPanel.class);
    private static final String LAST_STEP_BTN = "Last Step";
    private static final String START_TASK_BTN = "Start Task";

    private DistributedDeviceProcessPanel firstDevice;
    private DistributedDeviceProcessPanel secondDevice;
    private JBPanel buttonPanel;
    private JButton lastStepBtn;
    private JButton startTaskBtn;
    private TaskPanel contentPanel;

    /**
     * DistributedConfigPanel  constructor
     *
     * @param taskPanel taskPanel
     */
    public DistributedConfigPanel(TaskPanel taskPanel) {
        EventTrackUtils.getInstance().trackDistributedConfig();
        contentPanel = taskPanel;
        this.setLayout(new MigLayout("inset 0", "15[grow,fill]", "15[fill,fill]"));
        this.setOpaque(true);
        this.setBackground(JBColor.background().darker());
        initComponents();
        addEventListener();
    }

    /**
     * initComponents
     */
    private void initComponents() {
        initDeviceDescriptionPage();
        initDevicePanel();
        initButtonPanel();
    }

    /**
     * initDeviceDescriptionPage
     */
    private void initDeviceDescriptionPage() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initDeviceDescriptionPage");
        }
        JBPanel deviceDescription = new JBPanel(new MigLayout("insets 10", "[]20[]push"));
        deviceDescription.setBackground(JBColor.background().darker());
        JBLabel deviceLabel = new JBLabel("Devices");
        deviceLabel.setOpaque(false);
        deviceLabel.setFont(new Font("PingFang SC", Font.PLAIN, 24));
        deviceLabel.setForeground(JBColor.foreground().brighter());
        JBLabel deviceDescriptionLabel = new JBLabel("Task scene: Distribute tuning");
        deviceDescriptionLabel.setOpaque(false);
        deviceDescriptionLabel.setFont(new Font("PingFang SC", Font.PLAIN, 14));
        deviceDescription.add(deviceLabel);
        deviceDescription.add(deviceDescriptionLabel);
        this.add(deviceDescription, "wrap");
    }

    /**
     * initDevicePanel
     */
    private void initDevicePanel() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initDevicePanel");
        }
        JBPanel devicePanel = new JBPanel(new MigLayout("insets 0", "[grow,fill]"));
        firstDevice = new DistributedDeviceProcessPanel(1);
        devicePanel.add(firstDevice, "wrap");
        devicePanel.add(new JSeparator(), "wrap");
        secondDevice = new DistributedDeviceProcessPanel(2);
        devicePanel.add(secondDevice, "wrap");
        devicePanel.add(new JSeparator(), "wrap");
        JBScrollPane scrollPane = new JBScrollPane(devicePanel);
        this.add(scrollPane, "wrap");
    }

    /**
     * initButtonPanel
     */
    private void initButtonPanel() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initButtonPanel");
        }
        buttonPanel = new JBPanel(new MigLayout("insets 0", "push[]20[]20", "20[fill,fill]"));
        buttonPanel.setOpaque(false);
        lastStepBtn = new JButton(LAST_STEP_BTN);
        lastStepBtn.setName(LAST_STEP_BTN);
        lastStepBtn.setFocusPainted(false);
        lastStepBtn.setOpaque(false);
        lastStepBtn.setPreferredSize(new Dimension(140, 40));
        startTaskBtn = new JButton(START_TASK_BTN);
        startTaskBtn.setName(START_TASK_BTN);
        startTaskBtn.setFocusPainted(false);
        startTaskBtn.setOpaque(false);
        startTaskBtn.setPreferredSize(new Dimension(140, 40));
        buttonPanel.add(lastStepBtn);
        buttonPanel.add(startTaskBtn);
        this.add(buttonPanel, "wrap, span");
    }

    /**
     * addEventListener
     */
    private void addEventListener() {
        addRefreshDeviceList();
        addLastStepListener();
        addStartTaskListener();
        addFirstDeviceListener();
        addSecondDeviceListener();
    }

    /**
     * addLastStepListener
     */
    private void addLastStepListener() {
        lastStepBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                contentPanel.getTabContainer().remove(DistributedConfigPanel.this);
                contentPanel.getTabItem().setVisible(true);
                contentPanel.getTabContainer().repaint();
                QuartzManager.getInstance().deleteExecutor(DISTRIBUTED_REFRESH);
            }
        });
    }

    /**
     * addStartTaskListener
     */
    private void addStartTaskListener() {
        startTaskBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                DeviceIPPortInfo item = firstDevice.getDeviceComboBox().getItem();
                DeviceIPPortInfo deviceIPPortInfo = secondDevice.getDeviceComboBox().getItem();
                if (Objects.isNull(item) || Objects.isNull(deviceIPPortInfo)) {
                    new SampleDialog("prompt", "Device list is empty !").show();
                    return;
                }
                if (StringUtils.equals(item.getDeviceID(), deviceIPPortInfo.getDeviceID())) {
                    new SampleDialog("prompt", "The selection devices cannot be the same !").show();
                    return;
                }
                String firstProcess = firstDevice.getSearchComBox().getSelectedProcessName();
                String secondProcess = secondDevice.getSearchComBox().getSelectedProcessName();
                if (StringUtils.isBlank(firstProcess) || StringUtils.isBlank(secondProcess)) {
                    new SampleDialog("prompt", "The selection process cannot be empty !").show();
                    return;
                }
                DistributedManager distributedManager = new DistributedManager(new DistributeDevice(item, firstProcess),
                    new DistributeDevice(deviceIPPortInfo, secondProcess));
                boolean collectSuccess = distributedManager.startCollecting();
                if (collectSuccess) {
                    new TraceRecordDialog().load(contentPanel, distributedManager, true);
                } else {
                    new SampleDialog("prompt", "Collection failed, please wait 10s then try again !").show();
                }
            }
        });
    }

    /**
     * addSecondDeviceListener
     */
    private void addSecondDeviceListener() {
        if (secondDevice != null) {
            secondDevice.getSearchComBox().setSelectedTextFileListener(new SelectedTextFileLister() {
                @Override
                public void textFileClick() {
                    DeviceIPPortInfo item = secondDevice.getDeviceComboBox().getItem();
                    ProcessManager instance = ProcessManager.getInstance();
                    if (!instance.isRequestProcess()) {
                        new SwingWorker<List<String>, Integer>() {
                            @Override
                            protected List<String> doInBackground() {
                                LOGGER.info("start Process");
                                List<ProcessInfo> processInfos = ProcessManager.getInstance().getProcessList(item);
                                LOGGER.info("Process end");

                                List<String> processNames = new ArrayList<>();
                                for (int index = 0; index < processInfos.size(); index++) {
                                    ProcessInfo processInfo = processInfos.get(index);
                                    processNames
                                        .add(processInfo.getProcessName() + "(" + processInfo.getProcessId() + ")");
                                }
                                LOGGER.info("processNames handle end");
                                return processNames;
                            }

                            @Override
                            protected void done() {
                                try {
                                    LOGGER.info("getVector");
                                    List<String> vector = get();
                                    LOGGER.info("getVector end");
                                    secondDevice.getSearchComBox().refreshProcess(vector);
                                    LOGGER.info(" refreshProcess end");
                                } catch (InterruptedException | ExecutionException exception) {
                                    exception.printStackTrace();
                                }
                            }
                        }.execute();
                    }
                }
            });
            secondDevice.getDeviceComboBox().addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent itemEvent) {
                    secondDevice.getSearchComBox().clearSelectedName();
                }
            });
        }
    }

    /**
     * addFirstDeviceListener
     */
    private void addFirstDeviceListener() {
        if (firstDevice != null) {
            firstDevice.getSearchComBox().setSelectedTextFileListener(new SelectedTextFileLister() {
                @Override
                public void textFileClick() {
                    DeviceIPPortInfo item = firstDevice.getDeviceComboBox().getItem();
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
                                    firstDevice.getSearchComBox().refreshProcess(vector);
                                } catch (InterruptedException | ExecutionException exception) {
                                    exception.printStackTrace();
                                }
                            }
                        }.execute();
                    }
                }
            });
            firstDevice.getDeviceComboBox().addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent itemEvent) {
                    firstDevice.getSearchComBox().clearSelectedName();
                }
            });
        }
    }

    /**
     * addRefreshDeviceList
     */
    private void addRefreshDeviceList() {
        QuartzManager.getInstance().addExecutor(DISTRIBUTED_REFRESH, new Runnable() {
            @Override
            public void run() {
                List<DeviceIPPortInfo> deviceInfos = MultiDeviceManager.getInstance().getOnlineDeviceInfoList();
                if (!deviceInfos.isEmpty()) {
                    Vector<DeviceIPPortInfo> items = new Vector<DeviceIPPortInfo>();
                    for (DeviceIPPortInfo deviceIPPortInfo : deviceInfos) {
                        items.add(deviceIPPortInfo);
                    }
                    Vector<DeviceIPPortInfo> oldDevice = firstDevice.getVector();
                    if (!compareVector(oldDevice, items)) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                firstDevice.refreshDeviceItem(items);
                                secondDevice.refreshDeviceItem(vectorReverse(items));
                            }
                        });
                    }
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Vector<DeviceIPPortInfo> items = new Vector<>();
                            firstDevice.refreshDeviceItem(items);
                            secondDevice.refreshDeviceItem(items);
                            firstDevice.getSearchComBox().clearSelectedName();
                            secondDevice.getSearchComBox().clearSelectedName();
                        }
                    });
                }
            }
        });
        QuartzManager.getInstance().startExecutor(DISTRIBUTED_REFRESH, 0, LayoutConstants.THOUSAND);
    }

    /**
     * vectorReverse
     *
     * @param vector oldVector
     * @return new vector
     */
    private Vector<DeviceIPPortInfo> vectorReverse(Vector<DeviceIPPortInfo> vector) {
        Vector<DeviceIPPortInfo> deviceIPPortInfos = new Vector<>();
        for (int index = (vector.size() - 1); index >= 0; index--) {
            DeviceIPPortInfo deviceIPPortInfo = vector.get(index);
            deviceIPPortInfos.add(deviceIPPortInfo);
        }
        return deviceIPPortInfos;
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
}
