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

package ohos;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.StatusRuntimeException;
import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.transport.grpc.HiProfilerClient;
import ohos.devtools.datasources.transport.grpc.ProfilerClient;
import ohos.devtools.datasources.transport.grpc.ProfilerServiceHelper;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.transport.grpc.service.StreamPluginResult;
import ohos.devtools.datasources.utils.common.util.CommonUtil;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.service.MultiDeviceManager;
import ohos.devtools.datasources.utils.plugin.IPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.AnalysisType;
import ohos.devtools.datasources.utils.plugin.service.PlugManager;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.pluginconfig.AgentConfig;
import ohos.devtools.pluginconfig.BytraceConfig;
import ohos.devtools.pluginconfig.CpuConfig;
import ohos.devtools.pluginconfig.DataCheckConfig;
import ohos.devtools.pluginconfig.DiskIoConfig;
import ohos.devtools.pluginconfig.FtraceConfig;
import ohos.devtools.pluginconfig.HilogConfig;
import ohos.devtools.pluginconfig.MemoryConfig;
import ohos.devtools.pluginconfig.ProcessConfig;
import ohos.devtools.pluginconfig.UserConfig;
import ohos.devtools.views.user.UserManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Data Check Consumer Test
 *
 * @since 2021/2/1 9:31
 */
public class DataCheckConsumerTest {
    private Long session;
    private DeviceIPPortInfo deviceIPPortInfo;

    /**
     * init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_utils_DataPoller_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: SR000FK61J
     */
    @Before
    public void init() {
        DataBaseApi.getInstance().initDataSourceManager();
        PlugManager.getInstance().unzipStdDevelopTools();
        List<Class<? extends IPluginConfig>> plugConfigList = new ArrayList();
        plugConfigList.add(ProcessConfig.class);
        plugConfigList.add(DataCheckConfig.class);
        plugConfigList.add(AgentConfig.class);
        plugConfigList.add(BytraceConfig.class);
        plugConfigList.add(FtraceConfig.class);
        plugConfigList.add(CpuConfig.class);
        plugConfigList.add(HilogConfig.class);
        if (Objects.nonNull(UserManager.getInstance().getSdkImpl())) {
            plugConfigList.add(UserConfig.class);
        }
        plugConfigList.add(DiskIoConfig.class);
        plugConfigList.add(MemoryConfig.class);
        PlugManager.getInstance().loadingPlugs(plugConfigList);
        MultiDeviceManager.getInstance().start();
        while (true) {
            if (MultiDeviceManager.getInstance().getOnlineDeviceInfoList().size() > 0) {
                break;
            }
        }
        deviceIPPortInfo = MultiDeviceManager.getInstance().getOnlineDeviceInfoList().get(0);
        ProcessInfo processInfo = new ProcessInfo();
        processInfo.setProcessId(1);
        processInfo.setProcessName("");
        processInfo.setDeviceId(deviceIPPortInfo.getDeviceID());
        session =
            SessionManager.getInstance().createSession(deviceIPPortInfo, processInfo, AnalysisType.APPLICATION_TYPE);
        SessionManager.getInstance().startSession(session, false);
    }

    /**
     * data Check Handle Test
     *
     * @tc.name: dataCheckHandleTest
     * @tc.number: OHOS_JAVA_utils_DataPoller_dataCheckHandleTest_0001
     * @tc.desc: data Check Handle Test
     * @tc.type: functional testing
     * @tc.require: SR000FK61J
     */
    @Test
    public void dataCheckHandleTest() {
        ProfilerClient profilerClient = HiProfilerClient.getInstance()
            .getProfilerClient(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
        SessionInfo sessionInfo = SessionManager.getInstance().getSessionInfo(session);
        ProfilerServiceTypes.FetchDataRequest request =
            ProfilerServiceHelper.fetchDataRequest(CommonUtil.getRequestId(), sessionInfo.getSessionId(), null);
        Iterator<ProfilerServiceTypes.FetchDataResponse> response = null;
        try {
            boolean beakPoint = false;
            response = profilerClient.fetchData(request);
            AtomicLong compareValue = new AtomicLong(0L);
            while (!beakPoint && response.hasNext()) {
                ProfilerServiceTypes.FetchDataResponse fetchDataResponse = response.next();
                List<CommonTypes.ProfilerPluginData> lists = fetchDataResponse.getPluginDataList();
                for (CommonTypes.ProfilerPluginData pluginData : lists) {
                    if (compareValue.get() == 49) {
                        beakPoint = true;
                        break;
                    }
                    String name = pluginData.getName();
                    if (name.equals("/data/local/tmp/libstreamplugin.z.so")) {
                        compareValue.set(dataCheckHandle(pluginData, compareValue.get()));
                    }
                }
            }
            SessionManager.getInstance().stopAndDestoryOperation(session);
        } catch (StatusRuntimeException exception) {
            Assert.assertTrue(false);
        } finally {
            SessionManager.getInstance().stopAllSession();
        }
    }

    private long dataCheckHandle(CommonTypes.ProfilerPluginData dataCheck, Long compareValue) {
        ByteString data = dataCheck.getData();
        StreamPluginResult.StreamData.Builder streamDataBuilder = StreamPluginResult.StreamData.newBuilder();
        StreamPluginResult.StreamData streamData = null;
        try {
            streamData = streamDataBuilder.mergeFrom(data).build();
        } catch (InvalidProtocolBufferException exe) {
            Assert.assertTrue(false);
            return compareValue;
        }
        Long intData = streamData.getIntdata();
        String stringData = streamData.getStringdata();
        Assert.assertEquals(String.valueOf(intData), stringData);
        Assert.assertEquals(intData, compareValue);
        return compareValue + 1;
    }
}
