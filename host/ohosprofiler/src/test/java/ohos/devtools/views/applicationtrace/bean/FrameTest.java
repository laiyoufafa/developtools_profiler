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

package ohos.devtools.views.applicationtrace.bean;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FrameTest {

    @Test
    void getStartNs() throws NoSuchFieldException, IllegalAccessException {
        Func func = new Func();
        Frame frame = new Frame(func);
        final Field field = frame.getClass().getDeclaredField("startNs");
        field.setAccessible(true);
        field.set(frame, 1L);
        assertEquals(frame.getStartNs(), 1L);
        final Field normalField = frame.getClass().getDeclaredField("startNs");
        normalField.setAccessible(true);
        normalField.set(frame, 0L);
        assertEquals(frame.getStartNs(), 0L);
        final Field negativeField = frame.getClass().getDeclaredField("startNs");
        negativeField.setAccessible(true);
        negativeField.set(frame, -1L);
        assertEquals(frame.getStartNs(), -1L);
    }

    @Test
    void setStartNs() throws NoSuchFieldException, IllegalAccessException {
        Func func = new Func();
        Frame frame = new Frame(func);
        final Field field = frame.getClass().getDeclaredField("startNs");
        field.setAccessible(true);
        frame.setStartNs(1L);
        assertEquals(field.get(frame), 1L);
        final Field zeroField = frame.getClass().getDeclaredField("startNs");
        zeroField.setAccessible(true);
        frame.setStartNs(0L);
        assertEquals(zeroField.get(frame), 0L);
        final Field negativeField = frame.getClass().getDeclaredField("startNs");
        negativeField.setAccessible(true);
        frame.setStartNs(-1L);
        assertEquals(negativeField.get(frame), -1L);
    }

    @Test
    void getDur() throws NoSuchFieldException, IllegalAccessException {
        Func func = new Func();
        Frame frame = new Frame(func);
        final Field field = frame.getClass().getDeclaredField("dur");
        field.setAccessible(true);
        field.set(frame, 1L);
        assertEquals(frame.getDur(), 1L);
        final Field normalField = frame.getClass().getDeclaredField("dur");
        normalField.setAccessible(true);
        normalField.set(frame, 0L);
        assertEquals(frame.getDur(), 0L);
        final Field negativeField = frame.getClass().getDeclaredField("dur");
        negativeField.setAccessible(true);
        negativeField.set(frame, -1L);
        assertEquals(frame.getDur(), -1L);
    }

    @Test
    void setDur() throws NoSuchFieldException, IllegalAccessException {
        Func func = new Func();
        Frame frame = new Frame(func);
        final Field field = frame.getClass().getDeclaredField("dur");
        field.setAccessible(true);
        frame.setDur(1L);
        assertEquals(field.get(frame), 1L);
        final Field zeroField = frame.getClass().getDeclaredField("dur");
        zeroField.setAccessible(true);
        frame.setDur(0L);
        assertEquals(zeroField.get(frame), 0L);
        final Field negativeField = frame.getClass().getDeclaredField("dur");
        negativeField.setAccessible(true);
        frame.setDur(-1L);
        assertEquals(negativeField.get(frame), -1L);
    }

    @Test
    void getMainThreadCpu() throws NoSuchFieldException, IllegalAccessException {
        Func func = new Func();
        Frame frame = new Frame(func);
        final Field field = frame.getClass().getDeclaredField("mainThreadCpu");
        field.setAccessible(true);
        field.set(frame, 1L);
        assertEquals(frame.getMainThreadCpu(), 1L);
        final Field normalField = frame.getClass().getDeclaredField("mainThreadCpu");
        normalField.setAccessible(true);
        normalField.set(frame, 0L);
        assertEquals(frame.getMainThreadCpu(), 0L);
        final Field negativeField = frame.getClass().getDeclaredField("mainThreadCpu");
        negativeField.setAccessible(true);
        negativeField.set(frame, -1L);
        assertEquals(frame.getMainThreadCpu(), -1L);
    }

    @Test
    void setMainThreadCpu() throws NoSuchFieldException, IllegalAccessException {
        Func func = new Func();
        Frame frame = new Frame(func);
        final Field field = frame.getClass().getDeclaredField("mainThreadCpu");
        field.setAccessible(true);
        frame.setMainThreadCpu(1L);
        assertEquals(field.get(frame), 1L);
        final Field zeroField = frame.getClass().getDeclaredField("mainThreadCpu");
        zeroField.setAccessible(true);
        frame.setMainThreadCpu(0L);
        assertEquals(zeroField.get(frame), 0L);
        final Field negativeField = frame.getClass().getDeclaredField("mainThreadCpu");
        negativeField.setAccessible(true);
        frame.setMainThreadCpu(-1L);
        assertEquals(negativeField.get(frame), -1L);
    }

    @Test
    void getMainThreadWall() throws NoSuchFieldException, IllegalAccessException {
        Func func = new Func();
        Frame frame = new Frame(func);
        final Field field = frame.getClass().getDeclaredField("mainThreadWall");
        field.setAccessible(true);
        field.set(frame, 1L);
        assertEquals(frame.getMainThreadWall(), 1L);
        final Field normalField = frame.getClass().getDeclaredField("mainThreadWall");
        normalField.setAccessible(true);
        normalField.set(frame, 0L);
        assertEquals(frame.getMainThreadWall(), 0L);
        final Field negativeField = frame.getClass().getDeclaredField("mainThreadWall");
        negativeField.setAccessible(true);
        negativeField.set(frame, -1L);
        assertEquals(frame.getMainThreadWall(), -1L);
    }

    @Test
    void setMainThreadWall() throws NoSuchFieldException, IllegalAccessException {
        Func func = new Func();
        Frame frame = new Frame(func);
        final Field field = frame.getClass().getDeclaredField("mainThreadCpu");
        field.setAccessible(true);
        frame.setMainThreadCpu(1L);
        assertEquals(field.get(frame), 1L);
        final Field zeroField = frame.getClass().getDeclaredField("mainThreadCpu");
        zeroField.setAccessible(true);
        frame.setMainThreadCpu(0L);
        assertEquals(zeroField.get(frame), 0L);
        final Field negativeField = frame.getClass().getDeclaredField("mainThreadCpu");
        negativeField.setAccessible(true);
        frame.setMainThreadCpu(-1L);
        assertEquals(negativeField.get(frame), -1L);
    }

    @Test
    void getRenderThreadWall() throws NoSuchFieldException, IllegalAccessException {
        Func func = new Func();
        Frame frame = new Frame(func);
        final Field field = frame.getClass().getDeclaredField("renderThreadWall");
        field.setAccessible(true);
        field.set(frame, 1L);
        assertEquals(frame.getRenderThreadWall(), 1L);
        final Field normalField = frame.getClass().getDeclaredField("renderThreadWall");
        normalField.setAccessible(true);
        normalField.set(frame, 0L);
        assertEquals(frame.getRenderThreadWall(), 0L);
        final Field negativeField = frame.getClass().getDeclaredField("renderThreadWall");
        negativeField.setAccessible(true);
        negativeField.set(frame, -1L);
        assertEquals(frame.getRenderThreadWall(), -1L);
    }

    @Test
    void setRenderThreadWall() throws NoSuchFieldException, IllegalAccessException {
        Func func = new Func();
        Frame frame = new Frame(func);
        final Field field = frame.getClass().getDeclaredField("renderThreadWall");
        field.setAccessible(true);
        frame.setRenderThreadWall(1L);
        assertEquals(field.get(frame), 1L);
        final Field zeroField = frame.getClass().getDeclaredField("renderThreadWall");
        zeroField.setAccessible(true);
        frame.setRenderThreadWall(0L);
        assertEquals(zeroField.get(frame), 0L);
        final Field negativeField = frame.getClass().getDeclaredField("renderThreadWall");
        negativeField.setAccessible(true);
        frame.setRenderThreadWall(-1L);
        assertEquals(negativeField.get(frame), -1L);
    }

    @Test
    void getRenderThreadCpu() throws NoSuchFieldException, IllegalAccessException {
        Func func = new Func();
        Frame frame = new Frame(func);
        final Field field = frame.getClass().getDeclaredField("renderThreadCpu");
        field.setAccessible(true);
        field.set(frame, 1L);
        assertEquals(frame.getRenderThreadCpu(), 1L);
        final Field normalField = frame.getClass().getDeclaredField("renderThreadCpu");
        normalField.setAccessible(true);
        normalField.set(frame, 0L);
        assertEquals(frame.getRenderThreadCpu(), 0L);
        final Field negativeField = frame.getClass().getDeclaredField("renderThreadCpu");
        negativeField.setAccessible(true);
        negativeField.set(frame, -1L);
        assertEquals(frame.getRenderThreadCpu(), -1L);
    }

    @Test
    void setRenderThreadCpu() throws NoSuchFieldException, IllegalAccessException {
        Func func = new Func();
        Frame frame = new Frame(func);
        final Field field = frame.getClass().getDeclaredField("renderThreadCpu");
        field.setAccessible(true);
        frame.setRenderThreadCpu(1L);
        assertEquals(field.get(frame), 1L);
        final Field zeroField = frame.getClass().getDeclaredField("renderThreadCpu");
        zeroField.setAccessible(true);
        frame.setRenderThreadCpu(0L);
        assertEquals(zeroField.get(frame), 0L);
        final Field negativeField = frame.getClass().getDeclaredField("renderThreadCpu");
        negativeField.setAccessible(true);
        frame.setRenderThreadCpu(-1L);
        assertEquals(negativeField.get(frame), -1L);
    }

    @Test
    void getTotal() throws NoSuchFieldException, IllegalAccessException {
        Func func = new Func();
        Frame frame = new Frame(func);
        final Field field = frame.getClass().getDeclaredField("total");
        field.setAccessible(true);
        field.set(frame, 1L);
        assertEquals(frame.getTotal(), 1L);
        final Field normalField = frame.getClass().getDeclaredField("total");
        normalField.setAccessible(true);
        normalField.set(frame, 0L);
        assertEquals(frame.getTotal(), 0L);
        final Field negativeField = frame.getClass().getDeclaredField("total");
        negativeField.setAccessible(true);
        negativeField.set(frame, -1L);
        assertEquals(frame.getTotal(), -1L);
    }

    @Test
    void setTotal() throws NoSuchFieldException, IllegalAccessException {
        Func func = new Func();
        Frame frame = new Frame(func);
        final Field field = frame.getClass().getDeclaredField("total");
        field.setAccessible(true);
        frame.setTotal(1L);
        assertEquals(field.get(frame), 1L);
        final Field zeroField = frame.getClass().getDeclaredField("total");
        zeroField.setAccessible(true);
        frame.setTotal(0L);
        assertEquals(zeroField.get(frame), 0L);
        final Field negativeField = frame.getClass().getDeclaredField("total");
        negativeField.setAccessible(true);
        frame.setTotal(-1L);
        assertEquals(negativeField.get(frame), -1L);
    }

}