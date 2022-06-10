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
import wm from '@ohos.window'

export let floatingWindowOffsetX: number = 50
export let floatingWindowOffsetY: number = 200
export let titleWindowOffsetX: number = 300
export let titleWindowOffsetY: number = 200
export let lineChartWindowOffsetX:number=1
export let lineChartWindowOffsetY:number=1
export let windowWidth: number = 2560
export let windowHeight: number = 1600

export function initFloatWindow() {
  createFloatWindow("currentNow")
  createFloatWindow("shellBackTemp")
  createFloatWindow("ddrFrequency")
  createFloatWindow("gpuFrequency")
  createFloatWindow("cpu0Frequency")
  createFloatWindow("cpu1Frequency")
  createFloatWindow("cpu2Frequency")
  createFloatWindow("RAM")
  createFloatWindow("FPS")
}
export function createFloatWindow(floatName:string) {
    //5.5SP2  2106 改成 8
    wm.create(globalThis.abilityContext, floatName, 2106).then((floatWin) => {
      floatWin.moveTo(lineChartWindowOffsetX, lineChartWindowOffsetY).then(() => {
          floatWin.resetSize(240,180).then(() => {
            floatWin.getProperties().then((property) => {
              property.isTransparent = false
            })
            if(floatName=="currentNow"){
              floatWin.loadContent('pages/CurrentNowLineChartPage').then(() => {
                floatWin.setBackgroundColor("#B3000000").then(() => { //透明
                  console.log("xhq win setTransparent end.");
                  floatWin.setBackgroundColor("#80020000").then(() => { //背景颜色
                  })
                })
                floatWin.show().then(() => {
                  floatWin.hide()
                })
              })
            }else if(floatName=="shellBackTemp"){
              floatWin.loadContent('pages/ShellBackTempLineChartPage').then(() => {
                floatWin.setBackgroundColor("#B3000000").then(() => { //透明
                  console.log("xhq win setTransparent end.");
                  floatWin.setBackgroundColor("#80020000").then(() => { //背景颜色
                  })
                })
                floatWin.show().then(() => {
                  floatWin.hide()
                })
              })
            }
            else if(floatName=="ddrFrequency"){
              floatWin.loadContent('pages/DDRLineChartPage').then(() => {
                floatWin.setBackgroundColor("#B3000000").then(() => { //透明
                  console.log("xhq win setTransparent end.");
                  floatWin.setBackgroundColor("#80020000").then(() => { //背景颜色
                  })
                })
                floatWin.show().then(() => {
                  floatWin.hide()
                })
              })
            }
            else if(floatName=="gpuFrequency"){
              floatWin.loadContent('pages/GPULineChartPage').then(() => {
                floatWin.setBackgroundColor("#B3000000").then(() => { //透明
                  console.log("xhq win setTransparent end.");
                  floatWin.setBackgroundColor("#80020000").then(() => { //背景颜色
                  })
                })
                floatWin.show().then(() => {
                  floatWin.hide()
                })
              })
            }
            else if(floatName=="cpu0Frequency"){
              floatWin.loadContent('pages/CPU0LineChartPage').then(() => {
                floatWin.setBackgroundColor("#B3000000").then(() => { //透明
                  console.log("xhq win setTransparent end.");
                  floatWin.setBackgroundColor("#80020000").then(() => { //背景颜色
                  })
                })
                floatWin.show().then(() => {
                  floatWin.hide()
                })
              })
            }
            else if(floatName=="cpu1Frequency"){
              floatWin.loadContent('pages/CPU1LineChartPage').then(() => {
                floatWin.setBackgroundColor("#B3000000").then(() => { //透明
                  floatWin.setBackgroundColor("#80020000").then(() => { //背景颜色
                  })
                })
                floatWin.show().then(() => {
                  floatWin.hide()
                })
              })
            }
            else if(floatName=="cpu2Frequency"){
              floatWin.loadContent('pages/CPU2LineChartPage').then(() => {
                floatWin.setBackgroundColor("#B3000000").then(() => { //透明
                  console.log("xhq win setTransparent end.");
                  floatWin.setBackgroundColor("#80020000").then(() => { //背景颜色
                  })
                })
                floatWin.show().then(() => {
                  floatWin.hide()
                })
              })
            }
            else if(floatName=="RAM"){
              floatWin.loadContent('pages/RAMLineChartPage').then(() => {
                floatWin.setBackgroundColor("#B3000000").then(() => { //透明
                  console.log("xhq win setTransparent end.");
                  floatWin.setBackgroundColor("#80020000").then(() => { //背景颜色
                  })
                })
                floatWin.show().then(() => {
                  floatWin.hide()
                })
              })
            }else if(floatName=="FPS") {
              floatWin.loadContent('pages/FpsLineChartPage').then(() => {
                floatWin.setBackgroundColor("#B3000000").then(() => { //透明
                  console.log("xhq win setTransparent end.");
                  floatWin.setBackgroundColor("#80020000").then(() => { //背景颜色
                  })
                })
                floatWin.show().then(() => {
                  floatWin.hide()
                  console.log("FPS--------------loadContent")
                })
              })
            }
          })
        })
    })
}
export function destoryAllFloatWindow() {
  destoryFloatWindow("currentNow")
  destoryFloatWindow("shellBackTemp")
  destoryFloatWindow("ddrFrequency")
  destoryFloatWindow("gpuFrequency")
  destoryFloatWindow("cpu0Frequency")
  destoryFloatWindow("cpu1Frequency")
  destoryFloatWindow("cpu2Frequency")
  destoryFloatWindow("RAM")
  destoryFloatWindow("FPS")

}
export function destoryFloatWindow(floatName:string) {

    wm.find(floatName).then((fltWin) => {
      fltWin.destroy().then(() => {

      })
    })

}
export function hideFloatWindow(floatName:string) {

    wm.find(floatName).then((fltWin) => {
      fltWin.hide()
    })

}
export function showFloatWindow(floatName:string) {

    wm.find(floatName).then((fltWin) => {
      fltWin.show()
    })

}
export function moveFloatWindow(floatName:string,offsetX: number, offsetY: number) {
  var xx = (floatingWindowOffsetX + offsetX * 2) < 0 ? 0 : ((floatingWindowOffsetX + offsetX * 2) > (windowWidth - 200) ? (windowWidth - 200) : (floatingWindowOffsetX + offsetX * 2))
  var yy = (floatingWindowOffsetY + offsetY * 2) < 0 ? 0 : ((floatingWindowOffsetY + offsetY * 2) > (windowHeight - 200) ? (windowHeight - 200) : (floatingWindowOffsetY + offsetY * 2))

  wm.find(floatName).then((fltWin) => {
    fltWin.moveTo(xx, yy)
  })

}
export function setFloatWindow(offsetX: number, offsetY: number) {
    this.floatingWindowOffsetX = (floatingWindowOffsetX + offsetX * 2) < 0 ? 0 : ((floatingWindowOffsetX + offsetX * 2) > (windowWidth - 200) ? (windowWidth - 200) : (floatingWindowOffsetX + offsetX * 2))
    this.floatingWindowOffsetY = (floatingWindowOffsetY + offsetY * 2) < 0 ? 0 : ((floatingWindowOffsetY + offsetY * 2) > (windowHeight - 200) ? (windowHeight - 200) : (floatingWindowOffsetY + offsetY * 2))
}