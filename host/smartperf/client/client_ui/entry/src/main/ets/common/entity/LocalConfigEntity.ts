/*
 * Copyright (C) 2022 Huawei Device Co., Ltd.
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
export enum TestMode {
    ONLINE,
    BRIGHTNESS,
    STARTUP
}

export class OtherSupport {
    public testName: string
    public testSrc: string
    public testMode: TestMode
    public resource: Resource

    constructor(testName: string, testSrc: string, testMode: TestMode, resource: Resource) {
        this.testName = testName
        this.testSrc = testSrc
        this.testMode = testMode
        this.resource = resource
    }
}

export class SwitchItem {
    public id: string
    public switchName: string
    public switchSrc: Resource
    public isOpen: boolean
    public enable: boolean

    constructor(id: string, switchName: string, switchSrc: Resource, isOpen: boolean, enable: boolean) {
        this.id = id
        this.switchName = switchName
        this.switchSrc = switchSrc
        this.isOpen = isOpen
        this.enable = enable
    }
}

export class CollectItem {
    public name: string
    public isSupport: boolean
    public enable: boolean

    constructor(name: string, isSupport: boolean, enable: boolean) {
        this.name = name
        this.isSupport = isSupport
        this.enable = enable
    }
}

export class TaskInfoConfig {
    public testName: string
    public collectItem: Array<CollectItem>
    public switchItem: Array<SwitchItem>

    constructor(testName?: string, collectItem?: Array<CollectItem>, switchItem?: Array<SwitchItem>) {
        this.testName = testName
        this.collectItem = collectItem
        this.switchItem = switchItem
    }
}

export class AppInfoItem {
    public id: number
    public packageName: string
    public appName: string
    public appVersion: String
    public appIcon: string
    public abilityName: string

    constructor(packageName: string, appName: string, appVersion: String, appIcon: string, abilityName: string) {
        this.packageName = packageName
        this.appName = appName
        this.appVersion = appVersion
        this.appIcon = appIcon
        this.abilityName = abilityName
    }
}

export class ReportItem {
    public sessionId: String;
    public dbPath: String;
    public packageName: String;
    public iconId: String;
    public name: String;
    public appName: String;
    public startTime: String;
    public testDuration: String;
    public upStatus: boolean;

    constructor(sessionId: String, dbPath: String, packageName: String, iconId: String, name: String, appName: String, startTime: String, testDuration: String, upStatus: boolean) {
        this.sessionId = sessionId
        this.dbPath = dbPath
        this.packageName = packageName
        this.iconId = iconId
        this.name = name
        this.appName = appName
        this.startTime = startTime
        this.testDuration = testDuration
        this.upStatus = upStatus
    }

    public getStartTime(): string{
        return this.startTime.valueOf()
    }

    public getTestDuration(): string{
        return this.testDuration.valueOf()
    }

    public getDbPath(): string{
        return this.dbPath.valueOf()
    }
}

export const otherSupportList = new Array(
    new OtherSupport('亮度调节', '调节屏幕亮度', TestMode.BRIGHTNESS, $r("app.media.icon_brightness_plus")),
)

export let switchList = new Array(
    new SwitchItem("trace", '是否抓取trace', $r("app.media.icon_average_frame_b"), false, true),
    new SwitchItem("is_camera", '是否为相机应用', $r("app.media.icon_camera"), false, true),
    new SwitchItem("is_video", '是否为视频应用', $r("app.media.icon_video"), false, true),
    new SwitchItem("screen_capture", '是否开启截图', $r("app.media.icon_screencap"), false, true)
)

export class QuestionItem {
    public question: string
    public answer: string

    constructor(question: string, answer: string) {
        this.answer = answer
        this.question = question
    }
}

export const questionList = new Array(
    new QuestionItem('1.SP工具支持FPS采集吗?', '可以，fps依赖Hidumper能力..'),
    new QuestionItem('2.SP工具支持RAM采集吗?', 'ram采集目前是 读取进程节点内存信息中的PSS值...'),
    new QuestionItem('3.FPS采集不到?', '可能是视频应用，需要联系开发添加对应的图层，做采集适配'),
    new QuestionItem('4.SP采集原理?', '目前除fps外,其他采集均是通过cat 系统节点获取'),
    new QuestionItem('5.报告页的值是怎么算的?', '最终以一场测试结果的平均值为准'),
    new QuestionItem('6.SP后续规划?', '集成更多采集能力,如trace采集,counter采集,网络采集等等;优化数据展示方式,报告上传网站端,在线分析性能功耗问题')
)

export class SummaryItem {
    public icon: Resource
    public content: string
    public value: string

    constructor(icon: Resource, content: string, value: string) {
        this.icon = icon
        this.content = content
        this.value = value
    }
}

