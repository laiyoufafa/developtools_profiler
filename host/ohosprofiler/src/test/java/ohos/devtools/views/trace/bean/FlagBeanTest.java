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

package ohos.devtools.views.trace.bean;

import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test FlagBean class
 *
 * @date 2021/4/24 18:05
 */
class FlagBeanTest {
    /**
     * test get the ns .
     */
    @Test
    void getNs() {
        FlagBean flagBean = new FlagBean();
        flagBean.setNs(3L);
        assertEquals(3L, flagBean.getNs());
    }

    /**
     * test set the ns .
     */
    @Test
    void setNs() {
        FlagBean flagBean = new FlagBean();
        flagBean.setNs(3L);
        assertEquals(3L, flagBean.getNs());
    }

    /**
     * test get the Visible .
     */
    @Test
    void isVisible() {
        boolean visiable = true;
        FlagBean flagBean = new FlagBean();
        flagBean.setVisible(visiable);
        assertEquals(visiable, flagBean.isVisible());
    }

    /**
     * test set the Visible .
     */
    @Test
    void setVisible() {
        boolean visiable = true;
        FlagBean flagBean = new FlagBean();
        flagBean.setVisible(visiable);
        assertEquals(visiable, flagBean.isVisible());
    }

    /**
     * test get the name .
     */
    @Test
    void getName() {
        FlagBean flagBean = new FlagBean();
        flagBean.setName("name");
        assertEquals("name", flagBean.getName());
    }

    /**
     * test set the name .
     */
    @Test
    void setName() {
        FlagBean flagBean = new FlagBean();
        flagBean.setName("name");
        assertEquals("name", flagBean.getName());
    }

    /**
     * test get the time .
     */
    @Test
    void getTime() {
        FlagBean flagBean = new FlagBean();
        flagBean.setTime(3L);
        assertEquals(3L, flagBean.getTime());
    }

    /**
     * test set the time .
     */
    @Test
    void setTime() {
        FlagBean flagBean = new FlagBean();
        flagBean.setTime(3L);
        assertEquals(3L, flagBean.getTime());
    }

    /**
     * test get the time .
     */
    @Test
    void getColor() {
        Color black = Color.BLACK;
        FlagBean flagBean = new FlagBean();
        flagBean.setColor(black);
        assertEquals(black, flagBean.getColor());
    }

    /**
     * test set the time .
     */
    @Test
    void setColor() {
        Color black = Color.BLACK;
        FlagBean flagBean = new FlagBean();
        flagBean.setColor(black);
        assertEquals(black, flagBean.getColor());
    }

}