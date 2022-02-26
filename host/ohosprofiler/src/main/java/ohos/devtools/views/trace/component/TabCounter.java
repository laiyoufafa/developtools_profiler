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
import ohos.devtools.views.trace.bean.Counter;
import ohos.devtools.views.trace.bean.TabCounterBean;
import ohos.devtools.views.trace.util.ComparatorUtils;
import ohos.devtools.views.trace.util.Db;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * TabCounter
 *
 * @since 2021/5/12 16:34
 */
public class TabCounter extends JBPanel {
    private JBTable table = new JBTable();
    private FTableModel<TabCounterBean> model = new FTableModel();
    private List<FTableModel.Column<TabCounterBean>> columns;
    private JBLabel selectRangeLabel = new JBLabel("selected range:");
    private TableRowSorter<FTableModel> tableRowSorter;
    private long leftNs;
    private long rightNs;

    /**
     * structure function
     */
    public TabCounter() {
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
     * set TabData
     *
     * @param trackIds trackIds
     * @param leftNs leftNs
     * @param rightNs rightNs
     */
    public void loadTabData(final List<Integer> trackIds, long leftNs, long rightNs) {
        this.leftNs = leftNs;
        this.rightNs = rightNs;
        selectRangeLabel.setText("Selected range:" + (rightNs - leftNs) / 1000000.0 + "ms");
        if (trackIds != null && !trackIds.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            for (int index = 0, size = trackIds.size(); index < size; index++) {
                buffer.append(trackIds.get(index));
                if (index < size - 1) {
                    buffer.append(",");
                }
            }
            loadTabData2(buffer);
        }
    }

    private void loadTabData2(StringBuffer buffer) {
        CompletableFuture.runAsync(() -> {
            List<Counter> result = new ArrayList<>() {
            };
            Db.getInstance().query(Sql.SYS_GET_TAB_COUNTERS, result, buffer.toString(), rightNs);
            Map<Integer, List<Counter>> collect = result.stream().collect(Collectors.groupingBy(Counter::getTrackId));
            Set<Integer> keys = collect.keySet();
            List<TabCounterBean> source = new ArrayList<>();
            double range = (rightNs - leftNs) * 1.0 / 1000000000;
            int count = 0;
            for (Integer key : keys) {
                List<Counter> counters = collect.get(key);
                List<Counter> list =
                    counters.stream().filter(counter -> counter.getStartTime() > leftNs).collect(Collectors.toList());
                if (list.size() > 0) {
                    int index = counters.indexOf(list.get(0));
                    if (index > 0) {
                        list.add(0, counters.get(index - 1));
                    }
                } else {
                    list.add(counters.get(counters.size() - 1));
                }
                TabCounterBean tabCounter = getTabCounter(list, range);
                count += tabCounter.getCount();
                source.add(tabCounter);
            }
            TabCounterBean tcb = new TabCounterBean();
            tcb.setCount(count);
            source.add(0, tcb);
            SwingUtilities.invokeLater(() -> {
                if (result != null && result.size() > 0) {
                    // set row sorter
                    TabCounterBean cb = source.get(0);
                    for (int index = 0; index < 9; index++) {
                        if (index == 4) {
                            tableRowSorter.setComparator(index, ComparatorUtils.generateComparator(cb.getCount() + ""));
                        } else {
                            tableRowSorter.setComparator(index, ComparatorUtils.generateComparator(""));
                        }
                    }
                }
                model.setDataSource(source);
                model.fireTableDataChanged();
            });
        }, Utils.getPool()).whenComplete((unused, throwable) -> {
            if (Objects.nonNull(throwable)) {
                throwable.printStackTrace();
            }
        });
    }

    private TabCounterBean getTabCounter(List<Counter> list, double range) {
        TabCounterBean bean = new TabCounterBean();
        if (list.size() > 0) {
            Counter first = list.get(0);
            bean.setTrackId(first.getTrackId());
            bean.setName(first.getName());
            bean.setFirstValue(first.getValue());
            bean.setCount(list.size());
            bean.setLastValue(list.get(list.size() - 1).getValue());
            // delta value = (last value - first value)
            bean.setDeltaValue(bean.getLastValue() - bean.getFirstValue());
            // rate = (last value - first value) / time range
            bean.setRate(bean.getDeltaValue() / range);
            List<Counter> collect =
                list.stream().sorted(Comparator.comparing(Counter::getValue)).collect(Collectors.toList());
            bean.setMinValue(collect.get(0).getValue());
            bean.setMaxValue(collect.get(collect.size() - 1).getValue());
            // Calculate the weighted average value
            double weightAvg = 0.0D;
            long timeRange = rightNs - leftNs;
            for (int index = 0, size = list.size(); index < size; index++) {
                long start = index == 0 ? leftNs : list.get(index).getStartTime();
                long end = index == size - 1 ? rightNs : list.get(index + 1).getStartTime();
                weightAvg += list.get(index).getValue() * ((end - start) * 1.0 / timeRange);
            }
            BigDecimal decimal = new BigDecimal(weightAvg);
            weightAvg = decimal.setScale(2, RoundingMode.HALF_UP).doubleValue();
            bean.setWeightAvgValue(weightAvg);
        }
        return bean;
    }

    private void initColumns() {
        columns = new ArrayList<>();
        columns.add(new FTableModel.Column<>("Name", item -> item.getName() == null ? "" : item.getName()));
        columns.add(
            new FTableModel.Column<>("Delta value", item -> item.getDeltaValue() == null ? "" : item.getDeltaValue()));
        columns.add(new FTableModel.Column<>("Rate /s", item -> item.getRate() == null ? "" : item.getRate()));
        columns.add(new FTableModel.Column<>("Weight avg value",
            item -> item.getWeightAvgValue() == null ? "" : item.getWeightAvgValue()));
        columns.add(new FTableModel.Column<>("Count", item -> item.getCount() == null ? "" : item.getCount()));
        columns.add(
            new FTableModel.Column<>("First value", item -> item.getFirstValue() == null ? "" : item.getFirstValue()));
        columns.add(
            new FTableModel.Column<>("Last value", item -> item.getLastValue() == null ? "" : item.getLastValue()));
        columns
            .add(new FTableModel.Column<>("Min value", item -> item.getMinValue() == null ? "" : item.getMinValue()));
        columns
            .add(new FTableModel.Column<>("Max value", item -> item.getMaxValue() == null ? "" : item.getMaxValue()));
    }

}
