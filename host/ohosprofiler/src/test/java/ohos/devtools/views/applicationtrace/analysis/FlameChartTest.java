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

package ohos.devtools.views.applicationtrace.analysis;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Flame Chart Test
 *
 * @since: 2021/10/22 16:00
 */
class FlameChartTest {
    private FlameChart flameChart = new FlameChart();
    private JBScrollPane scrollPane =
        new JBScrollPane(flameChart, JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JBScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    @Test
    void change() {
        flameChart.change(1, 2, 3);
        assertNotNull(flameChart);
    }

    @Test
    void changeNormal() {
        flameChart.change(0, 0, 1);
        assertNotNull(flameChart);
    }

    @Test
    void changeParamNoNormal() {
        flameChart.change(-1, -1, -1);
        assertNotNull(flameChart);
    }

    @Test
    void changeParamError() {
        flameChart.change(100, -100, -100);
        assertNotNull(flameChart);
    }

    @Test
    void changeMaxParam() {
        flameChart.change(Long.MAX_VALUE, Long.MIN_VALUE, 0);
        assertNotNull(flameChart);
    }

    @Test
    void testChange() {
        flameChart.change(1, 2, new ArrayList<>());
        assertNotNull(flameChart);
    }

    @Test
    void testChangeParam() {
        flameChart.change(0, 0, new ArrayList<>());
        assertNotNull(flameChart);
    }

    @Test
    void testChangeMaxParam() {
        ArrayList<Integer> integers = new ArrayList<>();
        integers.add(-1);
        flameChart.change(Long.MAX_VALUE, Long.MIN_VALUE, integers);
        assertNotNull(flameChart);
    }

    @Test
    void testChangeErrorParam() {
        ArrayList<Integer> integers = new ArrayList<>();
        integers.add(-1);
        integers.add(Integer.MAX_VALUE);
        integers.add(Integer.MIN_VALUE);
        flameChart.change(100, -100, integers);
        assertNotNull(flameChart);
    }

    @Test
    void testChangeNormalParam() {
        ArrayList<Integer> integers = new ArrayList<>();
        integers.add(100);
        integers.add(Integer.MIN_VALUE);
        flameChart.change(100, 10000L, integers);
        assertNotNull(flameChart);
    }

    @Test
    void setCurrentEmptySearchText() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        flameChart.setAllNode(allNodes);
        flameChart.setCurrentSearchText("");
        final Field field = flameChart.getClass().getDeclaredField("currentSearchText");
        field.setAccessible(true);
        assertEquals("", field.get(flameChart));
    }

    @Test
    void setCurrentNullSearchText() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        flameChart.setAllNode(allNodes);
        flameChart.setCurrentSearchText(null);
        final Field field = flameChart.getClass().getDeclaredField("currentSearchText");
        field.setAccessible(true);
        assertNull(field.get(flameChart));
    }

    @Test
    void setCurrentNormalSearchText() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        flameChart.setAllNode(allNodes);
        flameChart.setCurrentSearchText("text");
        final Field field = flameChart.getClass().getDeclaredField("currentSearchText");
        field.setAccessible(true);
        assertEquals("text", field.get(flameChart));
    }

    @Test
    void setCurrentNoParamSearchText() throws NoSuchFieldException, IllegalAccessException {
        FlameChart flameChartNoParam = new FlameChart();
        JBScrollPane scrollPaneNoParam = new JBScrollPane(flameChartNoParam, JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JBScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        flameChartNoParam.setAllNode(allNodes);
        final Field field = flameChartNoParam.getClass().getDeclaredField("currentSearchText");
        field.setAccessible(true);
        assertEquals("", field.get(flameChartNoParam));
    }

    @Test
    void setCurrentParamSearchText() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        flameChart.setAllNode(allNodes);
        flameChart.setCurrentSearchText("currentSearchText");
        final Field field = flameChart.getClass().getDeclaredField("currentSearchText");
        field.setAccessible(true);
        assertEquals("currentSearchText", field.get(flameChart));
    }

    @Test
    void setAllNode() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        flameChart.setAllNode(allNodes);
        final Field field = flameChart.getClass().getDeclaredField("data");
        field.setAccessible(true);
        assertEquals(allNodes, field.get(flameChart));
    }

    @Test
    void setAllNodeItemNoNull() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        allNodes.add(node);
        flameChart.setAllNode(allNodes);
        final Field field = flameChart.getClass().getDeclaredField("data");
        field.setAccessible(true);
        assertEquals(allNodes, field.get(flameChart));
    }

    @Test
    void setAllNodeNull() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(null);
        allNodes.add(node);
        flameChart.setAllNode(allNodes);
        final Field field = flameChart.getClass().getDeclaredField("data");
        field.setAccessible(true);
        assertEquals(allNodes, field.get(flameChart));
    }

    @Test
    void setAllNodeParam() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(null);
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(null);
        allNodes.add(node);
        allNodes.add(childNode);
        flameChart.setAllNode(allNodes);
        final Field field = flameChart.getClass().getDeclaredField("data");
        field.setAccessible(true);
        assertEquals(allNodes, field.get(flameChart));
    }

    @Test
    void setAllNodeParamNoChildren() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
        allNodes.add(node);
        allNodes.add(childNode);
        flameChart.setAllNode(allNodes);
        final Field field = flameChart.getClass().getDeclaredField("data");
        field.setAccessible(true);
        assertEquals(allNodes, field.get(flameChart));
    }

    @Test
    void testSetAllNode() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        flameChart.setAllNode(allNodes, 0);
        final Field field = flameChart.getClass().getDeclaredField("duration");
        field.setAccessible(true);
        assertEquals(0L, field.get(flameChart));
    }

    @Test
    void testSetAllNodeItemNoNull() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        allNodes.add(node);
        flameChart.setAllNode(allNodes, 0);
        final Field field = flameChart.getClass().getDeclaredField("duration");
        field.setAccessible(true);
        assertEquals(0L, field.get(flameChart));
    }

    @Test
    void testSetAllNodeParam() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(null);
        allNodes.add(node);
        flameChart.setAllNode(allNodes, 0);
        final Field field = flameChart.getClass().getDeclaredField("duration");
        field.setAccessible(true);
        assertEquals(0L, field.get(flameChart));
    }

    @Test
    void testSetAllNodeNull() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(null);
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(null);
        allNodes.add(node);
        allNodes.add(childNode);
        flameChart.setAllNode(allNodes, 0);
        final Field field = flameChart.getClass().getDeclaredField("duration");
        field.setAccessible(true);
        assertEquals(0L, field.get(flameChart));
    }

    @Test
    void testSetAllNodeParamNoChildren() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
        allNodes.add(node);
        allNodes.add(childNode);
        flameChart.setAllNode(allNodes, 0);
        final Field field = flameChart.getClass().getDeclaredField("duration");
        field.setAccessible(true);
        assertEquals(0L, field.get(flameChart));
    }

    @Test
    void resetEmptyAllNode() {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        flameChart.setAllNode(allNodes);
        flameChart.resetAllNode();
        assertNotNull(flameChart);
    }

    @Test
    void resetOneNodeAllNode() {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        allNodes.add(node);
        flameChart.setAllNode(allNodes);
        flameChart.resetAllNode();
        assertNotNull(flameChart);
    }

    @Test
    void resetTwoNodeAllNode() {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
        allNodes.add(node);
        allNodes.add(childNode);
        flameChart.setAllNode(allNodes);
        flameChart.resetAllNode();
        assertNotNull(flameChart);
    }

    @Test
    void resetTwoNUllChildNodeAllNode() {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(null);
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(null);
        allNodes.add(node);
        allNodes.add(childNode);
        flameChart.setAllNode(allNodes);
        flameChart.resetAllNode();
        assertNotNull(flameChart);
    }

    @Test
    void resetOneNUllChildNodeAllNode() {
        ArrayList<DefaultMutableTreeNode> allNodes = new ArrayList<>();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(null);
        allNodes.add(node);
        flameChart.setAllNode(allNodes);
        flameChart.resetAllNode();
        assertNotNull(flameChart);
    }

    @Test
    void paintComponent() {
        BufferedImage image = UIUtil.createImage(new JPanel(), 1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        flameChart.paintComponent(graphics2D);
        assertNotNull(flameChart);
    }
}