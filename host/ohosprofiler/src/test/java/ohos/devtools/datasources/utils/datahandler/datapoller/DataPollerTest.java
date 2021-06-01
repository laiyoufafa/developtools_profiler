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

package ohos.devtools.datasources.utils.datahandler.datapoller;

import com.alibaba.fastjson.JSONObject;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.grpc.util.MutableHandlerRegistry;
import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.databases.datatable.MemoryTable;
import ohos.devtools.datasources.transport.grpc.HiProfilerClient;
import ohos.devtools.datasources.transport.grpc.MockProfilerServiceImplBase;
import ohos.devtools.datasources.transport.grpc.ProfilerClient;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginResult;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.utils.common.Constant;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static ohos.devtools.datasources.utils.common.Constant.MEMORY_PLUG;

/**
 * DataPollerTest
 *
 * @Description DataPollerTest
 * @Date 2021/4/3 15:02
 **/
public class DataPollerTest {
    /**
     * requestId
     */
    private static volatile Integer requestId = 1;

    /**
     * grpcCleanup
     */
    public GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    /**
     * serverName
     */
    private String serverName;

    /**
     * SessionManager
     */
    private SessionManager session;

    /**
     * DeviceIPPortInfo
     */
    private DeviceIPPortInfo device;

    /**
     * ProcessInfo
     */
    private ProcessInfo process;

    /**
     * JSONObject
     */
    private JSONObject jsonObject;

    private MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();

    /**
     * init
     *
     * @throws IOException IOException
     */
    @Before
    public void init() throws IOException {
        session = SessionManager.getInstance();
        session.setDevelopMode(true);
        DataBaseApi apo = DataBaseApi.getInstance();
        apo.initDataSourceManager();
        jsonObject = new JSONObject();
        JSONObject memoryObject = new JSONObject();
        memoryObject.put("Select All", true);
        memoryObject.put("Java", true);
        memoryObject.put("Native", true);
        memoryObject.put("Graphics", true);
        memoryObject.put("Stack", true);
        memoryObject.put("Code", true);
        memoryObject.put("Others", true);
        jsonObject.put("Memory", memoryObject);
        device = new DeviceIPPortInfo();
        device.setIp("");
        device.setPort(11007);
        device.setForwardPort(11007);
        process = new ProcessInfo();
        process.setProcessId(111);
        process.setProcessName("process");
        serverName = InProcessServerBuilder.generateName();
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).fallbackHandlerRegistry(serviceRegistry).directExecutor().build()
                .start());
    }

    /**
     * functional testing dataPoller
     *
     * @tc.name: dataPoller
     * @tc.number: OHOS_JAVA_utils_DataPoller_dataPoller_0001
     * @tc.desc: dataPoller
     * @tc.type: functional testing
     * @tc.require: SR000FK61J
     */
    @Test
    public void dataPollerTest() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            /**
             * init createSession
             *
             * @param request request
             * @param responseObserver responseObserver
             */
            @Override
            public void createSession(ProfilerServiceTypes.CreateSessionRequest request,
                StreamObserver<ProfilerServiceTypes.CreateSessionResponse> responseObserver) {
                ProfilerServiceTypes.CreateSessionResponse reply =
                    ProfilerServiceTypes.CreateSessionResponse.newBuilder().setSessionId(1).setStatus(0).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }

            /**
             * init getCapabilities
             *
             * @param request request
             * @param responseObserver responseObserver
             */
            @Override
            public void getCapabilities(ProfilerServiceTypes.GetCapabilitiesRequest request,
                StreamObserver<ProfilerServiceTypes.GetCapabilitiesResponse> responseObserver) {
                ProfilerServiceTypes.ProfilerPluginCapability pluginCapability =
                    ProfilerServiceTypes.ProfilerPluginCapability.newBuilder(
                        ProfilerServiceTypes.ProfilerPluginCapability.newBuilder()
                            .setName("/data/local/tmp/libmemdataplugin.z.so")
                            .setPath("/data/local/tmp/libmemdataplugin.z.so").build()).build();
                ProfilerServiceTypes.GetCapabilitiesResponse reply =
                    ProfilerServiceTypes.GetCapabilitiesResponse.newBuilder().addCapabilities(pluginCapability)
                        .setStatus(0).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }

            /**
             * init fetchData
             *
             * @param request request
             * @param responseObserver responseObserver
             */
            @Override
            public void fetchData(ProfilerServiceTypes.FetchDataRequest request,
                StreamObserver<ProfilerServiceTypes.FetchDataResponse> responseObserver) {
                MemoryPluginResult.AppSummary sss =
                    MemoryPluginResult.AppSummary.newBuilder().setJavaHeap(getIntData()).setNativeHeap(getIntData())
                        .setCode(getIntData()).setStack(getIntData()).setGraphics(getIntData())
                        .setPrivateOther(getIntData()).setSystem(0).build();
                MemoryPluginResult.ProcessMemoryInfo processesInfoOne =
                    MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(31141)
                        .setName("com.eg.and.AlipayGphone:push").setRssShmemKb(1).setMemsummary(sss).build();
                MemoryPluginResult.ProcessMemoryInfo processesInfoTwo =
                    MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(31142).setName("com.eg.and.AlipayGphone")
                        .setRssShmemKb(2222222).build();
                MemoryPluginResult.ProcessMemoryInfo processesInfoThree =
                    MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(31144)
                        .setName("com.hisunflytone.and:pushservice").setRssShmemKb(3333333).build();
                MemoryPluginResult.MemoryData aaa =
                    MemoryPluginResult.MemoryData.newBuilder().addProcessesinfo(processesInfoOne)
                        .addProcessesinfo(processesInfoTwo).addProcessesinfo(processesInfoThree).build();
                CommonTypes.ProfilerPluginData data =
                    CommonTypes.ProfilerPluginData.newBuilder().setName("memory-plugin").setStatus(0)
                        .setData(aaa.toByteString()).build();
                ProfilerServiceTypes.FetchDataResponse fetchDataResponse =
                    ProfilerServiceTypes.FetchDataResponse.newBuilder().setResponseId(123456789).setStatus(0)
                        .setHasMore(false).addPluginData(data).build();
                responseObserver.onNext(fetchDataResponse);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        SessionManager.getInstance();
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient("", 11007, channel);
        long num = session.createSession(device, process, Constant.REALTIME_SCENE, jsonObject);
        HashMap<String, AbstractDataStore> map = new HashMap();
        map.put(MEMORY_PLUG, new MemoryTable());
        DataPoller dataPoller = new DataPoller(num, 111, client, map);
        dataPoller.run();
        Assert.assertNotNull(dataPoller);
    }

    /**
     * get Int Data
     *
     * @return int
     */
    private int getIntData() {
        requestId++;
        if (requestId == Integer.MAX_VALUE) {
            requestId = 0;
        }
        return requestId;
    }

}