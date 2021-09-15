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

package com.openharmony.devices;

import com.openharmony.client.Client;
import com.openharmony.hdc.HarmonyDebugConnector;

/**
 * Device info ¡¢create at 20210912
 */
public class Devices {
    private static final String ONLINE = "Connected";
    private static final char SEPARATOR = '-';
    private String mSerialNumber;

    private String mSelinux;
    private String mBuildDate;
    private String mFingerprint;
    private String mSdkVersion;
    private String mBuildType;
    private String mDebuggable;
    private String mVndkVersion;
    private String mSecure;
    private String mSecurityPatch;
    private String mManufacturer;
    private String mProductBrand;
    private String mProductBoard;
    private String mProductModel;
    private String mProductDevice;
    private String mName;

    private Client mClient;
    private DeviceState mState;

    /**
     * init devices state
     * example :
     *  * connect key: 15010038475446345206a332927bb7af
     *  * connect type: USB
     *  * status:Connected
     *  * ip:localhost
     *
     * @param serialNumber device id
     * @param deviceState device base state
     * @param testMode whether in test mode
     */
    public Devices(String serialNumber, DeviceState deviceState, boolean testMode) {
        if (serialNumber == null || isUnValid(serialNumber)) {
            mSerialNumber = "TestModeDeviceID";
        }
        if (testMode) {
            mSerialNumber = serialNumber;
            mState = deviceState;
            setClient(HarmonyDebugConnector.createConnect("Hdc", true));
            setState(mState);
            setSelinux("Permission");
            setBuildDate("19700101");
            setFingerprint("v300:10/QP1A.190711.020/ohosbuild05291519:userdebug/test-keys");
            setSdkVersion("28");
            setBuildType("userdebug");
            setDebuggable("true");
            setVndkVersion("28");
            setSecure("1");
            setSecurityPatch("19700101");
            setManufacturer("OHOS");
            setProductBrand("OHOS");
            setProductBoard("OHOS");
            setProductModel("OHOS");
            setProductDevice("OHOS");
        } else {
            mSerialNumber = serialNumber;
            mState = deviceState;
        }
    }

    /**
     * get connect client
     *
     * @return client
     */
    public Client getClient() {
        return mClient;
    }

    /**
     * set connect client
     */
    protected void setClient(HarmonyDebugConnector hdc) {
        mClient = new Client(getSerialNumber(), hdc);
    }

    /**
     * get device id
     *
     * @return SerialNumber
     */
    public String getSerialNumber() {
        return mSerialNumber;
    }

    /**
     * get device status
     *
     * @return whether online
     */
    public boolean isOnline() {
        return mState.getStatus().equals(ONLINE);
    }

    /**
     * get connect status value
     *
     * @return device state
     */
    public DeviceState getState() {
        return mState;
    }

    /**
     * set connect status value
     */
    protected void setState(DeviceState state) {
        this.mState = state;
    }

    /**
     * get ro.boot.selinux value
     *
     * @return Selinux
     */
    public String getSelinux() {
        return mSelinux;
    }

    /**
     * set ro.boot.selinux value
     */
    protected void setSelinux(String mSelinux) {
        this.mSelinux = mSelinux;
    }

    /**
     * get ro.build.date value
     *
     * @return BuildDate
     */
    public String getBuildDate() {
        return mBuildDate;
    }

    /**
     * set ro.build.date value
     */
    protected void setBuildDate(String mBuildDate) {
        this.mBuildDate = mBuildDate;
    }

    /**
     * get ro.build.fingerprint value
     *
     * @return Fingerprint
     */
    public String getFingerprint() {
        return mFingerprint;
    }

    /**
     * set ro.build.fingerprint value
     */
    protected void setFingerprint(String mFingerprint) {
        this.mFingerprint = mFingerprint;
    }

    /**
     * get ro.build.version.sdk value
     *
     * @return SdkVersion
     */
    public String getSdkVersion() {
        return mSdkVersion;
    }

    /**
     * set ro.build.version.sdk value
     */
    protected void setSdkVersion(String mSdkVersion) {
        this.mSdkVersion = mSdkVersion;
    }

    /**
     * get ro.build.type value
     *
     * @return BuildType
     */
    public String getBuildType() {
        return mBuildType;
    }

    /**
     * set ro.build.type value
     */
    protected void setBuildType(String mBuildType) {
        this.mBuildType = mBuildType;
    }

    /**
     * get ro.debuggable value
     *
     * @return Debuggable
     */
    public String getDebuggable() {
        return mDebuggable;
    }

    /**
     * set ro.debuggable value
     */
    protected void setDebuggable(String mDebuggable) {
        this.mDebuggable = mDebuggable;
    }

    /**
     * get ro.vndk.version value
     *
     * @return VndkVersion
     */
    public String getVndkVersion() {
        return mVndkVersion;
    }

    /**
     * set ro.vndk.version value
     */
    protected void setVndkVersion(String mVndkVersion) {
        this.mVndkVersion = mVndkVersion;
    }

    /**
     * get ro.secure value
     *
     * @return Secure
     */
    public String getSecure() {
        return mSecure;
    }

    /**
     * set ro.secure value
     */
    protected void setSecure(String mSecure) {
        this.mSecure = mSecure;
    }

    /**
     * get ro.build.version.security_patch value
     *
     * @return Security Patch date
     */
    public String getSecurityPatch() {
        return mSecurityPatch;
    }

    /**
     * set ro.build.version.security_patch value
     */
    protected void setSecurityPatch(String mSecurityPatch) {
        this.mSecurityPatch = mSecurityPatch;
    }

    /**
     * get ro.product.manufacturer value
     *
     * @return Manufacturer
     */
    public String getManufacturer() {
        return mManufacturer;
    }

    /**
     * set ro.product.manufacturer value
     */
    protected void setManufacturer(String mManufacturer) {
        this.mManufacturer = mManufacturer;
    }

    /**
     * get ro.product.brand value
     *
     * @return Product Brand
     */
    public String getProductBrand() {
        return mProductBrand;
    }

    /**
     * set ro.product.brand value
     */
    protected void setProductBrand(String mProductBrand) {
        this.mProductBrand = mProductBrand;
    }

    /**
     * get ro.product.board value
     *
     * @return Product Board
     */
    public String getProductBoard() {
        return mProductBoard;
    }

    /**
     * set ro.product.board value
     */
    protected void setProductBoard(String mProductBoard) {
        this.mProductBoard = mProductBoard;
    }

    /**
     * get ro.product.model value
     *
     * @return ProductModel
     */
    public String getProductModel() {
        return mProductModel;
    }

    /**
     * set ro.product.model value
     */
    protected void setProductModel(String mProductModel) {
        this.mProductModel = mProductModel;
    }

    /**
     * get ro.product.device value
     *
     * @return ProductDevice
     */
    public String getProductDevice() {
        return mProductDevice;
    }

    /**
     * set ro.product.device value
     */
    protected void setProductDevice(String mProductDevice) {
        this.mProductDevice = mProductDevice;
    }

    /**
     * get device name = manufacturer-model-SerialNumber
     *
     * @return name
     */
    public String getName() {
        if (mName != null) {
            return mName;
        }
        mName = constructName();
        return mName;
    }

    private String constructName() {
        String manufacturer = cleanUpStringForDisplay(mManufacturer);
        String model = cleanUpStringForDisplay(mProductModel);

        StringBuilder sb = new StringBuilder(30);

        if (manufacturer != null) {
            sb.append(manufacturer);
            sb.append(SEPARATOR);
        }

        if (model != null) {
            sb.append(model);
            sb.append(SEPARATOR);
        }

        sb.append(mSerialNumber);
        return sb.toString();
    }

    private static String cleanUpStringForDisplay(String s) {
        if (s == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append('_');
            }
        }
        return sb.toString();
    }

    private static boolean isUnValid(String string) {
        return string.trim().length() == 0;
    }
}
