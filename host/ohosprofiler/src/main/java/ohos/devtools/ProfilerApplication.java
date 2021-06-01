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

package ohos.devtools;

import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.utils.device.service.MultiDeviceManager;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.views.layout.swing.LayoutView;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Description IDE Application startup class
 * @Date 2021/2/9 14:35
 **/
public class ProfilerApplication {
    private ProfilerApplication() {
    }

    private static final Logger LOGGER = LogManager.getLogger(ProfilerApplication.class);

    /**
     * main
     *
     * @param args args
     */
    public static void main(String[] args) {
        // print ohos Profiler Start the system used
        LOGGER.error("ohos Profiler Start OS is {}", System.getProperty("os.name"));
        // Application initialization Step1 Initialize the data center
        ProfilerLogManager.getSingleton().updateLogLevel(Level.ERROR);
        LOGGER.info("start init {}", "init");
        DataBaseApi apo = DataBaseApi.getInstance();
        LOGGER.info("end init {}", "init");
        apo.initDataSourceManager();
        // Start the device discovery service
        QuartzManager instance = QuartzManager.getInstance();
        String name = MultiDeviceManager.class.getName();
        MultiDeviceManager manager = MultiDeviceManager.getInstance();
        instance.addExecutor(name, manager);
        instance.startExecutor(name, QuartzManager.DELAY, QuartzManager.PERIOD);
        // Launch the main page
        LayoutView.init();
    }
}
