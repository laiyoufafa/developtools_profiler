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

import ohos.devtools.views.trace.AbstractNode;
import ohos.devtools.views.trace.DField;
import ohos.devtools.views.trace.util.Utils;

import java.awt.Graphics2D;
import java.util.List;

/**
 * app function abstract class
 */
public abstract class AppFunc extends AbstractNode {
    /**
     * fun name
     */
    @DField(name = "funName")
    protected String funcName = "";

    /**
     * thread id
     */
    @DField(name = "tid")
    protected Integer tid;

    /**
     * depth
     */
    @DField(name = "depth")
    protected Integer depth = 0;

    /**
     * thread name
     */
    @DField(name = "threadName")
    protected String threadName = "";

    /**
     * start ts
     */
    @DField(name = "startTs")
    protected long startTs;

    /**
     * duration
     */
    @DField(name = "dur")
    protected long dur;

    /**
     * end ts
     */
    protected long endTs = 0;

    /**
     * blood id
     */
    protected String bloodId = "";

    /**
     * parent blood id
     */
    protected String parentBloodId = "";

    /**
     * Gets the value of funcName .
     *
     * @return the value of funcName .
     */
    public String getFuncName() {
        return funcName;
    }

    /**
     * Sets the funcName .
     * <p>You can use getFuncName() to get the value of funcName.</p>
     *
     * @param param .
     */
    public void setFuncName(final String param) {
        this.funcName = param;
    }

    /**
     * Gets the value of tid .
     *
     * @return the value of tid .
     */
    public Integer getTid() {
        return tid;
    }

    /**
     * Sets the tid .
     * <p>You can use getTid() to get the value of tid.</p>
     *
     * @param param .
     */
    public void setTid(final Integer param) {
        this.tid = param;
    }

    /**
     * Gets the value of depth .
     *
     * @return the value of depth .
     */
    public Integer getDepth() {
        return depth;
    }

    /**
     * Sets the depth .
     * <p>You can use getDepth() to get the value of depth.</p>
     *
     * @param param .
     */
    public void setDepth(final Integer param) {
        this.depth = param;
    }

    /**
     * Gets the value of threadName .
     *
     * @return the value of threadName .
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * Sets the threadName .
     * <p>You can use getThreadName() to get the value of threadName.</p>
     *
     * @param param .
     */
    public void setThreadName(final String param) {
        this.threadName = param;
    }

    /**
     * Gets the value of startTs .
     *
     * @return the value of startTs .
     */
    public long getStartTs() {
        return startTs;
    }

    /**
     * Sets the startTs .
     * <p>You can use getStartTs() to get the value of startTs.</p>
     *
     * @param param .
     */
    public void setStartTs(final long param) {
        this.startTs = param;
    }

    /**
     * Gets the value of dur .
     *
     * @return the value of dur .
     */
    public long getDur() {
        return dur;
    }

    /**
     * Sets the dur .
     * <p>You can use getDur() to get the value of dur.</p>
     *
     * @param param .
     */
    public void setDur(final Long param) {
        this.dur = param;
    }

    /**
     * Gets the value of endTs .
     *
     * @return the value of endTs .
     */
    public long getEndTs() {
        return endTs;
    }

    /**
     * Sets the endTs .
     * <p>You can use getEndTs() to get the value of endTs.</p>
     *
     * @param param .
     */
    public void setEndTs(final long param) {
        this.endTs = param;
    }

    /**
     * Gets the value of bloodId .
     *
     * @return the value of bloodId .
     */
    public String getBloodId() {
        return bloodId;
    }

    /**
     * Sets the bloodId .
     * <p>You can use getBloodId() to get the value of bloodId.</p>
     *
     * @param param .
     */
    public void setBloodId(final String param) {
        this.bloodId = param;
    }

    /**
     * Gets the value of parentBloodId .
     *
     * @return the value of parentBloodId .
     */
    public String getParentBloodId() {
        return parentBloodId;
    }

    /**
     * Sets the parentBloodId .
     * <p>You can use getParentBloodId() to get the value of parentBloodId.</p>
     *
     * @param param .
     */
    public void setParentBloodId(final String param) {
        this.parentBloodId = param;
    }

    /**
     * create StackId by parentStackId、funcName and depth
     */
    public void createBloodId() {
        if (depth == 0) {
            bloodId = Utils.md5String(threadName + funcName + depth);
            parentBloodId = Utils.md5String(threadName);
        } else {
            bloodId = Utils.md5String(parentBloodId + funcName + depth);
        }
    }

    @Override
    public void draw(Graphics2D paint) {
    }

    @Override
    public List<String> getStringList(String time) {
        return null;
    }

}
