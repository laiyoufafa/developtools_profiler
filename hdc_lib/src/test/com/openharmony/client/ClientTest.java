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

package test.com.openharmony.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.InvalidParameterException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.openharmony.client.Client;
import com.openharmony.hdc.HarmonyDebugConnector;

/**
 * Client UT test Create at 202109012
 */
public class ClientTest {
    private String mHdcPath;
    private String mAppPath1;
    private String mAppPath2;
    private String mLocalFile1;
    private String mLocalFile2;
    private String mDeviceFile1;
    private String mDeviceFile2;
    private String mDeviceDir;
    private String mLocalDir;
    private String mLocalPullFile;
    private HarmonyDebugConnector mHdc;

    /**
     * setUp test env
     *
     * @throws Exception IO error
     */
    @Before
    public void setUp() throws Exception {
        mHdcPath = "C:\\hdc_std.exe";
        mAppPath1 = "C:\\Test1.hap";
        mAppPath2 = "C:\\Test2.hap";
        mLocalFile1 = "C:\\TestFile1";
        mLocalFile2 = "C:\\TestFile2";
        mDeviceFile1 = "/data/TestFile1";
        mDeviceFile2 = "/data/TestFile2";
        mDeviceDir = "/data/";
        mLocalDir = "E:\\";
        mLocalPullFile = "E:\\OHOSTest";
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        mHdc.startDevicesMonitor();
        Thread.sleep(3000);
    }

    /**
     * destroy hdc env
     *
     * @throws Exception hdc exception
     */
    @After
    public void tearDown() throws Exception {
        mHdc.destroyConnect();
    }

    /**
     * test client init with error para
     */
    @Test
    public void testClient01() {
        try {
            Client client = new Client(null, null);
        } catch (InvalidParameterException error) {
            assertEquals("client value error", error.getMessage());
        }
    }

    /**
     * test client init with error para
     */
    @Test
    public void testClient02() {
        try {
            Client client = new Client("aaa", null);
        } catch (InvalidParameterException error) {
            assertEquals("client value error", error.getMessage());
        }
    }

    /**
     * test client init with error para
     */
    @Test
    public void testClient03() {
        try {
            Client client = new Client(null, mHdc);
        } catch (InvalidParameterException error) {
            assertEquals("client value error", error.getMessage());
        }
    }

    /**
     * test client init with correct para
     */
    @Test
    public void testClient04() {
        Client client = new Client("aaa", mHdc);
        assertNotNull(client.getHarmonyDebugConnector());
    }

    /**
     * test client init with correct para
     */
    @Test
    public void testClient05() {
        Client client = new Client("aaa", mHdc);
        assertEquals("aaa", client.getSerialNumber());
    }

    /**
     * test reboot function
     */
    @Test(timeout = 60000)
    public void testReboot() {
        isDeviceOnline();
        String deviceID = mHdc.getDevices()[0].getSerialNumber();
        boolean isContain = false;
        mHdc.getDevices()[0].getClient().reboot();
        try {
            Thread.sleep(12000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        for (int timeOut = 0; timeOut < 60; timeOut++) {
            try {
                if (isContain) {
                    break;
                }
                Thread.sleep(1000);
                for (int count = 0; count < mHdc.getDevices().length; count++) {
                    if (mHdc.getDevices()[count].getSerialNumber().equals(deviceID)
                            && mHdc.getDevices()[count].isOnline()) {
                        isContain = true;
                    }
                }
            } catch (InterruptedException error) {
                error.printStackTrace();
            }
        }
        assertTrue(isContain);
    }

    /**
     * test install correct hap
     */
    @Test(timeout = 10000)
    public void testInstallHap01() {
        isDeviceOnline();
        assertTrue(mHdc.getDevices()[0].getClient().installHap("-r", mAppPath1));
    }

    /**
     * test install two hap
     */
    @Test(timeout = 20000)
    public void testInstallHap02() {
        isDeviceOnline();
        assertTrue(mHdc.getDevices()[0].getClient().installHap("-r", mAppPath1));
        assertTrue(mHdc.getDevices()[0].getClient().installHap("-r", mAppPath2));
    }

    /**
     * test install error hap
     */
    @Test(timeout = 20000)
    public void testInstallHap03() {
        isDeviceOnline();
        assertFalse(mHdc.getDevices()[0].getClient().installHap("-r", "aaaa"));
    }

    /**
     * test uninstall correct hap
     */
    @Test(timeout = 30000)
    public void testUninstallHap01() {
        isDeviceOnline();
        assertTrue(mHdc.getDevices()[0].getClient().installHap("-r", mAppPath1));
        sleep(5);
        assertTrue(mHdc.getDevices()[0].getClient().uninstallHap("", "com.example.yantest"));
    }

    /**
     * test uninstall two hap
     */
    @Test(timeout = 30000)
    public void testUninstallHap02() {
        isDeviceOnline();
        assertTrue(mHdc.getDevices()[0].getClient().installHap("-r", mAppPath1));
        assertTrue(mHdc.getDevices()[0].getClient().installHap("-r", mAppPath2));
        sleep(5);
        assertTrue(mHdc.getDevices()[0].getClient().uninstallHap("", "com.example.p10"));
        assertTrue(mHdc.getDevices()[0].getClient().uninstallHap("", "com.example.yantest"));
    }

    /**
     * test uninstall wrong package name
     */
    @Test(timeout = 30000)
    public void testUninstallHap03() {
        isDeviceOnline();
        assertTrue(mHdc.getDevices()[0].getClient().installHap("-r", mAppPath1));
        sleep(6);
        assertFalse(mHdc.getDevices()[0].getClient().uninstallHap("", "com.example.aaaa"));
    }

    /**
     * test start hap
     */
    @Test(timeout = 30000)
    public void testStartHap01() {
        isDeviceOnline();
        assertTrue(mHdc.getDevices()[0].getClient().installHap("-r", mAppPath2));
        sleep(6);
        assertTrue(mHdc.getDevices()[0].getClient().startHap("com.example.p10", "com.example.p10.MainAbility"));
    }

    /**
     * test start hap with wrong package name
     */
    @Test(timeout = 30000)
    public void testStartHap02() {
        isDeviceOnline();
        assertTrue(mHdc.getDevices()[0].getClient().installHap("-r", mAppPath2));
        assertFalse(mHdc.getDevices()[0].getClient().startHap("com.example", "com.example.p10.MainAbility"));
    }

    /**
     * test start hap with wrong class name
     */
    @Test(timeout = 30000)
    public void testStartHap03() {
        isDeviceOnline();
        assertTrue(mHdc.getDevices()[0].getClient().installHap("-r", mAppPath2));
        assertFalse(mHdc.getDevices()[0].getClient().startHap("com.example.p10", "com.example.p10"));
    }

    /**
     * test send small file to device
     */
    @Test(timeout = 30000)
    public void testSendFile01() {
        isDeviceOnline();
        assertEquals("root succeess", mHdc.getDevices()[0].getClient().root());
        sleep(3);
        for (int wait = 0; wait < 30; wait++) {
            if (mHdc.getDevices()[0].isOnline()) {
                break;
            } else {
                sleep(1);
            }
        }
        assertEquals("please check call back", mHdc.getDevices()[0].getClient().sendFile(mLocalFile1, mDeviceDir));
    }

    /**
     * test send big file to device
     */
    @Test(timeout = 30000)
    public void testSendFile02() {
        isDeviceOnline();
        assertEquals("root succeess", mHdc.getDevices()[0].getClient().root());
        sleep(3);
        for (int wait = 0; wait < 30; wait++) {
            if (mHdc.getDevices()[0].isOnline()) {
                break;
            } else {
                sleep(1);
            }
        }
        assertEquals("please check call back", mHdc.getDevices()[0].getClient().sendFile(mLocalFile2, mDeviceDir));
    }

    /**
     * test recv small file to device
     */
    @Test(timeout = 30000)
    public void testRecvFile01() {
        isDeviceOnline();
        assertEquals("root succeess", mHdc.getDevices()[0].getClient().root());
        sleep(3);
        for (int wait = 0; wait < 30; wait++) {
            if (mHdc.getDevices()[0].isOnline()) {
                break;
            } else {
                sleep(1);
            }
        }
        assertEquals("please check call back", mHdc.getDevices()[0].getClient().sendFile(mLocalFile1, mDeviceDir));
        sleep(2);
        assertEquals("please check call back", mHdc.getDevices()[0].getClient().recvFile(mDeviceFile1, mLocalDir));
    }

    /**
     * test send big file to device
     */
    @Test(timeout = 30000)
    public void testRecvFile02() {
        isDeviceOnline();
        assertEquals("root succeess", mHdc.getDevices()[0].getClient().root());
        sleep(3);
        for (int wait = 0; wait < 30; wait++) {
            if (mHdc.getDevices()[0].isOnline()) {
                break;
            } else {
                sleep(1);
            }
        }
        assertEquals("please check call back", mHdc.getDevices()[0].getClient().sendFile(mLocalFile2, mDeviceDir));
        sleep(2);
        assertEquals("please check call back", mHdc.getDevices()[0].getClient().recvFile(mDeviceFile2, mLocalPullFile));
    }

    /**
     * test get prop
     */
    @Test(timeout = 5000)
    public void testGetProp01() {
        isDeviceOnline();
        assertTrue(mHdc.getDevices()[0].getClient().getProp("ro.build.date", "").length() > 0);
    }

    /**
     * test get error prop
     */
    @Test(timeout = 5000)
    public void testGetProp02() {
        isDeviceOnline();
        assertEquals("error", mHdc.getDevices()[0].getClient().getProp("aaaa", "error"));
    }

    /**
     * test root device
     */
    @Test(timeout = 10000)
    public void testRoot01() {
        isDeviceOnline();
        String debugable = mHdc.getDevices()[0].getDebuggable().toString();
        assertEquals("1", debugable);
        assertEquals("root succeess", mHdc.getDevices()[0].getClient().root());
    }

    /**
     * test root device failed
     */
    @Test(timeout = 10000)
    public void testRoot02() {
        isDeviceOnline();
        mHdc.getDevices()[0].getDebuggable();
        assertEquals("0", mHdc.getDevices()[0].getDebuggable());
        assertEquals("root failed", mHdc.getDevices()[0].getClient().root());
    }

    /**
     * test remount device
     */
    @Test(timeout = 10000)
    public void testRemount01() {
        isDeviceOnline();
        String debugable = mHdc.getDevices()[0].getDebuggable().toString();
        assertEquals("1", debugable);
        assertEquals("root succeess", mHdc.getDevices()[0].getClient().root());
        sleep(5);
        assertEquals("remount success", mHdc.getDevices()[0].getClient().remount());
    }

    /**
     * test remount device failed
     */
    @Test(timeout = 10000)
    public void testRemount02() {
        isDeviceOnline();
        String debugable = mHdc.getDevices()[0].getDebuggable().toString();
        assertEquals("0", debugable);
        assertEquals("root failed", mHdc.getDevices()[0].getClient().root());
        sleep(5);
        assertEquals("remount failed", mHdc.getDevices()[0].getClient().remount());
    }

    /**
     * test Remote Connect
     */
    @Test(timeout = 10000)
    public void testSetRemoteConnect01() {
        String temp = mHdc.getDevices()[0].getClient().setRemoteConnect("192.168.0.100", "10178");
        assertEquals("[Success]Connect OK", temp);
    }

    /**
     * test Remote Connect with error para
     */
    @Test(timeout = 10000)
    public void testSetRemoteConnect02() {
        String temp = mHdc.getDevices()[0].getClient().setRemoteConnect("192.168.0.100", "1017800");
        assertEquals("[Fail]IP:Port incorrect", temp);
    }

    /**
     * test Remote Connect with device refuse
     */
    @Test(timeout = 10000)
    public void testSetRemoteConnect03() {
        String temp = mHdc.getDevices()[0].getClient().setRemoteConnect("192.168.0.100", "9999");
        assertEquals("[Fail]Connect failed", temp);
    }

    /**
     * test remove Remote Connect
     */
    @Test(timeout = 10000)
    public void testRemoveRemoteConnect01() {
        String temp = mHdc.getDevices()[0].getClient().setRemoteConnect("192.168.0.100", "10178");
        assertEquals("[Success]Connect OK", temp);
        assertEquals("", mHdc.getDevices()[0].getClient().removeRemoteConnect("192.168.0.100", "10178"));
    }

    /**
     * test remove Remote Connect with error para
     */
    @Test(timeout = 10000)
    public void testRemoveRemoteConnect02() {
        String temp = mHdc.getDevices()[0].getClient().removeRemoteConnect("192.168.0.100", "9999");
        assertEquals("[Fail]No target available", temp);
    }

    /**
     * test Forward Port with tcp
     */
    @Test(timeout = 30000)
    public void testForwardPort01() {
        assertEquals("Forwardport result:OK", mHdc.getDevices()[0].getClient().forwardPort("tcp:8888", "tcp:11888"));
    }

    /**
     * test Forward Port with localabstract
     */
    @Test(timeout = 30000)
    public void testForwardPort02() {
        String temp = mHdc.getDevices()[0].getClient().forwardPort("tcp:8888", "localabstract:ohos");
        assertEquals("Forwardport result:OK", temp);
    }

    /**
     * test Forward Port with localreserved
     */
    @Test(timeout = 30000)
    public void testForwardPort03() {
        String temp = mHdc.getDevices()[0].getClient().forwardPort("tcp:8888", "localreserved:ohos");
        assertEquals("Forwardport result:OK", temp);
    }

    /**
     * test Forward Port with localfilesystem
     */
    @Test(timeout = 30000)
    public void testForwardPort04() {
        String temp = mHdc.getDevices()[0].getClient().forwardPort("tcp:8888", "localfilesystem:ohos");
        assertEquals("Forwardport result:OK", temp);
    }

    /**
     * test Forward Port with jdwp
     */
    @Test(timeout = 30000)
    public void testForwardPort05() {
        assertEquals("Forwardport result:OK", mHdc.getDevices()[0].getClient().forwardPort("tcp:8888", "jdwp:hdcd"));
    }

    /**
     * test Forward Port with dev
     */
    @Test(timeout = 30000)
    public void testForwardPort06() {
        assertEquals("Forwardport result:OK", mHdc.getDevices()[0].getClient().forwardPort("tcp:8888", "dev:hdcd"));
    }

    /**
     * test Forward Port with same tcp
     */
    @Test(timeout = 30000)
    public void testForwardPort07() {
        assertEquals("Forwardport result:OK", mHdc.getDevices()[0].getClient().forwardPort("tcp:8888", "tcp:11888"));
        String temp = mHdc.getDevices()[0].getClient().forwardPort("tcp:8888", "tcp:11887");
        assertEquals("[Fail]TCP Port listen failed at 8888", temp);
    }

    /**
     * test Forward Port with different tcp
     */
    @Test(timeout = 30000)
    public void testForwardPort08() {
        assertEquals("Forwardport result:OK", mHdc.getDevices()[0].getClient().forwardPort("tcp:8888", "tcp:11888"));
        assertEquals("Forwardport result:OK", mHdc.getDevices()[0].getClient().forwardPort("tcp:8887", "tcp:11888"));
    }

    /**
     * test remove error Forward Port
     */
    @Test(timeout = 30000)
    public void testRemoveForwardPort01() {
        String temp = mHdc.getDevices()[0].getClient().removeForwardPort("tcp:8888", "tcp:11888");
        assertEquals("[Fail]Remove forward ruler failed,ruler:tcp:8888 tcp:11888", temp);
    }

    /**
     * test remove ForwardPort
     */
    @Test(timeout = 30000)
    public void testRemoveForwardPort02() {
        assertEquals("Forwardport result:OK", mHdc.getDevices()[0].getClient().forwardPort("tcp:8888", "tcp:11888"));
        assertEquals("", mHdc.getDevices()[0].getClient().removeForwardPort("tcp:8888", "tcp:11888"));
    }

    /**
     * test get empty ForwardPort
     */
    @Test(timeout = 30000)
    public void testGetAllForwardPort01() {
        assertEquals("[Empty]", mHdc.getDevices()[0].getClient().getAllForwardPort());
    }

    /**
     * test get two ForwardPort
     */
    @Test(timeout = 30000)
    public void testGetAllForwardPort02() {
        assertEquals("Forwardport result:OK", mHdc.getDevices()[0].getClient().forwardPort("tcp:8888", "tcp:11888"));
        assertEquals("Forwardport result:OK", mHdc.getDevices()[0].getClient().forwardPort("tcp:8889", "tcp:11889"));
        String temp = mHdc.getDevices()[0].getClient().getAllForwardPort();
        assertTrue(temp.split("\n")[0].contains("tcp:8888 tcp:11888"));
        assertTrue(temp.split("\n")[0].contains("[Forward]"));
        assertTrue(temp.split("\n")[1].contains("tcp:8889 tcp:11889"));
        assertTrue(temp.split("\n")[1].contains("[Forward]"));
    }

    /**
     * test get device hilog
     */
    @Test(timeout = 10000)
    public void testGetHilog() {
        isDeviceOnline();
        assertEquals("please check call back", mHdc.getDevices()[0].getClient().getHilog());
    }

    /**
     * test Hiperf default function
     */
    @Test(timeout = 40000)
    public void testDumpHiperf() {
        isDeviceOnline();
        assertEquals("root succeess", mHdc.getDevices()[0].getClient().root());
        sleep(3);
        mHdc.getDevices()[0].getClient().dumpHiperf(5, "/data/Hiperf_test");
        sleep(6);
        for (int wait = 0; wait < 30; wait++) {
            if (mHdc.getDevices()[0].isOnline()) {
                break;
            } else {
                sleep(1);
            }
        }
        assertEquals("please check call back", mHdc.getDevices()[0].getClient().recvFile("/data/Hiperf_test", "C:"));
    }

    /**
     * test Bytrace default function
     */
    @Test(timeout = 30000)
    public void testBytrace() {
        isDeviceOnline();
        assertEquals("root succeess", mHdc.getDevices()[0].getClient().root());
        sleep(3);
        mHdc.getDevices()[0].getClient().bytrace(5, "/data/bytrace_test");
        sleep(6);
        for (int wait = 0; wait < 30; wait++) {
            if (mHdc.getDevices()[0].isOnline()) {
                break;
            } else {
                sleep(1);
            }
        }
        assertEquals("please check call back", mHdc.getDevices()[0].getClient().recvFile("/data/bytrace_test", "C:"));
    }

    /**
     * testSendGC
     */
    @Test
    public void testSendGC() {
        fail("Not yet implemented");
    }

    /**
     * testGetPropfiler
     */
    @Test
    public void testGetPropfiler() {
        fail("Not yet implemented");
    }

    /**
     * testGetScreenshot
     */
    @Test
    public void testGetScreenshot() {
        fail("Not yet implemented");
    }

    private void isDeviceOnline() {
        assertTrue(mHdc.getDevices().length > 0);
        assertTrue(mHdc.getDevices()[0].isOnline());
    }

    private void sleep (int sencond) {
        try {
            Thread.sleep(sencond * 1000);
        } catch (InterruptedException error) {
            error.printStackTrace();
        }
    }
}
