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

package ohos.devtools.views.distributed.component;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.distributed.util.DistributedCache;
import ohos.devtools.views.trace.util.Final;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;

/**
 * SettingDialog
 *
 * @since 2021/08/10 16:20
 */
public class SettingDialog extends JDialog {
    private final ActionListener cancelHandler = event -> {
        setVisible(false);
    };
    private JLabel title = new JLabel("Hitrace setting");
    private JBTextField totalInput = new JBTextField();
    private JBTextField delayInput = new JBTextField();
    private JButton okBtn = new JButton("Ok");
    private JButton cancelBtn = new JButton("Cancel");
    private SettingDialogListener listener;
    private final ActionListener okHandler = event -> {
        try {
            Double total = Double.parseDouble(totalInput.getText());
            Double delay = Double.parseDouble(delayInput.getText());
            DistributedCache.setTotalMedianTimes(total);
            DistributedCache.setDelayMedianTimes(delay);
        } catch (NumberFormatException exception) {
            exception.printStackTrace();
        }
        listener.settingOkClick();
        setVisible(false);
    };

    /**
     * SettingDialog
     *
     * @param listener listener
     */
    public SettingDialog(SettingDialogListener listener) {
        super();
        this.listener = listener;
        title.setFont(new Font(Final.FONT_NAME, Font.PLAIN, 14));
        setSize(500, 300);
        JPanel jp = new JPanel();
        jp.setLayout(new MigLayout("inset 10 20 20 10", "", ""));
        jp.add(title, "wrap");
        jp.add(new SettingLabel("Threshold："));
        jp.add(totalInput);
        jp.add(new SettingLabel("times median total"), "wrap");
        jp.add(new SettingLabel("Threshold："));
        jp.add(delayInput);
        jp.add(new SettingLabel("times median delay"), "wrap");
        JPanel content = new JPanel(new BorderLayout());
        JPanel bottom = new JPanel(new MigLayout("insets 5", "[grow,push,right][80]", "[50]"));
        content.add(jp, BorderLayout.CENTER);
        bottom.add(cancelBtn);
        bottom.add(okBtn);
        content.add(bottom, BorderLayout.SOUTH);
        setContentPane(content);
        setLocationRelativeTo(null);
        okBtn.addActionListener(okHandler);
        cancelBtn.addActionListener(cancelHandler);
        totalInput.setText(DistributedCache.getTotalMedianTimes().toString());
        delayInput.setText(DistributedCache.getDelayMedianTimes().toString());
    }

    /**
     * SettingDialogListener
     */
    public interface SettingDialogListener {
        /**
         * settingOkClick
         */
        void settingOkClick();
    }

    private class SettingLabel extends JBLabel {
        private AlphaComposite cmp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);

        public SettingLabel(String text) {
            super(text);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            if (graphics instanceof Graphics2D) {
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.setComposite(cmp.derive(0.59F));
                super.paintComponent(graphics);
            }
        }
    }
}
