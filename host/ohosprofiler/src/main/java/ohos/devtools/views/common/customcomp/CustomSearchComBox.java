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

package ohos.devtools.views.common.customcomp;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import ohos.devtools.views.common.UtConstant;
import org.apache.commons.lang.StringUtils;

import javax.swing.BorderFactory;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * CustomSearchComBox
 */
public class CustomSearchComBox extends JBPanel {
    private CustomTextField selectedProcessTextField;
    private CustomTextField searchField;
    private JBList processList;
    private JBScrollPane processScrollPane;
    private SelectedTextFileLister selectedTextFileListener;
    private CustomListFilerModel myListModel = new CustomListFilerModel();

    /**
     * CustomSearchComBox
     */
    public CustomSearchComBox() {
        this.setLayout(new BorderLayout());
        selectedProcessTextField = new CustomTextField("device");
        selectedProcessTextField.setName(UtConstant.UT_DEVICE_PROCESS_PANEL_PROCESS_NAME);
        selectedProcessTextField.setBackground(JBColor.background());
        selectedProcessTextField.setOpaque(true);
        selectedProcessTextField.setForeground(JBColor.foreground().brighter());
        selectedProcessTextField.setEditable(false);
        selectedProcessTextField.setBorder(BorderFactory.createLineBorder(JBColor.background().darker(), 1));
        this.add(selectedProcessTextField, BorderLayout.NORTH);
        searchField = new CustomTextField("press");
        searchField.setBackground(JBColor.background());
        searchField.setOpaque(true);
        searchField.setForeground(JBColor.foreground().brighter());
        searchField.setVisible(false);
        this.add(searchField, BorderLayout.CENTER);
        processList = new JBList(myListModel);
        processList.setBackground(JBColor.background());
        processList.setOpaque(true);
        processList.setForeground(JBColor.foreground().brighter());
        processList.setVisible(false);
        processList.setFont(new Font("PingFang SC", Font.PLAIN, 14));
        processScrollPane = new JBScrollPane(processList);
        processScrollPane.setVisible(false);
        this.add(processScrollPane, BorderLayout.SOUTH);
        selectedProcessTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 1) {
                    if (searchField.isShowing() && processList.isShowing()) {
                        hidden();
                        return;
                    } else {
                        selectedTextFileListener.textFileClick();
                        showProcess();
                    }
                }
            }
        });
        addProcessListListen();
        addProcessPanelEvent();
    }

    private void hidden() {
        myListModel.clear();
        searchField.setVisible(false);
        processScrollPane.setVisible(false);
        processList.setVisible(false);
        this.revalidate();
        this.repaint();
    }

    private void showProcess() {
        searchField.setVisible(true);
        processList.setVisible(true);
        processScrollPane.setVisible(true);
        this.revalidate();
        this.repaint();
    }

    private void addProcessListListen() {
        processList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Object selectedValue = processList.getSelectedValue();
                if (selectedValue instanceof String) {
                    String value = (String) selectedValue;
                    if (StringUtils.isNotBlank(value)) {
                        selectedProcessTextField.setText(value);
                        hidden();
                    }
                }
                if (processList.getSelectedIndex() == 0) {
                    processList.setSelectedValue(null, true);
                }
            }
        });
    }

    private void addProcessPanelEvent() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                String text = searchField.getText();
                myListModel.refilter(text);
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                String text = searchField.getText();
                myListModel.refilter(text);
            }

            @Override
            public void changedUpdate(DocumentEvent exception) {
            }
        });
    }

    /**
     * setSelectedTextFileListener
     *
     * @param selectedTextFileListener selectedTextFileListener
     */
    public void setSelectedTextFileListener(SelectedTextFileLister selectedTextFileListener) {
        this.selectedTextFileListener = selectedTextFileListener;
    }

    /**
     * Refresh process information
     *
     * @param processNames processNames
     */
    public void refreshProcess(List<String> processNames) {
        myListModel.clear();
        myListModel.addAll(processNames);
    }

    /**
     * getSelectedProcessName
     *
     * @return String
     */
    public String getSelectedProcessName() {
        return selectedProcessTextField.getText();
    }

    /**
     * getSelectedProcessTextFiled
     *
     * @return String
     */
    public CustomTextField getSelectedProcessTextFiled() {
        return selectedProcessTextField;
    }

    /**
     * clearSelectedName
     */
    public void clearSelectedName() {
        selectedProcessTextField.setText("");
        if (!myListModel.isEmpty()) {
            myListModel.clear();
        }
    }

}
