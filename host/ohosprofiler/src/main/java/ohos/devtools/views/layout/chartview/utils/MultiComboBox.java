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

package ohos.devtools.views.layout.chartview.utils;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import net.miginfocom.swing.MigLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * MultiComboBox
 *
 * @param <T>
 * @since 2021/11/16 9:31
 */
public class MultiComboBox<T> extends JComponent implements ActionListener {
    private static final Logger LOGGER = LogManager.getLogger(MultiComboBox.class);

    /**
     * scroll pane width
     */
    private static final int SCROLL_PANE_WIDTH = 500;

    /**
     * scroll_pane_height
     */
    private static final int SCROLL_PANE_HEIGHT = 100;

    /**
     * arrow button width
     */
    private static final int ARROW_BUTTON_WIDTH = 25;

    /**
     * text field height
     */
    private static final int TEXT_FIELD_HEIGHT = 20;

    /**
     * list values
     */
    private List<T> values;

    /**
     * multiDropDown popup
     */
    private MultiDropDownPopup popup;

    /**
     * multiDropDown textField
     */
    private JTextField textField;

    /**
     * multiDropDown arrowButton
     */
    private JButton arrowButton;

    /**
     * MultiComboBox
     *
     * @param value value
     */
    public MultiComboBox(List<T> value) {
        values = value;
        initComponent();
    }

    private void initComponent() {
        this.setLayout(new MigLayout("insets 0", "[grow]", "[grow]"));
        popup = new MultiDropDownPopup(values);
        textField = new JTextField();
        textField.setBackground(JBColor.background().brighter());
        textField.setPreferredSize(new Dimension(SCROLL_PANE_WIDTH, TEXT_FIELD_HEIGHT));
        textField.addActionListener(this);
        createArrowButton();
        arrowButton.setPreferredSize(new Dimension(ARROW_BUTTON_WIDTH, TEXT_FIELD_HEIGHT));
        arrowButton.addActionListener(this);
        add(textField, "split 2,growx");
        add(arrowButton, "span, flowx, wrap");
    }

    /**
     * Get selected data
     *
     * @return List <T>
     */
    public List<T> getSelectedValues() {
        return popup.selectedList;
    }

    /**
     * Set the value to be selected
     *
     * @param selectValues selectValues
     */
    public void setSelectValues(List<T> selectValues) {
        popup.setSelectValues(selectValues);
        setText(selectValues);
    }

    private void setText(List<T> values) {
        if (values != null && values.size() > 0) {
            textField.setText(values.toString().replace("[", "").replace("]", "").strip());
        } else {
            textField.setText("");
        }
    }

    /**
     * action Performed
     *
     * @param arg0 arg0
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (!popup.isVisible()) {
            popup.show(this, 0, getHeight());
        }
    }

    private void createArrowButton() {
        arrowButton = new JButton();
        arrowButton.setContentAreaFilled(false);
        arrowButton.setFocusPainted(false);
        arrowButton.setOpaque(true);
        arrowButton.setIcon(AllIcons.Actions.FindAndShowNextMatchesSmall);
        arrowButton.setPreferredSize(new Dimension(ARROW_BUTTON_WIDTH, TEXT_FIELD_HEIGHT));
    }

    /**
     * Multi DropDown Popup
     */
    private class MultiDropDownPopup extends JBPopupMenu implements ActionListener {
        private List<T> values;
        private List<JBCheckBox> checkBoxList = new ArrayList<>();
        private JButton commitButton;
        private JButton cancelButton;
        private List<T> selectedList = new ArrayList<>();

        /**
         * MultiDropDownPopup
         *
         * @param value value
         */
        public MultiDropDownPopup(List<T> value) {
            super();
            values = value;
            initComponent();
        }

        private void initComponent() {
            JBPanel checkboxPane = new JBPanel();
            this.setLayout(new MigLayout("insets 0", "20[grow]5[grow]", "[grow][grow]"));
            for (Object item : values) {
                JBCheckBox checkBox = new JBCheckBox(item.toString());
                checkBoxList.add(checkBox);
            }
            if (checkBoxList.get(0).getText().equals("Select All")) {
                checkBoxList.get(0).addItemListener(event -> {
                    if (checkBoxList.get(0).isSelected()) {
                        for (int index = 1; index < checkBoxList.size(); index++) {
                            if (!checkBoxList.get(index).isSelected()) {
                                checkBoxList.get(index).setSelected(true);
                            }
                        }
                    } else {
                        for (int index = 1; index < checkBoxList.size(); index++) {
                            if (checkBoxList.get(index).isSelected()) {
                                checkBoxList.get(index).setSelected(false);
                            }
                        }
                    }
                });

                checkboxPane.setLayout(new MigLayout("insets 0", "[]"));
                for (JBCheckBox checkBox : checkBoxList) {
                    checkboxPane.add(checkBox, "span");
                }
                JBPanel buttonPanel = new JBPanel(new MigLayout("insets 0", "[grow][][]"));
                commitButton = new JButton("Ok");
                commitButton.addActionListener(this);
                cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(this);
                buttonPanel.add(new JBLabel(), "growx,growy");
                buttonPanel.add(cancelButton);
                buttonPanel.add(commitButton, "gapright 5");
                JBScrollPane scrollPane = new JBScrollPane(checkboxPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setPreferredSize(new Dimension(SCROLL_PANE_WIDTH, SCROLL_PANE_HEIGHT));
                this.add(scrollPane, "span");
                this.add(buttonPanel, "span");

            }
        }

        /**
         * setSelectValues
         *
         * @param values values
         */
        private void setSelectValues(List<T> values) {
            if (values != null && values.size() > 0) {
                for (T value : values) {
                    for (JBCheckBox jbCheckBox : checkBoxList) {
                        if (value.equals(jbCheckBox.getText())) {
                            jbCheckBox.setSelected(true);
                        }
                    }
                }
                setText(getSelectedValues());
            }
        }

        /**
         * getSelectedValues
         *
         * @return List <T>
         */
        private List<T> getSelectedValues() {
            List<T> selectedValues = new ArrayList<>();
            if (checkBoxList.get(0).getText().equals("Select All")) {
                if (checkBoxList.get(0).isSelected()) {
                    for (int index = 1; index < checkBoxList.size(); index++) {
                        selectedValues.add(values.get(index));
                    }
                } else {
                    for (int index = 1; index < checkBoxList.size(); index++) {
                        if (checkBoxList.get(index).isSelected()) {
                            selectedValues.add(values.get(index));
                        }
                    }
                }
            } else {
                for (int index = 0; index < checkBoxList.size(); index++) {
                    if (checkBoxList.get(index).isSelected()) {
                        selectedValues.add(values.get(index));
                    }
                }
            }
            selectedList = selectedValues;
            return selectedValues;
        }

        /**
         * Auto-generated method stub
         *
         * @param event event
         */
        @Override
        public void actionPerformed(ActionEvent event) {
            Object source = event.getSource();
            if (source instanceof JButton) {
                JButton button = (JButton) source;
                if (button.equals(commitButton)) {
                    selectedList = getSelectedValues();
                    setText(selectedList);
                    popup.setVisible(false);
                } else if (button.equals(cancelButton)) {
                    popup.setVisible(false);
                } else {
                    LOGGER.info("not button is close");
                }
            }
        }
    }

    public JTextField getTextField() {
        return textField;
    }

    public void setTextField(JTextField textField) {
        this.textField = textField;
    }
}
