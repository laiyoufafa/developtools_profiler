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

import com.intellij.util.ui.UIUtil;
import ohos.devtools.views.perftrace.bean.PrefFunc;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AppFuncTest {

    @Test
    void getFuncName() throws NoSuchFieldException, IllegalAccessException {
        AppFunc appFunc = new Func();
        final Field field = appFunc.getClass().getDeclaredField("funcName");
        field.setAccessible(true);
        String funcName = "funcName";
        field.set(appFunc, funcName);
        assertEquals(funcName, appFunc.getFuncName());
        final Field nullField = appFunc.getClass().getDeclaredField("funcName");
        nullField.setAccessible(true);
        nullField.set(appFunc, null);
        assertNull(appFunc.getFuncName());
        final Field emptyField = appFunc.getClass().getDeclaredField("funcName");
        emptyField.setAccessible(true);
        emptyField.set(appFunc, "");
        assertEquals("", appFunc.getFuncName());
    }

    @Test
    void setFuncName() throws NoSuchFieldException, IllegalAccessException {
        AppFunc appFunc = new Func();
        appFunc.setFuncName("funcName");
        final Field field = appFunc.getClass().getDeclaredField("funcName");
        field.setAccessible(true);
        assertEquals(field.get(appFunc), "funcName");
        appFunc.setFuncName("");
        final Field emptyField = appFunc.getClass().getDeclaredField("funcName");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(appFunc), "");
        appFunc.setFuncName(null);
        final Field nullField = appFunc.getClass().getDeclaredField("funcName");
        nullField.setAccessible(true);
        assertNull(nullField.get(appFunc));
    }

    @Test
    void getTid() throws NoSuchFieldException, IllegalAccessException {
        AppFunc appFunc = new Func();
        final Field field = appFunc.getClass().getDeclaredField("tid");
        field.setAccessible(true);
        Integer tid = 0;
        field.set(appFunc, tid);
        assertEquals(tid, appFunc.getTid());
        final Field nullField = appFunc.getClass().getDeclaredField("tid");
        nullField.setAccessible(true);
        nullField.set(appFunc, null);
        assertNull(appFunc.getTid());
        final Field emptyField = appFunc.getClass().getDeclaredField("tid");
        emptyField.setAccessible(true);
        emptyField.set(appFunc, -1);
        assertEquals(-1, appFunc.getTid());
    }

    @Test
    void setTid() throws NoSuchFieldException, IllegalAccessException {
        Func appFunc = new Func();
        appFunc.setTid(0);
        final Field field = appFunc.getClass().getDeclaredField("tid");
        field.setAccessible(true);
        assertEquals(field.get(appFunc), 0);
        appFunc.setTid(-1);
        final Field emptyField = appFunc.getClass().getDeclaredField("tid");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(appFunc), -1);
        appFunc.setTid(null);
        final Field nullField = appFunc.getClass().getDeclaredField("tid");
        nullField.setAccessible(true);
        assertNull(nullField.get(appFunc));
    }

    @Test
    void getDepth() throws NoSuchFieldException, IllegalAccessException {
        AppFunc appFunc = new Func();
        final Field field = appFunc.getClass().getDeclaredField("depth");
        field.setAccessible(true);
        Integer depth = 0;
        field.set(appFunc, depth);
        assertEquals(depth, appFunc.getDepth());
        final Field nullField = appFunc.getClass().getDeclaredField("depth");
        nullField.setAccessible(true);
        nullField.set(appFunc, null);
        assertNull(appFunc.getDepth());
        final Field emptyField = appFunc.getClass().getDeclaredField("depth");
        emptyField.setAccessible(true);
        emptyField.set(appFunc, -1);
        assertEquals(-1, appFunc.getDepth());
    }

    @Test
    void setDepth() throws NoSuchFieldException, IllegalAccessException {
        Func appFunc = new Func();
        appFunc.setDepth(0);
        final Field field = appFunc.getClass().getDeclaredField("depth");
        field.setAccessible(true);
        assertEquals(field.get(appFunc), 0);
        appFunc.setDepth(-1);
        final Field emptyField = appFunc.getClass().getDeclaredField("depth");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(appFunc), -1);
        appFunc.setDepth(null);
        final Field nullField = appFunc.getClass().getDeclaredField("depth");
        nullField.setAccessible(true);
        assertNull(nullField.get(appFunc));
    }

    @Test
    void getThreadName() throws NoSuchFieldException, IllegalAccessException {
        AppFunc appFunc = new Func();
        final Field field = appFunc.getClass().getDeclaredField("threadName");
        field.setAccessible(true);
        String threadName = "threadName";
        field.set(appFunc, threadName);
        assertEquals(threadName, appFunc.getThreadName());
        final Field nullField = appFunc.getClass().getDeclaredField("threadName");
        nullField.setAccessible(true);
        nullField.set(appFunc, null);
        assertNull(appFunc.getThreadName());
        final Field emptyField = appFunc.getClass().getDeclaredField("threadName");
        emptyField.setAccessible(true);
        emptyField.set(appFunc, "");
        assertEquals("", appFunc.getThreadName());
    }

    @Test
    void setThreadName() throws NoSuchFieldException, IllegalAccessException {
        AppFunc appFunc = new Func();
        appFunc.setThreadName("threadName");
        final Field field = appFunc.getClass().getDeclaredField("threadName");
        field.setAccessible(true);
        assertEquals(field.get(appFunc), "threadName");
        appFunc.setThreadName("");
        final Field emptyField = appFunc.getClass().getDeclaredField("threadName");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(appFunc), "");
        appFunc.setThreadName(null);
        final Field nullField = appFunc.getClass().getDeclaredField("threadName");
        nullField.setAccessible(true);
        assertNull(nullField.get(appFunc));
    }

    @Test
    void getStartTs() throws NoSuchFieldException, IllegalAccessException {
        AppFunc appFunc = new Func();
        final Field field = appFunc.getClass().getDeclaredField("startTs");
        field.setAccessible(true);
        long startTs = 0L;
        field.set(appFunc, startTs);
        assertEquals(startTs, appFunc.getStartTs());
        final Field nullField = appFunc.getClass().getDeclaredField("startTs");
        nullField.setAccessible(true);
        nullField.set(appFunc, 1L);
        assertEquals(1L, appFunc.getStartTs());
        final Field emptyField = appFunc.getClass().getDeclaredField("startTs");
        emptyField.setAccessible(true);
        emptyField.set(appFunc, -1);
        assertEquals(-1, appFunc.getStartTs());
    }

    @Test
    void setStartTs() throws NoSuchFieldException, IllegalAccessException {
        AppFunc appFunc = new Func();
        long startTs = 0L;
        appFunc.setStartTs(startTs);
        final Field field = appFunc.getClass().getDeclaredField("startTs");
        field.setAccessible(true);
        assertEquals(field.get(appFunc), startTs);
        appFunc.setStartTs(1L);
        final Field emptyField = appFunc.getClass().getDeclaredField("startTs");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(appFunc), 1L);
        appFunc.setStartTs(-1L);
        final Field nullField = appFunc.getClass().getDeclaredField("startTs");
        nullField.setAccessible(true);
        assertEquals(nullField.get(appFunc), -1L);
    }

    @Test
    void getDur() throws NoSuchFieldException, IllegalAccessException {
        AppFunc appFunc = new Func();
        final Field field = appFunc.getClass().getDeclaredField("dur");
        field.setAccessible(true);
        long dur = 0L;
        field.set(appFunc, dur);
        assertEquals(dur, appFunc.getDur());
        final Field nullField = appFunc.getClass().getDeclaredField("dur");
        nullField.setAccessible(true);
        nullField.set(appFunc, 1L);
        assertEquals(1L, appFunc.getDur());
        final Field emptyField = appFunc.getClass().getDeclaredField("dur");
        emptyField.setAccessible(true);
        emptyField.set(appFunc, -1);
        assertEquals(-1, appFunc.getDur());
    }

    @Test
    void setDur() throws NoSuchFieldException, IllegalAccessException {
        AppFunc appFunc = new Func();
        long dur = 0L;
        appFunc.setDur(dur);
        final Field field = appFunc.getClass().getDeclaredField("dur");
        field.setAccessible(true);
        assertEquals(field.get(appFunc), dur);
        appFunc.setDur(1L);
        final Field emptyField = appFunc.getClass().getDeclaredField("dur");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(appFunc), 0L);
        appFunc.setDur(-1L);
        final Field nullField = appFunc.getClass().getDeclaredField("dur");
        nullField.setAccessible(true);
        assertEquals(nullField.get(appFunc), 0L);
    }

    @Test
    void getBloodId() throws NoSuchFieldException, IllegalAccessException {
        AppFunc appFunc = new PrefFunc();
        String bloodId = "bloodId";
        appFunc.setBloodId(bloodId);
        final Field field = appFunc.getClass().getSuperclass().getDeclaredField("bloodId");
        field.setAccessible(true);
        assertEquals(bloodId, appFunc.getBloodId());
        final Field nullField = appFunc.getClass().getSuperclass().getDeclaredField("bloodId");
        nullField.setAccessible(true);
        nullField.set(appFunc, null);
        assertNull(appFunc.getBloodId());
        final Field emptyField = appFunc.getClass().getSuperclass().getDeclaredField("bloodId");
        emptyField.setAccessible(true);
        emptyField.set(appFunc, "");
        assertEquals("", appFunc.getBloodId());
    }

    @Test
    void setBloodId() throws NoSuchFieldException, IllegalAccessException {
        AppFunc appFunc = new Func();
        appFunc.setBloodId("bloodId");
        final Field field = appFunc.getClass().getSuperclass().getDeclaredField("bloodId");
        field.setAccessible(true);
        assertEquals(field.get(appFunc), "bloodId");
        appFunc.setBloodId("");
        final Field emptyField = appFunc.getClass().getSuperclass().getDeclaredField("bloodId");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(appFunc), "");
        appFunc.setBloodId(null);
        final Field nullField = appFunc.getClass().getSuperclass().getDeclaredField("bloodId");
        nullField.setAccessible(true);
        assertNull(nullField.get(appFunc));
    }

    @Test
    void getParentBloodId() throws NoSuchFieldException, IllegalAccessException {
        AppFunc appFunc = new PrefFunc();
        String parentBloodId = "parentBloodId";
        appFunc.setParentBloodId(parentBloodId);
        final Field field = appFunc.getClass().getSuperclass().getDeclaredField("parentBloodId");
        field.setAccessible(true);
        assertEquals(parentBloodId, appFunc.getParentBloodId());
        final Field nullField = appFunc.getClass().getSuperclass().getDeclaredField("parentBloodId");
        nullField.setAccessible(true);
        nullField.set(appFunc, null);
        assertNull(appFunc.getParentBloodId());
        final Field emptyField = appFunc.getClass().getSuperclass().getDeclaredField("parentBloodId");
        emptyField.setAccessible(true);
        emptyField.set(appFunc, "");
        assertEquals("", appFunc.getParentBloodId());
    }

    @Test
    void setParentBloodId() throws NoSuchFieldException, IllegalAccessException {
        AppFunc appFunc = new Func();
        appFunc.setParentBloodId("parentBloodId");
        final Field field = appFunc.getClass().getSuperclass().getDeclaredField("parentBloodId");
        field.setAccessible(true);
        assertEquals(field.get(appFunc), "");
        appFunc.setParentBloodId("");
        final Field emptyField = appFunc.getClass().getSuperclass().getDeclaredField("parentBloodId");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(appFunc), "");
        appFunc.setParentBloodId(null);
        final Field nullField = appFunc.getClass().getSuperclass().getDeclaredField("parentBloodId");
        nullField.setAccessible(true);
        assertEquals(nullField.get(appFunc), "");
    }

    @Test
    void createBloodId() throws NoSuchFieldException, IllegalAccessException {
        AppFunc appFunc = new Func();
        appFunc.createBloodId();
        final Field field = appFunc.getClass().getSuperclass().getDeclaredField("parentBloodId");
        field.setAccessible(true);
        assertNotNull(field.get(appFunc));
        final Field bloodField = appFunc.getClass().getSuperclass().getDeclaredField("bloodId");
        bloodField.setAccessible(true);
        assertNotNull(bloodField.get(appFunc));
    }

    @Test
    void draw() {
        AppFunc appFunc = new Func();
        BufferedImage image = UIUtil.createImage(new JPanel(), 1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        appFunc.draw(graphics2D);
        assertNotNull(appFunc);
    }

    @Test
    void getStringList() {
        AppFunc appFunc = new Func();
        assertNotNull(appFunc.getStringList(""));
    }
}