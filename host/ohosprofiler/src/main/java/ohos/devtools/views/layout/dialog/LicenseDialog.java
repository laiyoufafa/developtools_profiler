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

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;

import ohos.devtools.views.common.LayoutConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Dimension;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * License Dialog
 *
 * @since 2021/11/22
 */
public class LicenseDialog {
    private static final Logger LOGGER = LogManager.getLogger(LicenseDialog.class);
    private static final int DIALOG_WIDTH = 655;
    private static final int TITLE_FONT_SIZE = 16;
    private static final int FONT_SIZE = 12;
    private static final int DIALOG_HEIGHT = 450;
    private static final int TITLE_X = 210;
    private static final int SCROLL_Y = 50;
    private static final int SCROLL_HEIGHT = 400;
    private static final int TITLE_Y = 10;
    private static final int TITLE_WIDTH = 240;
    private static final int TEXT_AREA_ROWS = 20;
    private static final int TEXT_AREA_COLUMNS = 40;
    private final CustomDialog sampleDialog;
    private final JBPanel jPanel;
    private final JBLabel titleLabel;
    private final JTextArea licenseTextArea;
    private final JScrollPane licenseScroll;

    /**
     * LicenseDialog
     */
    public LicenseDialog() {
        jPanel = new JBPanel(null);
        sampleDialog = new CustomDialog("Open Source Software Notice", jPanel);
        titleLabel = new JBLabel("Open Source Software Notice");
        jPanel.setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
        licenseTextArea = new JTextArea("", TEXT_AREA_ROWS, TEXT_AREA_COLUMNS);
        licenseTextArea.setFont(new Font("PingFang SC", Font.PLAIN, FONT_SIZE));
        getLicenseText();
        licenseTextArea.setEditable(false);
        licenseScroll = new JScrollPane(licenseTextArea);
        licenseScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        licenseScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        setFontAndBounds();
        // Add components
        jPanel.add(titleLabel);
        jPanel.add(licenseScroll);
        sampleDialog.setResizable(true);
        sampleDialog.show();
    }

    private void getLicenseText() {
        String str;
        BufferedReader bufferedReader = null;
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = LicenseDialog.class.getClassLoader().getResourceAsStream("LICENSE");
            assert resourceAsStream != null;
            bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
            while ((str = bufferedReader.readLine()) != null) {
                licenseTextArea.append(str + System.lineSeparator());
            }
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        } finally {
            if (resourceAsStream != null) {
                try {
                    resourceAsStream.close();
                } catch (IOException exception) {
                    LOGGER.error(exception.getMessage());
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException exception) {
                    LOGGER.error(exception.getMessage());
                }
            }
        }
    }

    /**
     * Set the style of J Panel and Button
     */
    private void setFontAndBounds() {
        titleLabel.setFont(new Font("PingFang SC", Font.BOLD, TITLE_FONT_SIZE));
        titleLabel.setBounds(TITLE_X, TITLE_Y, TITLE_WIDTH, LayoutConstants.NUM_20);
        licenseScroll.setBounds(0, SCROLL_Y, DIALOG_WIDTH, SCROLL_HEIGHT);
        licenseScroll.setBorder(null);
    }
}
