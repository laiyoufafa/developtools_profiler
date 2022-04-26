## 工具介绍

> OpenHarmony性能测试工具,通过采集设备性能指标，对采集数据进行实时展示、导出csv。

## 支持功能

> 当前版本支持如下功能

- 支持RK3568、Hi3516;
- 支持Shell启动;
- 支持采集整机CPU、GPU、DDR、POWER、TEMPERATURE、应用的FPS、RAM;

## 使用方式
>1、首先检查系统是否默认预制了SP_daemon，如打印如下日志，系统已内置SP_daemon

```bash
C:\>hdc_std shell
SP_daemon --help
usage: SP_daemon <options> <arguments>
--------------------------------------------------------------------
These are common commands list:
 -N             set num of profiler <must be non-null>
 -PKG           set pkgname of profiler
 -PID           set process id of profiler
 -OUT           set output path of CSV
 -c             get cpuFreq and cpuLoad
 -g             get gpuFreq and gpuLoad
 -f             get fps and fps jitters
 -t             get soc-temp gpu-temp ..
 -p             get current_now and voltage_now
 -r             get ram(pss)
 -snapshot      get screen capture
--------------------------------------------------------------------
Example: SP_daemon -N 20 -PKG ohos.samples.ecg -c -g -t -p -f
--------------------------------------------------------------------
command exec finished!
```
>2、执行示例命令：SP_daemon -N 20 -PKG ohos.samples.ecg -c -g -t -p -f
```
----------------------------------Print START------------------------------------
order:0 cpu0freq=1992000
order:1 cpu0load=23.469387
order:2 cpu1freq=1992000
order:3 cpu1load=26.262627
order:4 cpu2freq=1992000
order:5 cpu2load=19.000000
order:6 cpu3freq=1992000
order:7 cpu3load=74.747475
order:8 current_now=-1000.000000
order:9 gpu-thermal=48333.000000
order:10 gpufreq=200000000
order:11 gpuload=0.000000
order:12 soc-thermal=48888.000000
order:13 timestamp=1501925596847
order:14 voltage_now=4123456.000000
----------------------------------Print END--------------------------------------
----------------------------------Print START------------------------------------
order:0 cpu0freq=1992000
order:1 cpu0load=33.673470
order:2 cpu1freq=1992000
order:3 cpu1load=19.801979
order:4 cpu2freq=1992000
order:5 cpu2load=37.755100
order:6 cpu3freq=1992000
order:7 cpu3load=55.555553
order:8 current_now=-1000.000000
order:9 gpu-thermal=48333.000000
order:10 gpufreq=200000000
order:11 gpuload=0.000000
order:12 soc-thermal=48888.000000
order:13 timestamp=1501925597848
order:14 voltage_now=4123456.000000
----------------------------------Print END--------------------------------------
```
>3、执行完毕后会在data/local/tmp生成data.csv文件，每次执行命令覆盖写入
```bash
# cat /data/local/tmp/data.csv
cpu0freq,cpu0load,cpu1freq,cpu1load,cpu2freq,cpu2load,cpu3freq,cpu3load,current_now,gpu-thermal,gpufreq,gpuload,soc-thermal,timestamp,voltage_now
1992000,-1.000000,1992000,-1.000000,1992000,-1.000000,1992000,-1.000000,-1000.000000,48333.000000,200000000,0.000000,49444.000000,1501925677010,4123456.000000
1992000,16.326530,1992000,22.680412,1992000,62.626263,1992000,41.836735,-1000.000000,48333.000000,200000000,0.000000,48888.000000,1501925678011,4123456.000000
1992000,16.326530,1992000,35.353535,1992000,50.505051,1992000,42.857143,-1000.000000,48333.000000,200000000,0.000000,49444.000000,1501925679013,4123456.000000
```
---

## 参数说明

| 命令   | 功能                   |是否必选|
| :-----| :--------------------- |:-----|
| -N    | 设置采集次数             |是|
| -PKG  | 设置包名                |否|
| -PID  | 设置进程pid(对于ram适用) |否|
| -c    | 是否采集cpu             |否|
| -g    | 是否采集gpu             |否|
| -f    | 是否采集fps             |否|
| -t    | 是否采集温度             |否|
| -p    | 是否采集电流             |否|
| -r    | 是否采集内存             |否|
| -snapshot    | 是否截图            |否|

---
## 构建方式
>1、在OpenHarmony系统根目录执行全量编译命令（RK3568为例）： ./build.sh --product-name rk3568 --ccache <br>
