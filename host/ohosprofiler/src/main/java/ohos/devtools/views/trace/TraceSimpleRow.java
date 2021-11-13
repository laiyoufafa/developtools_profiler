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

package ohos.devtools.views.trace;

import com.intellij.ui.JBColor;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.SwingUtilities;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * TraceSimpleRow
 *
 * @since 2021/5/18 13:56
 */
public class TraceSimpleRow<T extends AbstractNode> extends AbstractRow {
    private IRender render;
    private List<T> data;
    private T currentData;
    private Supplier<List<T>> supplier;

    /**
     * structure
     *
     * @param name name
     */
    public TraceSimpleRow(String name) {
        super(name, false, true);
        loadData();
    }

    /**
     * setRender
     *
     * @param render set current render
     */
    public void setRender(IRender<T> render) {
        this.render = render;
    }

    /**
     * setSupplier
     *
     * @param supplier set current supplier
     */
    public void setSupplier(Supplier<List<T>> supplier) {
        this.supplier = supplier;
    }

    @Override
    public void contentPaint(Graphics graphics) {
        if (render != null) {
            if (Objects.isNull(data)) {
                graphics.setColor(JBColor.foreground());
                graphics.drawString("Loading...", 10, 10 + 12);
                loadData();
            } else {
                if (graphics instanceof Graphics2D) {
                    render.paint((Graphics2D) graphics, data);
                }
            }
        }
    }

    @Override
    public void mouseMoveHandler(Point point) {
        super.mouseMoveHandler(point);
        if (Objects.nonNull(data)) {
            if (data.stream().filter(it -> contains(it)).anyMatch(it -> it.getRect().contains(point))) {
                data.stream().filter(it -> contains(it) && it.getRect().contains(point)).forEach(it -> {
                    List<String> stringList = it.getStringList(getTimeByX(Utils.getX(point)));
                    Tip.getInstance().display(content, point, stringList);
                    if (Objects.nonNull(currentData)) {
                        currentData.moveOut(point, content);
                    }
                    it.moveIn(point, content);
                    currentData = it;
                });
            } else {
                if (Objects.nonNull(currentData)) {
                    currentData.moveOut(point, content);
                }
                Tip.getInstance().display(content, point, Arrays.asList(getTimeByX(Utils.getX(point))));
            }
        }
    }

    @Override
    public void loadData() {
        if (!isLoading.get()) {
            isLoading.set(true);
            CompletableFuture.runAsync(() -> {
                if (supplier != null) {
                    data = supplier.get();
                }
                SwingUtilities.invokeLater(() -> {
                    isLoading.set(false);
                    content.repaint();
                });
            }, Utils.getPool()).whenComplete((unused, throwable) -> {
                if (Objects.nonNull(throwable)) {
                    throwable.printStackTrace();
                }
            });
        }
    }

    /**
     * reload current row
     */
    public void reload() {
        data = null;
        repaint();
    }

    /**
     * interface IRender
     */
    public interface IRender<T> {
        /**
         * paint
         *
         * @param g2 Graphics2D
         * @param data data
         */
        void paint(Graphics2D g2, List<T> data);
    }
}
