/*
 * Copyright (C) 2021 Huawei Device Co., Ltd.
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
import hidebug from '@ohos.hidebug'
import fileio from '@ohos.fileio'
import process from '@ohos.process'

import {describe, beforeAll, beforeEach, afterEach, afterAll, it, expect} from 'deccjsunit/index'

describe("HidebugJsTest", function () {
    beforeAll(function() {
        /*
         * @tc.setup: setup invoked before all testcases
         */
         console.info('HidebugJsTest beforeAll called')
    })

    afterAll(function() {
        /*
         * @tc.teardown: teardown invoked after all testcases
         */
         console.info('HidebugJsTest afterAll called')
    })

    beforeEach(function() {
        /*
         * @tc.setup: setup invoked before each testcases
         */
         console.info('HidebugJsTest beforeEach called')
    })

    afterEach(function() {
        /*
         * @tc.teardown: teardown invoked after each testcases
         */
         console.info('HidebugJsTest afterEach called')
    })

    async function msleep(time) {
        let promise = new Promise((resolve, reject) => {
            setTimeout(() => resolve("done!"), time)
        });
        let result = await promise;
    }

    /**
     * test
     *
     * @tc.name: HidebugJsTest_001
     * @tc.desc: 检测cpuProfiler采集的cpuprofiler数据是否含有js napi callframe信息
     * @tc.type: FUNC
     * @tc.require: issueI5NXHX
     */
    it('HidebugJsTest_001', 0, function () {
        console.info("---------------------------HidebugJsTest_001----------------------------------");
        try {
            let timestamp = Date.now();
            let filename = "cpuprofiler_" + timestamp.toString();
            hidebug.startProfiling(filename);
            for (var i = 0; i < 3; i++) {
                hidebug.getSharedDirty();
            }
            hidebug.stopProfiling();
            var pid = process.pid;
            let path = "/proc/" + pid + "/root/data/storage/el2/base/files/" + filename + ".json";
            let data = fileio.readTextSync(path);
            if (data.includes("napi")) {
                expect(true).assertTrue();
            } else {
                expect(false).assertTrue();
            }
        } catch (err) {
            console.error('HidebugJsTest_001 has failed for ' + err);
            expect(false).assertTrue();
        }
    })
})