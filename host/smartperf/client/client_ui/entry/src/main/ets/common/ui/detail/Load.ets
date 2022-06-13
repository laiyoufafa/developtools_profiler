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
import { TIndexInfo } from '../../../common/entity/DatabaseEntity';

/**
 * 负载页面
 */
@Entry
@Component
export struct Load {
  @State private gpData: Array<TIndexInfo> = new Array()
  width: number = 350; //表的宽度
  height: number = 300; //表的高度
  minOffset: number = 15; //X轴线偏移量

  //cpuLoad0 cpuLoad1 cpuLoad2 cpuLoad3
  topAxis: XAxis = new XAxis(); //顶部X轴
  bottomAxis: XAxis = new XAxis(); //底部X轴
  leftAxis: YAxis = null;
  rightAxis: YAxis = null;
  lineData: LineData = null;

  //CPUFre0
  topAxis1: XAxis = new XAxis(); //顶部X轴
  bottomAxis1: XAxis = new XAxis(); //底部X轴
  leftAxis1: YAxis = null;
  rightAxis1: YAxis = null;
  lineData1: LineData = null;

  //GPULoad
  topAxis6: XAxis = new XAxis(); //顶部X轴
  bottomAxis6: XAxis = new XAxis(); //底部X轴
  leftAxis6: YAxis = null;
  rightAxis6: YAxis = null;
  lineData6: LineData = null;

  //GPUFre
  topAxis7: XAxis = new XAxis(); //顶部X轴
  bottomAxis7: XAxis = new XAxis(); //底部X轴
  leftAxis7: YAxis = null;
  rightAxis7: YAxis = null;
  lineData7: LineData = null;

  lineChartModel : LineChartModel =  new LineChartModel();
  lineChartModel1 : LineChartModel =  new LineChartModel();
  lineChartModel6 : LineChartModel =  new LineChartModel();
  lineChartModel7 : LineChartModel =  new LineChartModel();


  aboutToAppear() {
    //CPULoad 0-3
    this.lineData = this.initCpuLoad0to4Data();

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
    this.leftAxis.setLabelCount(11, false);
    this.leftAxis.setPosition(YAxisLabelPosition.OUTSIDE_CHART);
    this.leftAxis.setSpaceTop(15);
    this.leftAxis.setAxisMinimum(this.lineData.getYMin() - 10);
    this.leftAxis.setAxisMaximum(this.lineData.getYMax() + 10);
    this.leftAxis.enableGridDashedLine(10, 10, 0)
    this.rightAxis = new YAxis(AxisDependency.RIGHT);
    this.rightAxis.setDrawGridLines(false);
    this.rightAxis.setLabelCount(11, false);
    this.rightAxis.setSpaceTop(15);
    this.rightAxis.setAxisMinimum(this.lineData.getYMin() - 10); // this replaces setStartAtZero(true)
    this.rightAxis.setAxisMaximum(this.lineData.getYMax() + 10);


    //CPUFre0
    this.lineData1 = this.initCpu0FreData();
    if (this.gpData.length < 10) {
      this.topAxis1.setLabelCount(this.gpData.length, false);
    } else {
      this.topAxis1.setLabelCount(6, false);
    }

    this.topAxis1.setPosition(XAxisPosition.TOP);
    this.topAxis1.setAxisMinimum(0);
    this.topAxis1.setAxisMaximum(this.gpData.length - 1);

    if (this.gpData.length < 10) {
      this.bottomAxis1.setLabelCount(this.gpData.length, false);
    } else {
      this.bottomAxis1.setLabelCount(6, false);
    }
    this.bottomAxis1.setPosition(XAxisPosition.BOTTOM);
    this.bottomAxis1.setAxisMinimum(0);
    this.bottomAxis1.setAxisMaximum(this.gpData.length - 1);

    this.leftAxis1 = new YAxis(AxisDependency.LEFT);
    this.leftAxis1.setLabelCount(11, false);
    this.leftAxis1.setPosition(YAxisLabelPosition.OUTSIDE_CHART);
    this.leftAxis1.setSpaceTop(15);
    this.leftAxis1.setAxisMinimum(this.lineData1.getYMin() - 500);
    this.leftAxis1.setAxisMaximum(this.lineData1.getYMax() + 500);
    this.leftAxis1.enableGridDashedLine(10, 10, 0)
    this.rightAxis1 = new YAxis(AxisDependency.RIGHT);
    this.rightAxis1.setDrawGridLines(false);
    this.rightAxis1.setLabelCount(11, false);
    this.rightAxis1.setSpaceTop(15);
    this.rightAxis1.setAxisMinimum(this.lineData1.getYMin() - 500); // this replaces setStartAtZero(true)
    this.rightAxis1.setAxisMaximum(this.lineData1.getYMax() + 500);

    //GPULoad
    this.lineData6 = this.initGpuLoadData();
    if (this.gpData.length < 10) {
      this.topAxis6.setLabelCount(this.gpData.length, false);
    } else {
      this.topAxis6.setLabelCount(6, false);
    }

    this.topAxis6.setPosition(XAxisPosition.TOP);
    this.topAxis6.setAxisMinimum(0);
    this.topAxis6.setAxisMaximum(this.gpData.length - 1);

    if (this.gpData.length < 10) {
      this.bottomAxis6.setLabelCount(this.gpData.length, false);
    } else {
      this.bottomAxis6.setLabelCount(6, false);
    }
    this.bottomAxis6.setPosition(XAxisPosition.BOTTOM);
    this.bottomAxis6.setAxisMinimum(0);
    this.bottomAxis6.setAxisMaximum(this.gpData.length - 1);

    this.leftAxis6 = new YAxis(AxisDependency.LEFT);
    this.leftAxis6.setLabelCount(8, false);
    this.leftAxis6.setPosition(YAxisLabelPosition.OUTSIDE_CHART);
    this.leftAxis6.setSpaceTop(15);
    this.leftAxis6.setAxisMinimum(this.lineData6.getYMin() - 10);
    this.leftAxis6.setAxisMaximum(this.lineData6.getYMax() + 10);
    this.leftAxis6.enableGridDashedLine(10, 10, 0)
    this.rightAxis6 = new YAxis(AxisDependency.RIGHT);
    this.rightAxis6.setDrawGridLines(false);
    this.rightAxis6.setLabelCount(8, false);
    this.rightAxis6.setSpaceTop(15);
    this.rightAxis6.setAxisMinimum(this.lineData6.getYMin() - 10); // this replaces setStartAtZero(true)
    this.rightAxis6.setAxisMaximum(this.lineData6.getYMax() + 10);

    //GPUFre
    this.lineData7 = this.initGPUFreData();
    if (this.gpData.length < 10) {
      this.topAxis7.setLabelCount(this.gpData.length, false);
    } else {
      this.topAxis7.setLabelCount(6, false);
    }

    this.topAxis7.setPosition(XAxisPosition.TOP);
    this.topAxis7.setAxisMinimum(0);
    this.topAxis7.setAxisMaximum(this.gpData.length - 1);

    if (this.gpData.length < 10) {
      this.bottomAxis7.setLabelCount(this.gpData.length, false);
    } else {
      this.bottomAxis7.setLabelCount(6, false);
    }
    this.bottomAxis7.setPosition(XAxisPosition.BOTTOM);
    this.bottomAxis7.setAxisMinimum(0);
    this.bottomAxis7.setAxisMaximum(this.gpData.length - 1);

    this.leftAxis7 = new YAxis(AxisDependency.LEFT);
    this.leftAxis7.setLabelCount(8, false);
    this.leftAxis7.setPosition(YAxisLabelPosition.OUTSIDE_CHART);
    this.leftAxis7.setSpaceTop(15);
    this.leftAxis7.setAxisMinimum(this.lineData7.getYMin() - 500);
    this.leftAxis7.setAxisMaximum(this.lineData7.getYMax() + 500);
    this.leftAxis7.enableGridDashedLine(10, 10, 0)
    this.rightAxis7 = new YAxis(AxisDependency.RIGHT);
    this.rightAxis7.setDrawGridLines(false);
    this.rightAxis7.setLabelCount(8, false);
    this.rightAxis7.setSpaceTop(15);
    this.rightAxis7.setAxisMinimum(this.lineData7.getYMin() - 500); // this replaces setStartAtZero(true)
    this.rightAxis7.setAxisMaximum(this.lineData7.getYMax() + 500);


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

    this.lineChartModel1.setTopAxis(this.topAxis1);
    this.lineChartModel1.setBottomAxis(this.bottomAxis1);
    this.lineChartModel1.setWidth(this.width);
    this.lineChartModel1.setHeight(this.height);
    this.lineChartModel1.setMinOffset(this.minOffset);
    this.lineChartModel1.setLeftAxis(this.leftAxis1);
    this.lineChartModel1.setRightAxis(this.rightAxis1);
    this.lineChartModel1.setLineData(this.lineData1);
    this.lineChartModel1.setIsShowLegend(false);
    this.lineChartModel1.init();

    this.lineChartModel6.setTopAxis(this.topAxis6);
    this.lineChartModel6.setBottomAxis(this.bottomAxis6);
    this.lineChartModel6.setWidth(this.width);
    this.lineChartModel6.setHeight(this.height);
    this.lineChartModel6.setMinOffset(this.minOffset);
    this.lineChartModel6.setLeftAxis(this.leftAxis6);
    this.lineChartModel6.setRightAxis(this.rightAxis6);
    this.lineChartModel6.setLineData(this.lineData6);
    this.lineChartModel6.setIsShowLegend(false);
    this.lineChartModel6.init();

    this.lineChartModel7.setTopAxis(this.topAxis7);
    this.lineChartModel7.setBottomAxis(this.bottomAxis7);
    this.lineChartModel7.setWidth(this.width);
    this.lineChartModel7.setHeight(this.height);
    this.lineChartModel7.setMinOffset(this.minOffset);
    this.lineChartModel7.setLeftAxis(this.leftAxis7);
    this.lineChartModel7.setRightAxis(this.rightAxis7);
    this.lineChartModel7.setLineData(this.lineData7);
    this.lineChartModel7.setIsShowLegend(false);
    this.lineChartModel7.init();
  }

  /**
     * 初始化数据 cpu0Load、cpu1Load、cpu2Load、cpu3Load
     * @param count  曲线图点的个数
     * @param range  y轴范围
     */
  private initCpuLoad0to4Data(): LineData {

    let dataSet = new JArrayList<ILineDataSet>();

    let values = new JArrayList<EntryOhos>();
    console.log("gpDataCur.cpu0Frequency-----------1111:"+this.gpData.length)
    for (let index: number = 0; index < this.gpData.length; index++) {
      const gpDataCur = this.gpData[index];
      if (gpDataCur.cpu0Load == "") {
        continue
      }
      values.add(new EntryOhos(Number(index).valueOf(), Number(gpDataCur.cpu0Load).valueOf()));
    }
    let gradientFillColor = new Array<ColorStop>();
    gradientFillColor.push([0x0C0099CC, 0.2])
    gradientFillColor.push([0x7F0099CC, 0.4])
    gradientFillColor.push([0x0099CC, 1.0])
    let set1 = new LineDataSet(values, "cpu0Load(%)");
    set1.setDrawFilled(false);
    set1.setMode(Mode.CUBIC_BEZIER);
    set1.setDrawValues(false);
    set1.setGradientFillColor(gradientFillColor)
    set1.setColorByColor(Color.Blue);
    set1.setLineWidth(3)
    set1.setDrawCircles(false);
    set1.setCircleColor(Color.Blue);
    set1.setCircleRadius(8);
    set1.setCircleHoleRadius(4)
    set1.setCircleHoleColor(Color.Blue)
    set1.setDrawCircleHole(false)
    dataSet.add(set1);
    let values2 = new JArrayList<EntryOhos>();
    for (let index: number = 0; index < this.gpData.length; index++) {
      const gpDataCur = this.gpData[index];
      if (gpDataCur.cpu1Load == "") {
        continue
      }
      values2.add(new EntryOhos(Number(index).valueOf(), Number(gpDataCur.cpu1Load).valueOf()));
    }
    let gradientFillColor2 = new Array<ColorStop>();
    gradientFillColor2.push([0x0C0099CC, 0.2])
    gradientFillColor2.push([0x7F0099CC, 0.4])
    gradientFillColor2.push([0x0099CC, 1.0])
    let set2 = new LineDataSet(values2, "cpu1Load(%)");
    set2.setDrawFilled(false);
    set2.setMode(Mode.CUBIC_BEZIER);
    set2.setGradientFillColor(gradientFillColor2)
    set2.setColorByColor(Color.Green);
    set2.setLineWidth(3)
    set1.setDrawValues(false);
    set2.setDrawCircles(false);
    set2.setCircleColor(Color.Green);
    set2.setCircleRadius(8);
    set2.setCircleHoleRadius(4)
    set2.setCircleHoleColor(Color.Green)
    set2.setDrawCircleHole(false)
    dataSet.add(set2);
    let values3 = new JArrayList<EntryOhos>();
    for (let index: number = 0; index < this.gpData.length; index++) {
      const gpDataCur = this.gpData[index];
      if (gpDataCur.cpu2Load == "") {
        continue
      }
      values3.add(new EntryOhos(Number(index).valueOf(), Number(gpDataCur.cpu2Load).valueOf()));
    }
    let gradientFillColor3 = new Array<ColorStop>();
    gradientFillColor3.push([0x0C0099CC, 0.2])
    gradientFillColor3.push([0x7F0099CC, 0.4])
    gradientFillColor3.push([0x0099CC, 1.0])
    let set3 = new LineDataSet(values3, "cpu2Load(%)");
    set3.setDrawFilled(false);
    set3.setMode(Mode.CUBIC_BEZIER);
    set1.setDrawValues(false);
    set3.setGradientFillColor(gradientFillColor3)
    set3.setColorByColor(Color.Red);
    set3.setLineWidth(3)
    set3.setDrawCircles(false);
    set3.setCircleColor(Color.Red);
    set3.setCircleRadius(8);
    set3.setCircleHoleRadius(4)
    set3.setCircleHoleColor(Color.Red)
    set3.setDrawCircleHole(false)
    dataSet.add(set3);
    let values4 = new JArrayList<EntryOhos>();
    for (let index: number = 0; index < this.gpData.length; index++) {
      const gpDataCur = this.gpData[index];
      if (gpDataCur.cpu3Load == "") {
        continue
      }
      values4.add(new EntryOhos(Number(index).valueOf(), Number(gpDataCur.cpu3Load).valueOf()));
    }
    let gradientFillColor4 = new Array<ColorStop>();
    gradientFillColor4.push([0x0C0099CC, 0.2])
    gradientFillColor4.push([0x7F0099CC, 0.4])
    gradientFillColor4.push([0x0099CC, 1.0])
    let set4 = new LineDataSet(values4, "cpu3Load(%)");
    set4.setDrawFilled(false);
    set4.setMode(Mode.CUBIC_BEZIER);
    set4.setGradientFillColor(gradientFillColor4)
    set4.setColorByColor(Color.Orange);
    set1.setDrawValues(false);
    set4.setLineWidth(3)
    set4.setDrawCircles(false);
    set4.setCircleColor(Color.Orange);
    set4.setCircleRadius(8);
    set4.setCircleHoleRadius(4)
    set4.setCircleHoleColor(Color.Orange)
    set4.setDrawCircleHole(false)
    dataSet.add(set4);

    return new LineData(dataSet)
  }

  private initCpu0FreData(): LineData {

    let values = new JArrayList<EntryOhos>();
    for (let index: number = 0; index < this.gpData.length; index++) {
      const gpDataCur = this.gpData[index];

      if (gpDataCur.cpu0Frequency == "") {

        continue
      }

      values.add(new EntryOhos(Number(index).valueOf(), Number(gpDataCur.cpu0Frequency).valueOf() / 1e3));
    }


    let gradientFillColor = new Array<ColorStop>();
    gradientFillColor.push([0x0C0099CC, 0.2])
    gradientFillColor.push([0x7F0099CC, 0.4])
    gradientFillColor.push([0x0099CC, 1.0])

    let dataSet = new JArrayList<ILineDataSet>();

    let set1 = new LineDataSet(values, "cpu0Frequency(MHZ)");
    set1.setDrawFilled(false);
    set1.setMode(Mode.CUBIC_BEZIER);
    set1.setGradientFillColor(gradientFillColor)
    set1.setColorByColor(Color.Red);
    set1.setLineWidth(3)
    set1.setDrawCircles(false);
    set1.setCircleColor(Color.Blue);
    set1.setCircleRadius(8);
    set1.setCircleHoleRadius(4)
    set1.setCircleHoleColor(Color.Red)
    set1.setDrawCircleHole(false)
    dataSet.add(set1);
    return new LineData(dataSet)
  }

  //GPULoad
  private initGpuLoadData(): LineData {

    let values = new JArrayList<EntryOhos>();
    for (let index: number = 0; index < this.gpData.length; index++) {
      const gpDataCur = this.gpData[index];
      if (gpDataCur.gpuLoad == "") {
        continue
      }

      values.add(new EntryOhos(Number(index).valueOf(), Number(gpDataCur.gpuLoad).valueOf()));
    }


    let gradientFillColor = new Array<ColorStop>();
    gradientFillColor.push([0x0C0099CC, 0.2])
    gradientFillColor.push([0x7F0099CC, 0.4])
    gradientFillColor.push([0x0099CC, 1.0])

    let dataSet = new JArrayList<ILineDataSet>();

    let set1 = new LineDataSet(values, "GPULoad(%)");
    set1.setDrawFilled(false);
    set1.setMode(Mode.CUBIC_BEZIER);
    set1.setGradientFillColor(gradientFillColor)
    set1.setColorByColor(Color.Green);
    set1.setLineWidth(3)
    set1.setDrawCircles(false);
    set1.setCircleColor(Color.Blue);
    set1.setCircleRadius(8);
    set1.setCircleHoleRadius(4)
    set1.setCircleHoleColor(Color.Green)
    set1.setDrawCircleHole(false)
    dataSet.add(set1);
    return new LineData(dataSet)
  }

  private initGPUFreData(): LineData {

    let values = new JArrayList<EntryOhos>();
    for (let index: number = 0; index < this.gpData.length; index++) {
      const gpDataCur = this.gpData[index];
      if (gpDataCur.gpuFrequency == "") {
        continue
      }

      values.add(new EntryOhos(Number(index).valueOf(), Number(gpDataCur.gpuFrequency).valueOf() / 1e6));
    }


    let gradientFillColor = new Array<ColorStop>();
    gradientFillColor.push([0x0C0099CC, 0.2])
    gradientFillColor.push([0x7F0099CC, 0.4])
    gradientFillColor.push([0x0099CC, 1.0])

    let dataSet = new JArrayList<ILineDataSet>();

    let set1 = new LineDataSet(values, "GPUFrequency(MHZ)");
    set1.setDrawFilled(false);
    set1.setMode(Mode.CUBIC_BEZIER);
    set1.setGradientFillColor(gradientFillColor)
    set1.setColorByColor(Color.Green);
    set1.setLineWidth(3)
    set1.setDrawCircles(false);
    set1.setCircleColor(Color.Blue);
    set1.setCircleRadius(8);
    set1.setCircleHoleRadius(4)
    set1.setCircleHoleColor(Color.Green)
    set1.setDrawCircleHole(false)
    dataSet.add(set1);
    return new LineData(dataSet)
  }

  build() {

    Stack({ alignContent: Alignment.TopStart }) {
      Scroll() {
        Column({ space: 20 }) {
          LineChart({lineChartModel: this.lineChartModel})
          LineChart({lineChartModel: this.lineChartModel1})
          LineChart({lineChartModel: this.lineChartModel6})
          Stack() {
            LineChart({lineChartModel: this.lineChartModel7})
          }.margin({ bottom: "50%" })
        }.width('100%').alignItems(HorizontalAlign.Center)
      }.width('100%')
    }.width('100%').height('100%')
  }
}
