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

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTabbedPane;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.datasources.transport.grpc.SystemTraceHelper;
import ohos.devtools.datasources.utils.common.GrpcException;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.service.MultiDeviceManager;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.dialog.SampleDialog;
import ohos.devtools.views.layout.dialog.TraceRecordDialog;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import static ohos.devtools.views.common.Constant.DEVICE_REFRESH;

/**
 * SystemConfigPanel
 */
public class SystemConfigPanel extends JBPanel implements MouseListener, ItemListener {
    private static final String HTRACE_AUDIO_STR = "Audio";
    private static final String HTRACE_CAMERA_STR = "Camera";
    private static final String HTRACE_DATABASE_STR = "Database";
    private static final String HTRACE_GRAPHICS_STR = "Graphics";
    private static final String HTRACE_INPUT_STR = "Input";
    private static final String HTRACE_NETWORK_STR = "NetWork";
    private static final String HTRACE_VIDEO_STR = "Video";
    private static final String LAST_STEP_BTN = "Last Step";
    private static final String START_TASK_BTN = "Start Task";
    private static final String SCENE_TITLE_STR = "Device";
    private static final String SCENE_TITLE_DES_STR = "Task scene: Connected devices";
    private static final String CONFIG_TITLE_STR = "Trace config & Probes";
    private static final String CONFIG_TITLE_DES_STR = "Task scene: System tuning";
    private static final String TRACE_CONFIG_TITLE_STR = "Trace config";
    private static final String PROBES_TITLE_STR = "Probes";
    private static final String RECORD_MODEL_STR = "Record Mode";
    private static final String SCHEDULING_STR = "Scheduling details";
    private static final String SCHEDULING_DES_STR = "Enables high-detailed tracking of scheduling events";
    private static final String CPU_FREQUENCY_STR = "CPU Frequency and idle states";
    private static final String CPU_FREQUENCY_DES_STR = "Records cpu frequency and idle state change viaftrace";
    private static final String BOARD_STR = "Board voltages & frequency";
    private static final String BOARD_DES_STR = "Tracks voltage and frequency changes from board sensors";
    private static final String TRACE_CONFIG_MODE_STR = "Record Mode";
    private static final String TRACE_CONFIG_MODE_BUTTON_STR = "Stop when full";
    private static final String BUFFER_SIZE_TITLE_STR = "In-memory buffer size:";
    private static final String DURATION_TITLE_STR = "Max duration:";
    private static final String HIGH_FREQUENCY_STR = "High frequency memory";
    private static final String HIGH_FREQUENCY_DES_STR = "<html>Allows to track short memory splikes and transitories "
        + "through ftrace's mm_event. rss_stat and ion events."
        + " Available only on recent Kernel version >= 4.19"
        + "</html>";
    private static final String HTRACE_USERSPACE_STR = "Bytrace categories";
    private static final String HTRACE_USERSPACE_DES_STR = "<html>" + "Enables C++ / Java codebase annotations "
        + "(HTRACE_BEGIN() / os.Trace())" + "</html>";
    private static final String SYSCALLS_STR = "Syscalls";
    private static final String SYSCALLS_DES_STR = "Tracks the enter and exit of all syscalls";
    private static final String LOW_MEMORY_STR = "Low memory killer";
    private static final String LOW_MEMORY_DES_STR = "<html>"
        + "Record LMK events. Works both with the old in kernel LMK and"
        + "the newer userspace Imkd. It also tracks OOM score adjustments " +
        "</html>";
    private static final String RECORD_SETTING_STR = "<html>" +
        "<p style=\"margin-left:28px;font-size:13px;text-align:left;color:white;\">Record Setting</p>" +
        "<p style=\"margin-top:0px;margin-left:28px;font-size:9px;text-align:left;color:#757784;\">" +
        "Buffer mode.size and duration</p>" +
        "</html>";
    private static final String PROBES_CPU_STR = "<html>" +
        "<p style=\"font-size:13px;text-align:left;color:white;\">probes config</p>" +
        "<p style=\"margin-top:0px;font-size:9px;text-align:left;color:#757784;\">CPU usage,scheduling" +
        "<br>wakeups</p>" +
        "</html>";

    JSeparator separator = new JSeparator();

    ArrayList<String> schedulingEvents = new ArrayList<String>(Arrays.asList(
        "sched/sched_switch",
        "power/suspend_resume",
        "sched/sched_wakeup",
        "sched/sched_wakeup_new",
        "sched/sched_waking",
        "sched/sched_process_exit",
        "sched/sched_process_free",
        "task/task_newtask",
        "task/task_rename"));
    ArrayList<String> powerEvents = new ArrayList<String>(Arrays.asList(
        "regulator/regulator_set_voltage",
        "regulator/regulator_set_voltage_complete",
        "power/clock_enable",
        "power/clock_disable",
        "power/clock_set_rate",
        "power/suspend_resume"));

    ArrayList<String> cpuFreqEvents = new ArrayList<String>(Arrays.asList(
        "power/cpu_frequency",
        "power/cpu_idle",
        "power/suspend_resume"
    ));
    ArrayList<String> sysCallsEvents = new ArrayList<String>(Arrays.asList(
        "raw_syscalls/sys_enter",
        "raw_syscalls/sys_exit"
    ));
    ArrayList<String> highFrequencyEvents = new ArrayList<String>(Arrays.asList(
        "mm_event/mm_event_record",
        "kmem/rss_stat",
        "ion/ion_stat",
        "dmabuf_heap/dma_heap_stat",
        "kmem/ion_heap_grow",
        "kmem/ion_heap_shrink"
    ));
    ArrayList<String> hTraceAudioEvents = new ArrayList<String>(Arrays.asList(
        "audio"
    ));
    ArrayList<String> hTraceCameraEvents = new ArrayList<String>(Arrays.asList(
        "camera"
    ));
    ArrayList<String> hTraceDatabaseEvents = new ArrayList<String>(Arrays.asList(
        "database"
    ));
    ArrayList<String> hTraceGraphicsEvents = new ArrayList<String>(Arrays.asList(
        "gfx"
    ));
    ArrayList<String> hTraceInputEvents = new ArrayList<String>(Arrays.asList(
        "input"
    ));
    ArrayList<String> hTraceNetWorkEvents = new ArrayList<String>(Arrays.asList(
        "network"
    ));
    ArrayList<String> hTraceVideoEvents = new ArrayList<String>(Arrays.asList(
        "video"
    ));
    private JBCheckBox hTraceAudio;
    private JBCheckBox hTraceCamera;
    private JBCheckBox hTraceDatabase;
    private JBCheckBox hTraceGraphics;
    private JBCheckBox hTraceInput;
    private JBCheckBox hTraceNetWork;
    private JBCheckBox hTraceVideo;
    private TaskPanel contentPanel;
    private JBPanel sceneTitlePanel;
    private JBLabel sceneTitle;
    private JBLabel sceneTitleDes;
    private ComboBox<String> connectTypeComboBox;
    private ComboBox<String> deviceComboBox;
    private JBPanel configTitlePanel;
    private JBLabel configTitle;
    private JBLabel configTitleDes;
    private JBPanel traceConfigTab;
    private JBLabel traceConfigTitle;
    private JBPanel probesTab;
    private JBLabel probesTitle;
    private JBTabbedPane configTabbedPane;
    private JBPanel traceConfigWestPanel;
    private JBPanel traceConfigCenterPanel;
    private JBLabel recordSettingLabel;
    private JBPanel probesWestPanel;
    private JBPanel probesCenterPanel;
    private JBLabel probesCpu;
    private JButton lastStepBtn;
    private JButton startTaskBtn;
    private JBLabel recordModelTitle;
    private JBCheckBox schedulingCheckBox;
    private JBLabel schedulingCheckBoxDes;
    private JBCheckBox cpuFrequencyCheckBox;
    private JBLabel cpuFrequencyCheckBoxDes;
    private JBCheckBox boardCheckBox;
    private JBLabel boardCheckBoxDes;
    private JBCheckBox highFrequencyCheckBox;
    private JBLabel highFrequencyCheckBoxDes;
    private JBCheckBox hTraceUserspaceCheckBox;
    private JBLabel hTraceUserspaceCheckBoxDes;
    private JBCheckBox syscallsCheckBox;
    private JBLabel syscallsCheckBoxDes;
    private JBPanel buttonPanel;
    private int inMemoryValue = 10;
    private int maxDuration = 10;
    private String eventStr = "";
    private boolean chooseMode = true;
    private List<DeviceIPPortInfo> deviceInfoList = null;
    private DeviceIPPortInfo deviceIPPortInfo = null;
    private Vector<String> deviceInfo = new Vector<>();

    /**
     * SystemConfigPanel
     *
     * @param taskPanel taskPanel
     */
    public SystemConfigPanel(TaskPanel taskPanel) {
        contentPanel = taskPanel;
        setLayout(new MigLayout("inset 0", "15[grow,fill]", "15[fill,fill]"));
        setOpaque(true);
        setBackground(JBColor.background().darker());
        initComponents();
        addEventListener();
    }

    /**
     * initComponents
     */
    private void initComponents() {
        // init the sceneTitlePanel
        initSceneTitlePanel();
        // init the device ComboBox
        initDeviceComboBox();
        // init the configTitlePanel
        initConfigTitlePanel();
        // init the ConfigTabbedPane
        initConfigTabbedPane();
        // init the buttonPanel
        initButtonPanel();
    }

    private void initSceneTitlePanel() {
        sceneTitlePanel = new JBPanel(new MigLayout("insets 0",
            "[]15[]push", "[fill,fill]"));
        sceneTitlePanel.setOpaque(false);
        sceneTitle = new JBLabel(SCENE_TITLE_STR);
        sceneTitle.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
        sceneTitle.setForeground(JBColor.foreground().brighter());
        sceneTitleDes = new JBLabel(SCENE_TITLE_DES_STR);
        sceneTitleDes.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        sceneTitlePanel.add(sceneTitle);
        sceneTitlePanel.add(sceneTitleDes);
        this.add(sceneTitlePanel, "wrap, span");
    }

    private void initDeviceComboBox() {
        connectTypeComboBox = new ComboBox<String>();
        connectTypeComboBox.addItem(LayoutConstants.USB);
        deviceComboBox = new ComboBox<String>();
        this.add(connectTypeComboBox, "width 20%");
        this.add(deviceComboBox, "wrap, width 50%");
    }

    private void initConfigTitlePanel() {
        configTitlePanel = new JBPanel(new MigLayout("insets 0",
            "[]15[]push", "15[fill,fill]"));
        configTitlePanel.setOpaque(false);
        configTitle = new JBLabel(CONFIG_TITLE_STR);
        configTitle.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
        configTitle.setForeground(JBColor.foreground().brighter());
        configTitleDes = new JBLabel(CONFIG_TITLE_DES_STR);
        configTitleDes.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        configTitlePanel.add(configTitle);
        configTitlePanel.add(configTitleDes);
        this.add(configTitlePanel, "wrap, span");
    }

    private void initConfigTabbedPane() {
        // initTabPanel
        initTabPanel();
        // initTraceConfigTabItems
        initTraceConfigTabItems();
        // initProbesTabItems
        initProbesTabItems();
        // initProbesHtraceTabItem
        initProbesHTraceTabItem();
    }

    private void initTabPanel() {
        // init the traceConfigTab
        traceConfigTab = new JBPanel(new BorderLayout());
        traceConfigTitle = new JBLabel(TRACE_CONFIG_TITLE_STR, JBLabel.CENTER);
        traceConfigTitle.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
        traceConfigTitle.setPreferredSize(new Dimension(100, 40));
        traceConfigTab.add(traceConfigTitle);
        // init the probesTab
        probesTab = new JBPanel(new BorderLayout());
        probesTitle = new JBLabel(PROBES_TITLE_STR, JBLabel.CENTER);
        probesTitle.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
        probesTitle.setPreferredSize(new Dimension(100, 40));
        // init the configTabbedPane
        configTabbedPane = new JBTabbedPane();
        configTabbedPane.addTab("", traceConfigTab);
        configTabbedPane.addTab("", probesTab);
        configTabbedPane.setTabComponentAt(configTabbedPane.indexOfComponent(traceConfigTab), traceConfigTitle);
        configTabbedPane.setTabComponentAt(configTabbedPane.indexOfComponent(probesTab), probesTitle);
        this.add(configTabbedPane, "wrap, span");
    }

    private void initTraceConfigTabItems() {
        traceConfigWestPanel = new JBPanel(null);
        traceConfigCenterPanel = new JBPanel(null);
        recordSettingLabel = new JBLabel(RECORD_SETTING_STR, JBLabel.CENTER);
        recordSettingLabel.setOpaque(true);
        recordSettingLabel.setBorder(BorderFactory.createLineBorder(new Color(0, 117, 255), 1));
        recordSettingLabel.setBounds(0, 50, 200, 70);
        recordSettingLabel.setVisible(false);
        // init trace config items
        JBLabel traceConfigRecordMode = new JBLabel(TRACE_CONFIG_MODE_STR);
        JBRadioButton traceConfigModeButton = new JBRadioButton(TRACE_CONFIG_MODE_BUTTON_STR, true);
        traceConfigRecordMode.setBounds(50, 13, 200, 50);
        traceConfigModeButton.setBounds(50, 50, 160, 50);
        traceConfigCenterPanel.add(traceConfigRecordMode);
        traceConfigCenterPanel.add(traceConfigModeButton);
        // bufferSizeTitle
        JBLabel bufferSizeTitle = new JBLabel(BUFFER_SIZE_TITLE_STR);
        bufferSizeTitle.setBounds(50, 100, 200, 50);
        traceConfigCenterPanel.add(bufferSizeTitle);
        // bufferSizeValue
        JBLabel bufferSizeValue = getBufferSizeValueLabel();
        traceConfigCenterPanel.add(bufferSizeValue);
        // bufferSizeSlider
        JSlider bufferSizeSlider = new JSlider(0, 110);
        bufferSizeSlider.setValue(10);
        bufferSizeSlider.setPreferredSize(new Dimension(200, 0));
        bufferSizeSlider.setBounds(45, 140, 600, 30);
        traceConfigCenterPanel.add(bufferSizeSlider);
        // durationTitle
        JBLabel durationTitle = new JBLabel(DURATION_TITLE_STR);
        durationTitle.setBounds(50, 190, 200, 50);
        traceConfigCenterPanel.add(durationTitle);
        // durationValue
        JBLabel durationValue = getDurationValueLabel();
        traceConfigCenterPanel.add(durationValue);
        // durationSlider
        JSlider durationSlider = new JSlider(10, 600);
        durationSlider.setValue(10);
        durationSlider.setPreferredSize(new Dimension(200, 0));
        durationSlider.setBounds(45, 230, 600, 30);
        traceConfigCenterPanel.add(durationSlider);
        traceConfigWestPanel.add(recordSettingLabel);
        traceConfigWestPanel.setPreferredSize(new Dimension(200, 500));
        traceConfigTab.add(traceConfigWestPanel, BorderLayout.WEST);
        traceConfigTab.add(traceConfigCenterPanel, BorderLayout.CENTER);
        // bufferSizeSlider addChangeListener
        addBufferSizeListener(bufferSizeValue, bufferSizeSlider);
        // durationSlider addChangeListener
        addDurationSilderListener(durationValue, durationSlider);
    }

    @NotNull
    private JBLabel getBufferSizeValueLabel() {
        JBLabel bufferSizeValue = new JBLabel("" + 10 + " MB", JBLabel.CENTER);
        bufferSizeValue.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        bufferSizeValue.setVerticalTextPosition(JBLabel.CENTER);
        bufferSizeValue.setHorizontalTextPosition(JBLabel.CENTER);
        bufferSizeValue.setOpaque(true);
        bufferSizeValue.setBounds(700, 135, 110, 45);
        return bufferSizeValue;
    }

    @NotNull
    private JBLabel getDurationValueLabel() {
        JBLabel durationValue = new JBLabel("00:00:" + 10 + " h:m:s ", JBLabel.CENTER);
        durationValue.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        durationValue.setVerticalTextPosition(JBLabel.CENTER);
        durationValue.setHorizontalTextPosition(JBLabel.CENTER);
        durationValue.setOpaque(true);
        durationValue.setBounds(700, 225, 110, 45);
        return durationValue;
    }

    private void addDurationSilderListener(JBLabel durationValue, JSlider durationSlider) {
        durationSlider.addChangeListener(new ChangeListener() {
            /**
             * stateChanged
             *
             * @param changeEvent changeEvent
             */
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                int seconds = durationSlider.getValue() % 60;
                int minutes = (durationSlider.getValue() / 60) % 60;
                int hours = durationSlider.getValue() / (60 * 60);
                durationValue.setText(" " + String.format(Locale.ENGLISH, "%02d", hours) + ":" +
                    String.format(Locale.ENGLISH, "%02d", minutes) +
                    ":" + String.format(Locale.ENGLISH, "%02d", seconds)
                    + " h:m:s ");
                durationValue.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                durationValue.setVerticalTextPosition(JBLabel.CENTER);
                durationValue.setHorizontalTextPosition(JBLabel.CENTER);
                maxDuration = durationSlider.getValue();
            }
        });
    }

    private void addBufferSizeListener(JBLabel bufferSizeValue, JSlider bufferSizeSlider) {
        bufferSizeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                bufferSizeValue.setText("" + bufferSizeSlider.getValue() + " MB");
                bufferSizeValue.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                bufferSizeValue.setVerticalTextPosition(JBLabel.CENTER);
                bufferSizeValue.setHorizontalTextPosition(JBLabel.CENTER);
                inMemoryValue = bufferSizeSlider.getValue();
            }
        });
    }

    private void initProbesHTraceTabItem() {
        hTraceUserspaceCheckBox = new JBCheckBox(HTRACE_USERSPACE_STR, false);
        hTraceUserspaceCheckBoxDes = new JBLabel(HTRACE_USERSPACE_DES_STR, JBLabel.LEFT);
        hTraceUserspaceCheckBox.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        hTraceUserspaceCheckBox.setBounds(50, 330, 250, 50);
        hTraceUserspaceCheckBoxDes.setBounds(70, 360, 350, 80);
        hTraceAudio = new JBCheckBox(HTRACE_AUDIO_STR, false);
        hTraceCamera = new JBCheckBox(HTRACE_CAMERA_STR, false);
        hTraceDatabase = new JBCheckBox(HTRACE_DATABASE_STR, false);
        hTraceGraphics = new JBCheckBox(HTRACE_GRAPHICS_STR, false);
        hTraceInput = new JBCheckBox(HTRACE_INPUT_STR, false);
        hTraceNetWork = new JBCheckBox(HTRACE_NETWORK_STR, false);
        hTraceVideo = new JBCheckBox(HTRACE_VIDEO_STR, false);
        hTraceAudio.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        hTraceAudio.setBounds(500, 330, 90, 50);
        hTraceCamera.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        hTraceCamera.setBounds(590, 330, 90, 50);
        hTraceDatabase.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        hTraceDatabase.setBounds(680, 330, 90, 50);
        hTraceGraphics.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        hTraceGraphics.setBounds(770, 330, 90, 50);
        hTraceInput.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        hTraceInput.setBounds(500, 380, 90, 50);
        hTraceNetWork.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        hTraceNetWork.setBounds(590, 380, 90, 50);
        hTraceVideo.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        hTraceVideo.setBounds(680, 380, 90, 50);
        hTraceAddActionListener(hTraceUserspaceCheckBox);
        addActionListener(hTraceAudio);
        addActionListener(hTraceCamera);
        addActionListener(hTraceDatabase);
        addActionListener(hTraceGraphics);
        addActionListener(hTraceInput);
        addActionListener(hTraceNetWork);
        addActionListener(hTraceVideo);
        probesCenterPanel.add(hTraceUserspaceCheckBox);
        probesCenterPanel.add(hTraceUserspaceCheckBoxDes);
        probesCenterPanel.add(hTraceAudio);
        probesCenterPanel.add(hTraceCamera);
        probesCenterPanel.add(hTraceDatabase);
        probesCenterPanel.add(hTraceGraphics);
        probesCenterPanel.add(hTraceInput);
        probesCenterPanel.add(hTraceNetWork);
        probesCenterPanel.add(hTraceVideo);
    }

    /**
     * hTraceAddActionListener
     *
     * @param checkBoxObject checkBoxObject
     */
    public void hTraceAddActionListener(JBCheckBox checkBoxObject) {
        checkBoxObject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hTraceUserspaceCheckBox.isSelected()) {
                    hTraceAudio.setSelected(true);
                    hTraceCamera.setSelected(true);
                    hTraceDatabase.setSelected(true);
                    hTraceGraphics.setSelected(true);
                    hTraceInput.setSelected(true);
                    hTraceNetWork.setSelected(true);
                    hTraceVideo.setSelected(true);
                } else {
                    hTraceAudio.setSelected(false);
                    hTraceCamera.setSelected(false);
                    hTraceDatabase.setSelected(false);
                    hTraceGraphics.setSelected(false);
                    hTraceInput.setSelected(false);
                    hTraceNetWork.setSelected(false);
                    hTraceVideo.setSelected(false);
                }
            }
        });
    }

    /**
     * addActionListener
     *
     * @param checkBoxObject checkBoxObject
     */
    public void addActionListener(JBCheckBox checkBoxObject) {
        checkBoxObject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hTraceUserspaceCheckBoxAllSelected()) {
                    hTraceUserspaceCheckBox.setSelected(true);
                } else {
                    hTraceUserspaceCheckBox.setSelected(false);
                }
            }
        });
    }

    private boolean hTraceUserspaceCheckBoxAllSelected() {
        if (hTraceAudio.isSelected() && hTraceCamera.isSelected()) {
            if (hTraceDatabase.isSelected() && hTraceGraphics.isSelected()) {
                if (hTraceInput.isSelected() && hTraceNetWork.isSelected() && hTraceVideo.isSelected()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void initProbesTabItems() {
        // init the probes items
        probesWestPanel = new JBPanel(null);
        probesCenterPanel = new JBPanel(null);
        probesCpu = new JBLabel(PROBES_CPU_STR, JBLabel.CENTER);
        probesCpu.setOpaque(true);
        probesCpu.setBorder(BorderFactory.createLineBorder(new Color(0, 117, 255), 1));
        probesCpu.setBounds(0, 50, 200, 70);
        probesWestPanel.add(probesCpu);
        probesCpu.setVisible(false);
        probesWestPanel.setPreferredSize(new Dimension(200, 500));
        // init the component
        recordModelTitle = new JBLabel(RECORD_MODEL_STR);
        schedulingCheckBox = new JBCheckBox(SCHEDULING_STR, true);
        schedulingCheckBoxDes = new JBLabel(SCHEDULING_DES_STR, JBLabel.LEFT);
        cpuFrequencyCheckBox = new JBCheckBox(CPU_FREQUENCY_STR, false);
        cpuFrequencyCheckBoxDes = new JBLabel(CPU_FREQUENCY_DES_STR, JBLabel.LEFT);
        boardCheckBox = new JBCheckBox(BOARD_STR, false);
        boardCheckBoxDes = new JBLabel(BOARD_DES_STR, JBLabel.LEFT);
        highFrequencyCheckBox = new JBCheckBox(HIGH_FREQUENCY_STR, false);
        highFrequencyCheckBoxDes = new JBLabel(HIGH_FREQUENCY_DES_STR, JBLabel.LEFT);

        syscallsCheckBox = new JBCheckBox(SYSCALLS_STR, false);
        syscallsCheckBoxDes = new JBLabel(SYSCALLS_DES_STR, JBLabel.LEFT);
        // set the font
        schedulingCheckBox.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        cpuFrequencyCheckBox.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        boardCheckBox.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        highFrequencyCheckBox.setFont(new Font(Font.DIALOG, Font.BOLD, 14));

        syscallsCheckBox.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        // set the bounds
        recordModelTitle.setBounds(50, 13, 200, 50);
        schedulingCheckBox.setBounds(50, 50, 250, 50);
        schedulingCheckBoxDes.setBounds(70, 80, 350, 50);

        cpuFrequencyCheckBox.setBounds(50, 140, 250, 50);
        cpuFrequencyCheckBoxDes.setBounds(70, 170, 350, 50);
        boardCheckBox.setBounds(500, 50, 250, 50);
        boardCheckBoxDes.setBounds(520, 80, 350, 60);
        highFrequencyCheckBox.setBounds(500, 140, 250, 50);
        highFrequencyCheckBoxDes.setBounds(520, 170, 350, 80);
        syscallsCheckBox.setBounds(50, 230, 250, 50);
        syscallsCheckBoxDes.setBounds(70, 260, 350, 50);
        separator.setBounds(50, 310, 850, 10);
        separator.setBackground(new Color(255, 255, 255));
        addProbesCenterPanel();
    }

    /**
     * addProbesCenterPanel
     */
    private void addProbesCenterPanel() {
        // add component
        probesCenterPanel.add(recordModelTitle);
        probesCenterPanel.add(schedulingCheckBox);
        probesCenterPanel.add(schedulingCheckBoxDes);
        probesCenterPanel.add(cpuFrequencyCheckBox);
        probesCenterPanel.add(cpuFrequencyCheckBoxDes);
        probesCenterPanel.add(boardCheckBox);
        probesCenterPanel.add(boardCheckBoxDes);
        probesCenterPanel.add(highFrequencyCheckBox);
        probesCenterPanel.add(highFrequencyCheckBoxDes);
        probesCenterPanel.add(syscallsCheckBox);
        probesCenterPanel.add(syscallsCheckBoxDes);
        probesCenterPanel.add(separator);
        // checkBoxState
        probesTab.add(probesWestPanel, BorderLayout.WEST);
        probesTab.add(probesCenterPanel, BorderLayout.CENTER);
    }

    private void initButtonPanel() {
        buttonPanel = new JBPanel(new MigLayout("insets 0", "push[]20[]20",
            "[fill,fill]"));
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
        this.add(buttonPanel, "wrap, span, height 40!");
    }

    /**
     * addEventListener
     */
    private void addEventListener() {
        addDeviceRefresh();
        lastStepBtn.addMouseListener(this);
        startTaskBtn.addMouseListener(this);
        deviceComboBox.addItemListener(this);
    }

    /**
     * addDeviceRefresh
     */
    public void addDeviceRefresh() {
        QuartzManager.getInstance().addExecutor(DEVICE_REFRESH, new Runnable() {
            /**
             * run
             */
            @Override
            public void run() {
                deviceInfoList = MultiDeviceManager.getInstance().getOnlineDeviceInfoList();
                Vector<String> items = new Vector<>();
                deviceInfoList.forEach(deviceInfo -> {
                    items.add(deviceInfo.getDeviceName());
                });
                if (!deviceInfo.equals(items)) {
                    deviceInfo = items;
                    deviceComboBox.setModel(new DefaultComboBoxModel(items));
                    for (DeviceIPPortInfo deviceIPInfo : deviceInfoList) {
                        if (deviceIPInfo.getDeviceName().equals(deviceComboBox.getSelectedItem())) {
                            deviceIPPortInfo = deviceIPInfo;
                        } else {
                            deviceIPPortInfo = deviceInfoList.get(0);
                        }
                    }
                }
            }
        });
        QuartzManager.getInstance().startExecutor(DEVICE_REFRESH, LayoutConstants.DEFAULT_NUMBER,
            LayoutConstants.NUMBER_THREAD);
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        String name = mouseEvent.getComponent().getName();
        if (name.equals(LAST_STEP_BTN)) {
            contentPanel.getTabContainer().remove(this);
            contentPanel.getTabItem().setVisible(true);
            contentPanel.getTabContainer().repaint();
        }
        if (name.equals(START_TASK_BTN)) {
            int itemCount = deviceComboBox.getItemCount();
            // create system tuning load dialog object
            if (itemCount == 0) {
                new SampleDialog("prompt", "Device list is empty !").show();
                return;
            }
            if (deviceIPPortInfo == null) {
                new SampleDialog("prompt", "Please select the device !").show();
                return;
            }
            QuartzManager.getInstance().endExecutor(DEVICE_REFRESH);
            loadTraceRecordDialog();
            contentPanel.getTabContainer().repaint();
        }
    }

    /**
     * loadTraceRecordDialog
     */
    private void loadTraceRecordDialog() {
        try {
            if (!getClassificationSelect()) {
                new SampleDialog("prompt", "Please select the classification !").show();
            } else {
                String sessionId;
                if (chooseMode) {
                    eventStr = eventStr + ";sched";
                    sessionId = new SystemTraceHelper()
                        .createSessionByTraceRequest(deviceIPPortInfo, eventStr, maxDuration, inMemoryValue, false);
                } else {
                    ArrayList<ArrayList<String>> eventsList = new ArrayList();
                    ArrayList<ArrayList<String>> hTraceEventsList = new ArrayList();
                    getEvent(eventsList, hTraceEventsList);
                    sessionId = new SystemTraceHelper()
                        .createSessionHtraceRequest(deviceIPPortInfo, eventsList, hTraceEventsList, maxDuration,
                            inMemoryValue);
                }
                new TraceRecordDialog().load(contentPanel, maxDuration, sessionId, deviceIPPortInfo, chooseMode);
            }
        } catch (GrpcException grpcException) {
            grpcException.printStackTrace();
        }
    }

    /**
     * getClassificationSelect
     *
     * @return boolean
     */
    public boolean getClassificationSelect() {
        if (!schedulingCheckBox.isSelected() && !cpuFrequencyCheckBox.isSelected() && !boardCheckBox.isSelected()
            && !syscallsCheckBox.isSelected() && !highFrequencyCheckBox.isSelected() && !hTraceAudio.isSelected()
            && !hTraceCamera.isSelected() && !hTraceDatabase.isSelected() && !hTraceGraphics.isSelected()
            && !hTraceInput.isSelected() && !hTraceNetWork.isSelected() && !hTraceVideo.isSelected()) {
            return false;
        }else {
            return true;
        }
    }

    /**
     * getEvent
     *
     * @param eventsList eventsList
     * @param hTraceEventsList hTraceEventsList
     */
    public void getEvent(ArrayList<ArrayList<String>> eventsList, ArrayList<ArrayList<String>> hTraceEventsList) {
        if (schedulingCheckBox.isSelected()) {
            eventsList.add(schedulingEvents);
        }
        if (boardCheckBox.isSelected()) {
            eventsList.add(powerEvents);
        }
        if (cpuFrequencyCheckBox.isSelected()) {
            eventsList.add(cpuFreqEvents);
        }
        if (syscallsCheckBox.isSelected()) {
            eventsList.add(sysCallsEvents);
        }
        if (highFrequencyCheckBox.isSelected()) {
            eventsList.add(highFrequencyEvents);
        }
        if (hTraceAudio.isSelected()) {
            hTraceEventsList.add(hTraceAudioEvents);
        }
        if (hTraceCamera.isSelected()) {
            hTraceEventsList.add(hTraceCameraEvents);
        }
        if (hTraceDatabase.isSelected()) {
            hTraceEventsList.add(hTraceDatabaseEvents);
        }
        if (hTraceGraphics.isSelected()) {
            hTraceEventsList.add(hTraceGraphicsEvents);
        }
        if (hTraceInput.isSelected()) {
            hTraceEventsList.add(hTraceInputEvents);
        }
        if (hTraceNetWork.isSelected()) {
            hTraceEventsList.add(hTraceNetWorkEvents);
        }
        if (hTraceVideo.isSelected()) {
            hTraceEventsList.add(hTraceVideoEvents);
        }
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {
        if (deviceInfoList != null && deviceInfoList.size() > 0) {
            for (DeviceIPPortInfo deviceIPInfo : deviceInfoList) {
                if (deviceIPInfo.getDeviceName().equals(deviceComboBox.getSelectedItem())) {
                    deviceIPPortInfo = deviceIPInfo;
                }
            }
        }
    }
}
