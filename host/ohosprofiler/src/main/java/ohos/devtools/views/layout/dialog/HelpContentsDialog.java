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

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.jcef.JBCefBrowser;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.trace.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * HelpContentsDialog
 *
 * @since : 2021/10/25
 */
public class HelpContentsDialog extends DialogWrapper {
    private static final Logger LOGGER = LogManager.getLogger(HelpContentsDialog.class);
    private static final String TITLE = "Help Contents";
    private static final String HELP_CONTENTS_MAIN_FILE = "helps/main.html";
    private static final int DIALOG_WIDTH = 1424;
    private static final int DIALOG_HEIGHT = 768;

    /**
     * constructor
     */
    public HelpContentsDialog() {
        super(true);
        init();
        setTitle(TITLE);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel content = new JPanel(new BorderLayout());
        JBCefBrowser jbCefBrowser = new JBCefBrowser();
        content.add(jbCefBrowser.getComponent(), BorderLayout.CENTER);
        content.setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
        File htmlFile = new File(SessionManager.getInstance().getPluginPath() + HELP_CONTENTS_MAIN_FILE);
        jbCefBrowser.loadHTML(getHtmlString(htmlFile));
        // load html
        CompletableFuture.runAsync(() -> {
            String htmlStr = getHtmlString(htmlFile);
            SwingUtilities.invokeLater(() -> {
                jbCefBrowser.loadHTML(htmlStr);
            });
        }, Utils.getPool()).whenComplete((unused, throwable) -> {
            if (Objects.nonNull(throwable)) {
                throwable.printStackTrace();
            }
        });
        return content;
    }

    private String getHtmlString(File htmlFile) {
        StringBuffer htmlStringBuffer = new StringBuffer();
        try {
            BufferedReader bufferReader =
                new BufferedReader(new InputStreamReader(new FileInputStream(htmlFile), "utf-8"));
            while (bufferReader.ready()) {
                String line = bufferReader.readLine();
                if (line.contains(".html") && line.contains("#target#")) {
                    String absolutePath = htmlFile.getCanonicalPath();
                    String newPath = absolutePath.substring(0, absolutePath.lastIndexOf("\\")) + File.separator;
                    line = line.replace("#target#", newPath);
                }
                htmlStringBuffer.append(line);
            }
            bufferReader.close();
        } catch (IOException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("get Html String fail {}", exception.getMessage());
            }
        }
        return htmlStringBuffer.toString();
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        return null;
    }
}
