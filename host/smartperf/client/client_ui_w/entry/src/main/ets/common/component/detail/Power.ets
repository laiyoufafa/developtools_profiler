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
import LineChart from './chart/charts/LineChart'
import { LineChartModel } from './chart/charts/LineChart'
import { XAxis, XAxisPosition } from './chart/components/XAxis';
import YAxis, { AxisDependency, YAxisLabelPosition } from './chart/components/YAxis'
import LineData from './chart/data/LineData';
import { LineDataSet, ColorStop, Mode } from './chart/data/LineDataSet';
import EntryOhos from './chart/data/EntryOhos';
import { JArrayList } from './chart/utils/JArrayList';
import ILineDataSet from './chart/interfaces/datasets/ILineDataSet'
import { TIndexInfo } from '../../data/DatabaseEntity';


@Entry
@Component
export struct Power {
  @State private gpData: Array<TIndexInfo> = new Array()
  topAxis: XAxis = new XAxis(); //顶部X轴
  bottomAxis: XAxis = new XAxis(); //底部X轴
  width: number = 700; //表的宽度
  height: number = 250; //表的高度
  minOffset: number = 15; //X轴线偏移量
  leftAxis: YAxis = null;
  rightAxis: YAxis = null;
  lineData: LineData = null;
  lineChartModel : LineChartModel = new LineChartModel();

  aboutToAppear() {

    this.lineData = this.initCurveData();

    if (this.gpData.length < 10) {
      this.topAxis.setLabelCount(this.gpData.length, false);
    } else {
      this.topAxis.setLabelCount(6, false);
    }

    this.topAxis.setPosition(XAxisPosition.TOP);
    this.topAxis.setAxisMinimum(0);
    this.topAxis.setAxisMaximum(this.gpData.length - 1);

    if (this.gpData.length < 10) {
      this.bottomAxis.setLabelCount(this.gpData.length, false);
    } else {
      this.bottomAxis.setLabelCount(6, false);
    }
    this.bottomAxis.setPosition(XAxisPosition.BOTTOM);
    this.bottomAxis.setAxisMinimum(0);
    this.bottomAxis.setAxisMaximum(this.gpData.length - 1);

    this.leftAxis = new YAxis(AxisDependency.LEFT);
    this.leftAxis.setLabelCount(10, false);
    this.leftAxis.setPosition(YAxisLabelPosition.OUTSIDE_CHART);
    this.leftAxis.setSpaceTop(15);
    this.leftAxis.setAxisMinimum(this.lineData.getYMin() - 50); // this replaces setStartAtZero(true)
    this.leftAxis.setAxisMaximum(this.lineData.getYMax() + 50);
    this.leftAxis.enableGridDashedLine(10, 10, 0)

    this.rightAxis = new YAxis(AxisDependency.RIGHT);
    this.rightAxis.setDrawGridLines(false);
    this.rightAxis.setLabelCount(10, false);
    this.rightAxis.setSpaceTop(15);
    this.rightAxis.setAxisMinimum(this.lineData.getYMin() - 50); // this replaces setStartAtZero(true)
    this.rightAxis.setAxisMaximum(this.lineData.getYMax() + 50);

    this.lineChartModel.setTopAxis(this.topAxis);
    this.lineChartModel.setBottomAxis(this.bottomAxis);
    this.lineChartModel.setWidth(this.width);
    this.lineChartModel.setHeight(this.height);
    this.lineChartModel.setMinOffset(this.minOffset);
    this.lineChartModel.setLeftAxis(this.leftAxis);
    this.lineChartModel.setRightAxis(this.rightAxis);
    this.lineChartModel.setLineData(this.lineData);
    this.lineChartModel.setIsShowLegend(false);
    this.lineChartModel.init();
  }

  /**
     * 初始化数据
     * @param count  曲线图点的个数
     * @param range  y轴范围
     */
  private initCurveData(): LineData {

    let values = new JArrayList<EntryOhos>();
    for (let index: number = 0; index < this.gpData.length; index++) {
      const gpDataCur = this.gpData[index];
      if (gpDataCur.currentNow == "") {
        parseInt
        continue
      }
      values.add(new EntryOhos(Number(index).valueOf(),
      Math.round((Number(gpDataCur.currentNow).valueOf() * (Number(gpDataCur.voltageNow).valueOf() / 1e6)) / 3.8)
      ));
    }


    let gradientFillColor = new Array<ColorStop>();
    gradientFillColor.push([0x0C0099CC, 0.2])
    gradientFillColor.push([0x7F0099CC, 0.4])
    gradientFillColor.push([0x0099CC, 1.0])

    let dataSet = new JArrayList<ILineDataSet>();

    let set1 = new LineDataSet(values, "Power Info(归一化电流 MA)");
    set1.setDrawFilled(false);
    set1.setMode(Mode.CUBIC_BEZIER);
    set1.setGradientFillColor(gradientFillColor)
    set1.setColorByColor(Color.Blue);
    set1.setLineWidth(3)
    set1.setDrawValues(false);
    set1.setDrawCircles(false);
    set1.setCircleColor(Color.Blue);
    set1.setCircleRadius(8);
    set1.setDrawValues(false);
    set1.setCircleHoleRadius(4)
    set1.setCircleHoleColor(Color.Blue)
    set1.setDrawCircleHole(false)
    dataSet.add(set1);
    return new LineData(dataSet)
  }

  build() {
    Stack({ alignContent: Alignment.TopStart }) {
      Scroll() {
        Column({ space: 20 }) {
          LineChart({lineChartModel: this.lineChartModel})
          Text("Power Info(归一化电流 MA)") {
          }.fontWeight(FontWeight.Bold).fontColor(Color.Blue).fontSize('15fp').textAlign(TextAlign.Center)

        }.width('100%').alignItems(HorizontalAlign.Center)
      }.width('100%')
    }.width('100%').height('100%')
  }
}
