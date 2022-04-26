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

import prompt from '@system.prompt';
import router from '@system.router';
import database from '../../storage/DatabaseUtils';
import { secToTime } from '../../utils/TimeUtils';
import { ReportItem, getReportListDb } from '../../data/LocalCommon';
import { TopComponent } from '../main/TopComponent';
import { HomeBottomPage } from './HomeBottomPage';

@Component
@Preview
export struct Report {
  @State private reportItem: Array<ReportItem> = globalThis.reportList

  build() {
    Column() {
      Row() {
        TopComponent({ title: "报告" })
      }.backgroundColor($r("app.color.colorPrimary"))
      .width('100%')
      .padding({ bottom: '15vp' })

      List({ space: '20vp', initialIndex: 0 }) {
        ForEach(this.reportItem, (item) => {

          ListItem() {

            Flex({ direction: FlexDirection.Row, alignItems: ItemAlign.Start, justifyContent: FlexAlign.SpaceBetween }) {
              Row() {
                Image(globalThis.iconMap.get(item.packageName))
                  .width('60vp')
                  .height('60vp')
                  .padding('10vp')

                Flex({
                  direction: FlexDirection.Column,
                  alignItems: ItemAlign.Start,
                  justifyContent: FlexAlign.SpaceBetween
                }) {
                  Text(`${item.name}`).fontSize('15fp')

                  Text(`${item.appName}`).backgroundColor(0xFFFFFF)

                  Text(`${item.startTime}`).backgroundColor(0xFFFFFF)

                  Text(secToTime(item.testDuration).toString()).backgroundColor(0xFFFFFF)
                }

                Image($r("app.media.report_upload"))
                  .width('40vp')
                  .height('40vp')
                  .padding('10vp')
                  .margin({ left: 800 })
                  .onClick(function (item) {
                    prompt.showToast({ message: "报告上传功能正在开发中", duration: 1000 })
                  })

              }.margin({ left: '5%', top: '2%', bottom: '2%', right: '2%' }).onClick(() => {
                let databasePath: string = item.dbPath
                //截取时间戳
                console.log("consoleLog DbPath:" + item.dbPath);
                console.log("consoleLog packageName:" + item.packageName);
                console.log("consoleLog base64:" + globalThis.iconMap.get(item.packageName));
                let timeStamp = databasePath.substring(databasePath.lastIndexOf("/") + 1, databasePath.length)
                console.log("consoleLog DbPath timeStamp:" + timeStamp);
                database.queryData(timeStamp + ".db").then(data => {
                  console.log("resultSet column names:getResults:" + JSON.stringify(data))
                  console.log("resultSet column names:getResults length:" + data.length)
                  router.push({ uri: 'pages/ReportDetail', params: {
                    gpData: data,
                    reportItem: item
                  } })
                })
              })
            }
          }
        }, item => item.toString())
      }
      .edgeEffect(EdgeEffect.None) // 滑动到边缘无效果
      .chainAnimation(false) // 联动特效关闭
      .listDirection(Axis.Vertical) // 排列方向
      .divider({ strokeWidth: '2vp', color: 0xdddddd, startMargin: '10vp', endMargin: '12vp' }) // 每行之间的分界线

    }.height('88%')
  }
}