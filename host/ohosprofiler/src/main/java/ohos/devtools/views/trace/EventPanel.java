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

import com.intellij.ui.components.JBPanel;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * EventPanel
 *
 * @date: 2021/5/24 12:22
 */
public abstract class EventPanel extends JBPanel implements ITimeRange, IThreadRange, AncestorListener {
    /**
     * EventPanel
     */
    public EventPanel() {
        addAncestorListener(this);
    }

    @Override
    public void ancestorAdded(AncestorEvent event) {
        EventDispatcher.addRangeListener(this);
        EventDispatcher.addThreadRangeListener(this);
        EventDispatcher.dispatcherRange(TracePanel.startNS, TracePanel.endNS, 50L);
        if (TracePanel.rangeStartNS != null && TracePanel.rangeEndNS != null) {
            EventDispatcher.dispatcherThreadRange(TracePanel.rangeStartNS, TracePanel.rangeEndNS,
                TracePanel.currentSelectThreadIds);
        }
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {
        EventDispatcher.removeRangeListener(this);
        EventDispatcher.removeThreadRangeListener(this);
    }

    @Override
    public void ancestorMoved(AncestorEvent event) {
    }
}
