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
import { secToTime } from '../common/utils/TimeUtils';
import { TIndexInfo } from '../common/data/DatabaseEntity';
import router from '@system.router';
import { Summary } from '../common/component/detail/Summary';
import { StartTestTitleComponent } from '../common/component/StartTestTitleComponent';
import { ReportItem } from '../common/data/LocalCommon';
import { Performance } from '../common/component/detail/Performance';
import { Load } from '../common/component/detail/Load';
import { Power } from '../common/component/detail/Power';
import { Temperature } from '../common/component/detail/Temperature';


@Entry
@Component
struct ReportDetail {
  private controller: TabsController = new TabsController()
  private gpData: Array<TIndexInfo> = new Array()
  private reportItem: ReportItem = null

  aboutToAppear() {
    console.info("ReportDetail" + "onPageShow called");
    let data: any= router.getParams()["gpData"]
    let report: any = router.getParams()["reportItem"]
    console.info("ReportDetail" + "onPageShow called report:" + JSON.stringify(report));
    if (data != null) {
      this.gpData = data
    }
    if (report != null) {
      this.reportItem = report
    }
    console.info("ReportDetail" + "onPageShow called reportItem:" + JSON.stringify(this.reportItem));
  }

  build() {

    Column() {
      StartTestTitleComponent({ title: "报告详情" })

      Row() {
        Flex({ justifyContent: FlexAlign.SpaceBetween }) {
          Column() {
            Image(globalThis.iconMap.get(this.reportItem.packageName))
              .width('60vp')
              .height('60vp')
              .margin({ top: '20vp', left: '20vp' })
          }.margin({ left: '4%' })


          Column() {
            Text(`SP工具`).fontSize('15fp').margin({ top: '30vp' })
            Text(`应用版本：v1.0.2`).fontSize('15fp').margin({ top: '10vp' })
          }.margin({ right: '4%' })
        }
      }

      Row() {
        Flex({ direction: FlexDirection.Column, alignItems: ItemAlign.Start, justifyContent: FlexAlign.Start }) {
          Text(`开始时间:${this.reportItem.startTime}`)
            .fontSize('13fp')
            .fontColor($r("app.color.color_666"))
            .margin({ top: '5vp' })
          Text(`测试时长:${secToTime(Number(this.reportItem.testDuration))}`)
            .fontSize('13fp')
            .fontColor($r("app.color.color_666"))
            .margin({ top: '5vp' })
          Text(`文件路径:${this.reportItem.dbPath} / t_index_info.csv`)
            .fontSize('13fp')
            .fontColor($r("app.color.color_666"))
            .margin({ top: '5vp' })
        }.width('100%').margin({ left: '10vp', top: '10vp' }).height('80')
      }.margin({ left: '4%' })

      Row() {
        Flex() {
          Tabs({ barPosition: BarPosition.Start, index: 0, controller: this.controller }) {
            TabContent() {
              Summary({ gpData: this.gpData })
            }.tabBar('概览')

            TabContent() {
              Column({space : "10vp"}) {
                Performance({ gpData: this.gpData })
//                Text("性能展示页在加紧发布中，原始数据可通过hdc命令导出").fontWeight(FontWeight.Bold).fontSize("15fp")
//                Text(`hdc_std file recv /data/app/el2/100/base/com.ohos.gameperceptio/files/${this.reportItem.dbPath.substring(this.reportItem.dbPath.lastIndexOf("/") + 1)}/t_index_info.csv .`)
              }.width('100%').height('100%').align(Alignment.Center)
            }.tabBar('性能')

            TabContent() {
              Column({space : "10vp"}) {
                Load({ gpData: this.gpData })
//                Text("负载展示页在加紧发布中，原始数据可通过hdc命令导出").fontWeight(FontWeight.Bold).fontSize("15fp")
//                Text(`hdc_std file recv /data/app/el2/100/base/com.ohos.gameperceptio/files/${this.reportItem.dbPath.substring(this.reportItem.dbPath.lastIndexOf("/") + 1)}/t_index_info.csv .`)
              }.width('100%').height('100%').align(Alignment.Center)
            }.tabBar('负载')

            TabContent() {
              Column({space : "10vp"}) {
                Power({ gpData: this.gpData })
//                Text("功耗展示页在加紧发布中，原始数据可通过hdc命令导出").fontWeight(FontWeight.Bold).fontSize("15fp")
//                Text(`hdc_std file recv /data/app/el2/100/base/com.ohos.gameperceptio/files/${this.reportItem.dbPath.substring(this.reportItem.dbPath.lastIndexOf("/") + 1)}/t_index_info.csv .`)
              }.width('100%').height('100%').align(Alignment.Center)
            }.tabBar('功耗')

            TabContent() {
              Column({space : "10vp"}) {
                Temperature({ gpData: this.gpData })
//                Text("热展示页在加紧发布中，原始数据可通过hdc命令导出").fontWeight(FontWeight.Bold).fontSize("15fp")
//                Text(`hdc_std file recv /data/app/el2/100/base/com.ohos.gameperceptio/files/${this.reportItem.dbPath.substring(this.reportItem.dbPath.lastIndexOf("/") + 1)}/t_index_info.csv .`)
              }.width('100%').height('100%').align(Alignment.Center)
            }.tabBar('热')
          }.backgroundColor("#f5f5f5")
          .barWidth(360)
          .scrollable(true)
          .barHeight(60)
          .width('100%')
          .height('100%')
        }
      }
    }
  }
}