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

import ohos.devtools.views.trace.bean.WakeupBean;
import ohos.devtools.views.trace.listener.IScrollSliceLinkListener;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test ScrollSlicePanel class .
 *
 * @date: 2021/4/24 18:03
 */
class ScrollSlicePanelTest {
    /**
     * test set the Data .
     */
    @Test
    void setData() {
        ScrollSlicePanel scrollSlicePanel = new ScrollSlicePanel();
        List<ScrollSlicePanel.SliceData> dataSource = new ArrayList<>();
        WakeupBean wakeupBean = new WakeupBean();
        scrollSlicePanel.setData("title", dataSource, wakeupBean);
        final Field dataSourceField;
        final Field wakeupBeanField;
        try {
            dataSourceField = scrollSlicePanel.getClass().getDeclaredField("dataSource");
            wakeupBeanField = scrollSlicePanel.getClass().getDeclaredField("wakeupBean");
            dataSourceField.setAccessible(true);
            wakeupBeanField.setAccessible(true);
            assertEquals(dataSource, dataSourceField.get(scrollSlicePanel));
            assertEquals(wakeupBean, wakeupBeanField.get(scrollSlicePanel));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * test set the ScrollSliceLinkListener .
     */
    @Test
    void setScrollSliceLinkListener() {
        ScrollSlicePanel scrollSlicePanel = new ScrollSlicePanel();
        IScrollSliceLinkListener listener = new IScrollSliceLinkListener() {
            @Override
            public void linkClick(Object bean) {
            }
        };
        scrollSlicePanel.setScrollSliceLinkListener(listener);
        final Field field;
        try {
            field = scrollSlicePanel.getClass().getDeclaredField("listener");
            field.setAccessible(true);
            assertEquals(listener, field.get(scrollSlicePanel));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * test create the SliceData .
     */
    @Test
    void createSliceData() {
        String key = "key";
        String value = "value";
        boolean linkable = true;
        ScrollSlicePanel.SliceData sliceData = ScrollSlicePanel.createSliceData(key, value, linkable);
        assertEquals(key, sliceData.key);
        assertEquals(value, sliceData.value);
        assertEquals(linkable, sliceData.linkable);
    }
}