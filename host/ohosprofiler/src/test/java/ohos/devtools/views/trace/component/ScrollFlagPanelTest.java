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

import ohos.devtools.views.trace.bean.FlagBean;
import ohos.devtools.views.trace.listener.IFlagListener;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test ScrollFlagPanel class .
 *
 * @since 2021/4/24 18:03
 */
class ScrollFlagPanelTest {
    /**
     * test set the BeanData .
     */
    @Test
    void setData() {
        FlagBean flagBean = new FlagBean();
        ScrollFlagPanel scrollFlagPanel = new ScrollFlagPanel(flagBean);
        scrollFlagPanel.setData(flagBean);
        final Field field;
        try {
            field = scrollFlagPanel.getClass().getDeclaredField("flag");
            field.setAccessible(true);
            assertEquals(flagBean, field.get(scrollFlagPanel));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * test set the listener .
     */
    @Test
    void setFlagListener() {
        FlagBean flagBean = new FlagBean();
        ScrollFlagPanel scrollFlagPanel = new ScrollFlagPanel(flagBean);
        IFlagListener listener = new IFlagListener() {
            @Override
            public void flagRemove(FlagBean flag) {
            }

            @Override
            public void flagChange(FlagBean flag) {
            }
        };
        scrollFlagPanel.setFlagListener(listener);
        final Field field;
        try {
            field = scrollFlagPanel.getClass().getDeclaredField("flagListener");
            field.setAccessible(true);
            assertEquals(listener, field.get(scrollFlagPanel));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}