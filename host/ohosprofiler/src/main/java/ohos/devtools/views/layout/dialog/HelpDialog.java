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

package ohos.devtools.views.layout.dialog;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import ohos.devtools.views.common.LayoutConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Properties;

/**
 * Help Dialog
 */
public class HelpDialog {
    private static final Logger LOGGER = LogManager.getLogger(HelpDialog.class);
    private static final int DIALOG_WIDTH = 650;
    private static final int DIALOG_HEIGHT = 120;
    private static final int FONT_SIZE = 16;
    private static final int ICON_X = 15;
    private static final int TEXT_X = 70;
    private static final int THIRTY = 30;
    private static final int LICENSE_X = 500;
    private static final int LICENSE_Y = 90;
    private static final int LICENSE_WIDTH = 150;
    private static final int COPY_RIGHT_Y = 56;
    private static final int COPY_RIGHT_WIDTH = 330;
    private static final int VERSION_WIDTH = 590;
    private CustomDialog sampleDialog;
    private JBPanel jPanel;
    private JBLabel toolVersionNumber;
    private JBLabel copyRightIcon;
    private JBLabel copyRightLabel;
    private JBLabel license;

    /**
     * HelpDialog
     */
    public HelpDialog() {
        initComponent();
        setFontAndBounds();
        // Add components
        copyRightIcon.setIcon(IconLoader.getIcon("/images/copyright.png", getClass()));
        jPanel.add(toolVersionNumber);
        jPanel.add(copyRightIcon);
        jPanel.add(copyRightLabel);
        String labelText = "<html><p style=\"white-space:nowrap;overflow:hidden;margin-top: 1px;"
            + "text-overflow:ellipsis;text-decoration:underline;line-height:10px;font-size:10px\">"
            + "<font color=\"#369EFD\">Open Source Licenses</font></p><html>";
        license.setText(labelText);
        addListener();
        jPanel.add(license);
        sampleDialog.setResizable(true);
        sampleDialog.show();
    }

    private void initComponent() {
        jPanel = new JBPanel(null);
        jPanel.setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
        sampleDialog = new CustomDialog("About HosProfiler", jPanel);
        toolVersionNumber = new JBLabel("");
        toolVersionNumber.setText("HosProfiler " + getVersion());
        copyRightIcon = new JBLabel();
        copyRightLabel = new JBLabel("Copyright (c) 2021 Huawei Device Co., Ltd.");
        license = new JBLabel();
    }

    private String getVersion() {
        Properties properties = new Properties();
        String version = "";
        try {
            properties.load(HelpDialog.class.getClassLoader().getResourceAsStream("hosprofiler.properties"));
            version = properties.getProperty("version");
        } catch (IOException exception) {
            LOGGER.error("get version Exception {}", exception.getMessage());
        }
        return version;
    }

    private void addListener() {
        license.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                sampleDialog.close(1);
                new LicenseDialog();
            }

            @Override
            public void mouseEntered(MouseEvent event) {
                license.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent event) {
                license.setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    private void setFontAndBounds() {
        copyRightIcon.setBounds(ICON_X, THIRTY, THIRTY, THIRTY);
        toolVersionNumber.setFont(new Font("PingFang SC", Font.PLAIN, FONT_SIZE));
        toolVersionNumber.setBounds(TEXT_X, LayoutConstants.NUM_20, VERSION_WIDTH, LayoutConstants.NUM_20);
        copyRightLabel.setBounds(TEXT_X, COPY_RIGHT_Y, COPY_RIGHT_WIDTH, LayoutConstants.NUM_20);
        license.setBounds(LICENSE_X, LICENSE_Y, LICENSE_WIDTH, LayoutConstants.NUM_20);
    }
}
