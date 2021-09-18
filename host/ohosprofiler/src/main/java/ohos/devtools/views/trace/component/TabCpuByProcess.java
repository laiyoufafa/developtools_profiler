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

package ohos.devtools.views.trace.component;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.trace.Sql;
import ohos.devtools.views.trace.bean.TabThreadStatesBean;
import ohos.devtools.views.trace.util.ComparatorUtils;
import ohos.devtools.views.trace.util.Db;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * TabCounterBean
 *
 * @date: 2021/5/12 16:34
 */
public class TabCpuByProcess extends JBPanel {
    private JBTable table = new JBTable();
    private FTableModel<TabThreadStatesBean> model = new FTableModel();
    private List<FTableModel.Column<TabThreadStatesBean>> columns;
    private JBLabel selectRangeLabel = new JBLabel("selected range:");
    private TableRowSorter<FTableModel> tableRowSorter;

    /**
     * structure function
     */
    public TabCpuByProcess() {
        setLayout(new MigLayout("insets 0", "[grow,fill][]", "[15!,fill][grow,fill]"));
        setFocusable(true);
        add(selectRangeLabel, "skip 1,wrap");
        initColumns();
        model.setColumns(columns);
        table.setModel(model);
        JBScrollPane jsp = new JBScrollPane();
        jsp.setViewportView(table);
        add(jsp, "span");
        tableRowSorter = new TableRowSorter(model);
        table.setRowSorter(tableRowSorter);
    }

    /**
     * loadTabData
     *
     * @param cpus    cpus
     * @param leftNs  leftNs
     * @param rightNs rightNs
     */
    public void loadTabData(final List<Integer> cpus, long leftNs, long rightNs) {
        selectRangeLabel.setText("Selected range:" + (rightNs - leftNs) / 1000000.0 + "ms");
        if (cpus != null && !cpus.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            for (int index = 0, size = cpus.size(); index < size; index++) {
                buffer.append(cpus.get(index));
                if (index < size - 1) {
                    buffer.append(",");
                }
            }
            CompletableFuture.runAsync(() -> {
                List<TabThreadStatesBean> result = new ArrayList<>() {
                };
                Db.getInstance().query(Sql.SYS_GET_TAB_PROCESS_BY_CPU, result, buffer.toString(), leftNs, rightNs);
                SwingUtilities.invokeLater(() -> {
                    if (result != null && result.size() > 0) {
                        long sumWall = 0;
                        int sumOcc = 0;
                        for (TabThreadStatesBean bean : result) {
                            sumWall += bean.getWallDuration();
                            sumOcc += bean.getOccurrences();
                        }
                        TabThreadStatesBean sumBean = new TabThreadStatesBean();
                        sumBean.setProcess(" ");
                        sumBean.setWallDuration(sumWall);
                        sumBean.setOccurrences(sumOcc);
                        result.add(0, sumBean);
                        TabThreadStatesBean count = result.get(0);
                        tableRowSorter.setComparator(0, ComparatorUtils.generateComparator(" "));
                        tableRowSorter.setComparator(1, ComparatorUtils.generateComparator(""));
                        tableRowSorter.setComparator(2,
                            ComparatorUtils.generateComparator(Utils.transformTimeToMs(count.getWallDuration())));
                        tableRowSorter.setComparator(3, ComparatorUtils.generateComparator(""));
                        tableRowSorter.setComparator(4,
                            ComparatorUtils.generateComparator(String.valueOf(count.getOccurrences())));
                    }
                    model.setDataSource(result);
                    model.fireTableDataChanged();
                });
            }, Utils.getPool()).whenComplete((unused, throwable) -> {
                if (Objects.nonNull(throwable)) {
                    throwable.printStackTrace();
                }
            });
        }
    }

    private void initColumns() {
        columns = new ArrayList<>();
        columns.add(new FTableModel.Column<>("Process",
            item -> item.getProcess() == null || item.getProcess().isEmpty() ? "[NULL]" : item.getProcess()));
        columns.add(new FTableModel.Column<>("PID", item -> item.getPid() == null ? "" : item.getPid()));
        columns.add(new FTableModel.Column<>("Wall Duration（ms）",
            item -> item.getWallDuration() == null ? "" : Utils.transformTimeToMs(item.getWallDuration())));
        columns.add(new FTableModel.Column<>("Avg Wall Duration（ms）",
            item -> item.getAvgDuration() == null ? "" : Utils.transformTimeToMs(item.getAvgDuration())));
        // Occurrences : time range the same thread state count
        columns.add(new FTableModel.Column<>("Occurrences",
            item -> item.getOccurrences() == null ? "" : item.getOccurrences()));
    }

}
