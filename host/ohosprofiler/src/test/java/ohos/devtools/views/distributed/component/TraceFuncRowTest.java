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

import com.intellij.util.ui.UIUtil;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Trace Func Row Test
 *
 * @since 2021/2/1 9:31
 */
class TraceFuncRowTest {
    TraceFuncRow traceFuncRow = new TraceFuncRow("title", 1);

    @Test
    void getTid() {
        assertEquals(traceFuncRow.getTid(), 1);
    }

    @Test
    void setRender() throws NoSuchFieldException, IllegalAccessException {
        final Field field = traceFuncRow.getClass().getDeclaredField("render");
        field.setAccessible(true);
        traceFuncRow.setRender((g2, data2) -> {
            return;
        });
        assertNotNull(field.get(traceFuncRow));
    }

    @Test
    void setNullRender() throws NoSuchFieldException, IllegalAccessException {
        final Field field = traceFuncRow.getClass().getDeclaredField("render");
        field.setAccessible(true);
        traceFuncRow.setRender(null);
        assertNull(field.get(traceFuncRow));
    }

    @Test
    void setNormalRender() throws NoSuchFieldException, IllegalAccessException {
        final Field field = traceFuncRow.getClass().getDeclaredField("render");
        field.setAccessible(true);
        traceFuncRow.setRender((g2, data2) -> {
            data2.add(1);
        });
        assertNotNull(field.get(traceFuncRow));
    }

    @Test
    void setEmptyRender() throws NoSuchFieldException, IllegalAccessException {
        final Field field = traceFuncRow.getClass().getDeclaredField("render");
        field.setAccessible(true);
        traceFuncRow.setRender((g2, data2) -> {
            return;
        });
        assertNotNull(field.get(traceFuncRow));
    }

    @Test
    void setReturnRender() throws NoSuchFieldException, IllegalAccessException {
        final Field field = traceFuncRow.getClass().getDeclaredField("render");
        field.setAccessible(true);
        traceFuncRow.setRender((g2, data2) -> {
            return;
        });
        assertNotNull(field.get(traceFuncRow));
    }

    @Test
    void setSupplier() {
        traceFuncRow.setSupplier(() -> new ArrayList<>());
        assertNotNull(traceFuncRow);
    }

    @Test
    void setNullSupplier() {
        traceFuncRow.setSupplier(null);
        assertNotNull(traceFuncRow);
    }

    @Test
    void setEmptySupplier() {
        traceFuncRow.setSupplier(() -> null);
        assertNotNull(traceFuncRow);
    }

    @Test
    void setNormalSupplier() {
        traceFuncRow.setSupplier(() -> {
            ArrayList<Object> objects = new ArrayList<>();
            objects.add(1);
            return objects;
        });
        assertNotNull(traceFuncRow);
    }

    @Test
    void setTwoListSupplier() {
        traceFuncRow.setSupplier(() -> {
            ArrayList<Object> objects = new ArrayList<>();
            objects.add(1);
            objects.add(2);
            objects.add(3);
            return objects;
        });
        assertNotNull(traceFuncRow);
    }

    @Test
    void setSelect() {
        traceFuncRow.setSelect(false, 1, 2);
        assertNotNull(traceFuncRow);
    }

    @Test
    void setSelectTrue() {
        traceFuncRow.setSelect(true, 1, 2);
        assertNotNull(traceFuncRow);
    }

    @Test
    void setSelectMax() {
        traceFuncRow.setSelect(false, Integer.MAX_VALUE, 2);
        assertNotNull(traceFuncRow);
    }

    @Test
    void setSelectMin() {
        traceFuncRow.setSelect(false, Integer.MIN_VALUE, 2);
        assertNotNull(traceFuncRow);
    }

    @Test
    void setSelectNegative() {
        traceFuncRow.setSelect(false, -1, -1);
        assertNotNull(traceFuncRow);
    }

    @Test
    void contentPaint() {
        BufferedImage image = UIUtil.createImage(new JPanel(), 1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        traceFuncRow.contentPaint(graphics2D);
        assertNotNull(traceFuncRow);

    }

    @Test
    void mouseMoveHandler() {
        traceFuncRow.mouseMoveHandler(new Point(0, 0));
        assertNotNull(traceFuncRow);

    }

    @Test
    void loadData() {
        traceFuncRow.loadData();
        assertNotNull(traceFuncRow);
    }

    @Test
    void refreshNotify() {
        traceFuncRow.refreshNotify();
        assertNotNull(traceFuncRow);
    }

    @Test
    void getData() {
        assertNull(traceFuncRow.getData());
    }
}