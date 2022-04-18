## 工具介绍

> OpenHarmony性能测试工具,通过采集设备性能指标，对采集数据进行实时展示、导出csv。

## 支持功能

> 当前版本支持如下功能

- 支持RK3568、Hi3516、WGR;
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
 -PKG           set pkg_name of profiler
 -PID           set process id of profiler
 -OUT           set output path of CSV
 -c             get cpuFreq and cpuLoad
 -g             get gpuFreq and gpuLoad
 -d             get ddrFreq
 -f             get fps and fps jitters
 -t             get soc-temp gpu-temp ..
 -p             get current_now and voltage_now
 -r             get ram(pss)
--------------------------------------------------------------------
Example: SP_daemon -N 2 -PKG com.ohos.contacts -c -g -t -p -r
--------------------------------------------------------------------
command exec finished!
```
>2、执行示例命令SP_daemon -N 2 -PKG com.ohos.contacts -c -g -t -p -r
```bash
SP_daemon -N 2 -PKG com.ohos.contacts -c -g -t -p -r
set num:2
set pkg name:com.ohos.contacts

----------------------------------Print START------------------------------------
order:0 ambient=0.000000
order:1 cpu0freq=1700000
order:2 cpu0load=-1.000000
order:3 cpu1freq=1700000
order:4 cpu1load=-1.000000
order:5 cpu2freq=1700000
order:6 cpu2load=-1.000000
order:7 cpu3freq=1700000
order:8 cpu3load=-1.000000
order:9 cpu4freq=2343000
order:10 cpu4load=-1.000000
order:11 cpu5freq=2343000
order:12 cpu5load=-1.000000
order:13 cpu6freq=2343000
order:14 cpu6load=-1.000000
order:15 cpu7freq=2756000
order:16 cpu7load=-1.000000
order:17 current_now=-1213.000000
order:18 gpufreq=260000000
order:19 gpuload=0.000000
order:20 pss=37589
order:21 voltage_now=4.308090
----------------------------------Print END--------------------------------------
----------------------------------Print START------------------------------------
order:0 ambient=0.000000
order:1 cpu0freq=1700000
order:2 cpu0load=13.592233
order:3 cpu1freq=1700000
order:4 cpu1load=42.718445
order:5 cpu2freq=1700000
order:6 cpu2load=55.238094
order:7 cpu3freq=1700000
order:8 cpu3load=16.000000
order:9 cpu4freq=826000
order:10 cpu4load=0.000000
order:11 cpu5freq=826000
order:12 cpu5load=1.923077
order:13 cpu6freq=826000
order:14 cpu6load=0.961538
order:15 cpu7freq=2756000
order:16 cpu7load=99.038460
order:17 current_now=-1139.000000
order:18 gpufreq=260000000
order:19 gpuload=0.000000
order:20 pss=37589
order:21 voltage_now=4.308323
----------------------------------Print END--------------------------------------
:/data/local/tmp #
```
>3、执行完毕后会在data/local/tmp生成data.csv文件，每次执行命令覆盖写入
```bash
:/data/local/tmp # cat data.csv
ambient,cpu0freq,cpu0load,cpu1freq,cpu1load,cpu2freq,cpu2load,cpu3freq,cpu3load,cpu4freq,cpu4load,cpu5freq,cpu5load,cpu6freq,cpu6load,cpu7freq,cpu7load,current_now,gpufreq,gpuload,pss,voltage_now
0.000000,1700000,-1.000000,1700000,-1.000000,1700000,-1.000000,1700000,-1.000000,2343000,-1.000000,2343000,-1.000000,2343000,-1.000000,2756000,-1.000000,-1213.000000,260000000,0.000000,37589,4.308090
0.000000,1700000,13.592233,1700000,42.718445,1700000,55.238094,1700000,16.000000,826000,0.000000,826000,1.923077,826000,0.961538,2756000,99.038460,-1139.000000,260000000,0.000000,37589,4.308323
:/data/local/tmp #
```
---

## 参数说明

| 命令   | 功能                   |是否必选|
| :-----| :--------------------- |:-----|
| -N    | 设置采集次数             |是|
| -PKG  | 设置包名                | 否|
| -PID  | 设置进程pid(对于ram适用) |否|
| -c    | 是否采集cpu             | 否|
| -g    | 是否采集gpu             |否|
| -f    | 是否采集fps             |否|
| -t    | 是否采集温度             |否|
| -p    | 是否采集电流             |否|
| -r    | 是否采集内存             |否|


---
## 构建方式
>1、在OpenHarmony系统根目录执行全量编译命令（RK3568为例）： ./build.sh --product-name rk3568 --ccache <br>
