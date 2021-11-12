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

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * event dispatcher class
 *
 * @since 2021/5/24 12:00
 */
public class EventDispatcher {
    private static List<IFuncClick> clickEvents = new CopyOnWriteArrayList<>();
    private static List<ITimeRange> rangeEvents = new CopyOnWriteArrayList<>();
    private static List<IThreadRange> threadEvents = new CopyOnWriteArrayList<>();
    private static List<IFuncChange> funcChanges = new CopyOnWriteArrayList<>();

    /**
     * remove current RangeListener
     *
     * @param event event
     */
    public static void removeRangeListener(ITimeRange event) {
        rangeEvents.remove(event);
    }

    /**
     * remove current thread RangeListener
     *
     * @param event event
     */
    public static void removeThreadRangeListener(IThreadRange event) {
        threadEvents.remove(event);
    }

    /**
     * dispatcher the time Range
     *
     * @param startNS startNS
     * @param endNS endNS
     * @param scale scale
     */
    public static void dispatcherRange(long startNS, long endNS, long scale) {
        Iterator<ITimeRange> iterator = rangeEvents.iterator();
        long endTimeNs = endNS;
        while (iterator.hasNext()) {
            ITimeRange event = iterator.next();
            if (endNS == 0) {
                endTimeNs = TracePanel.endNS;
            }
            event.change(startNS, endTimeNs, scale);
        }
    }

    /**
     * dispatcherThreadRange
     *
     * @param startNS startNS
     * @param endNS endNS
     * @param threadIds threadIds
     */
    public static void dispatcherThreadRange(long startNS, long endNS, List<Integer> threadIds) {
        Iterator<IThreadRange> iterator = threadEvents.iterator();
        while (iterator.hasNext()) {
            IThreadRange event = iterator.next();
            event.change(startNS, endNS, threadIds);
        }
    }

    /**
     * dispatcherClickListener
     *
     * @param node node
     */
    public static void dispatcherClickListener(AbstractNode node) {
        Iterator<IFuncClick> iterator = clickEvents.iterator();
        while (iterator.hasNext()) {
            IFuncClick event = iterator.next();
            event.change(node);
        }
    }

    /**
     * addRangeListener
     *
     * @param listener listener
     */
    public static void addRangeListener(ITimeRange listener) {
        if (!rangeEvents.contains(listener)) {
            rangeEvents.add(listener);
        }
    }

    /**
     * addThreadRangeListener
     *
     * @param listener listener
     */
    public static void addThreadRangeListener(IThreadRange listener) {
        if (!threadEvents.contains(listener)) {
            threadEvents.add(listener);
        }
    }

    /**
     * addClickListener
     *
     * @param listener listener
     */
    public static void addClickListener(IFuncClick listener) {
        if (!clickEvents.contains(listener)) {
            clickEvents.add(listener);
        }
    }

    /**
     * clearData
     */
    public static void clearData() {
        if (Objects.nonNull(clickEvents)) {
            clickEvents.clear();
        }
        if (Objects.nonNull(rangeEvents)) {
            rangeEvents.clear();
        }
        if (Objects.nonNull(threadEvents)) {
            threadEvents.clear();
        }
        if (Objects.nonNull(funcChanges)) {
            funcChanges.clear();
        }
    }

    /**
     * add func select change listener
     *
     * @param listener select range change listener
     */
    public static void addFuncSelectChange(IFuncChange listener) {
        if (!funcChanges.contains(listener)) {
            funcChanges.add(listener);
        }
    }

    /**
     * dispatcherClickListener
     *
     * @param id id
     */
    public static void dispatcherFuncChangeListener(Integer id) {
        Iterator<IFuncChange> iterator = funcChanges.iterator();
        while (iterator.hasNext()) {
            IFuncChange event = iterator.next();
            event.change(id);
        }
    }
}
