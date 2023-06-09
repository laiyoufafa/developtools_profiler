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
import { TIndexInfo } from '../common/entity/DatabaseEntity';
import router from '@system.router';
import { Summary } from '../common/ui/detail/Summary';
import { StartTestTitleComponent } from '../common/ui/StartTestTitleComponent';
import { ReportItem } from '../common/entity/LocalConfigEntity';


@Entry
@Component
struct ReportDetail {
  private controller: TabsController = new TabsController()
  private gpData: Array<TIndexInfo> = new Array()
  private reportItem: ReportItem = null

  aboutToAppear() {
    let data = router.getParams()["gpData"]
    let report = router.getParams()["reportItem"]
    if (data != null) {
      this.gpData = data
    }
    if (report != null) {
      this.reportItem = report
    }
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
          Text(`文件路径:${this.reportItem.dbPath}/t_index_info.csv`)
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
          }
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