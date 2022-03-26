## 工具介绍

OpenHarmony性能测试工具,通过采集设备性能指标，对采集数据进行实时展示、导出csv。

## 支持功能

> 当前版本支持如下功能

- 支持RK3568、Hi3516、WGR;
- 支持Shell启动;
- 支持采集整机CPU、GPU、DDR、POWER、TEMPERATURE、应用的FPS、RAM;

## 使用方式

**1、GP_deamon --help**
```bash
> hdc_std shell
:/ # cd data/local/tmp
:/data/local/tmp # ./SP_daemon --help
usage: ./SP_daemon <options> <arguments>
--------------------------------------------------------------------
These are common commands list:
 -N      set num of profiler <must be non-null>
 -PKG    set pkg_name of profiler
 -PID    set process id of profiler
 -c      get cpuFreq and cpuLoad
 -g      get gpuFreq and gpuLoad
 -d      get ddrFreq
 -f      get fps and fps jitters <dependent on hidumper capability>
 -t      get soc-temp gpu-temp ..
 -p      get current_now and voltage_now
 -r      get ram(pss)
--------------------------------------------------------------------
Example: ./SP_daemon -N 2 -PKG com.ohos.contacts -c -g -t -p -r
--------------------------------------------------------------------
```
**2、SP_daemon -N 2 -PKG com.ohos.contacts -c -g -t -p -r**
```bash
:/data/local/tmp # ./SP_daemon -N 2 -PKG com.ohos.contacts -c -g -t -p -r
set num:2
set pkg name:com.ohos.contacts

----------------------------------print_gp_map START------------------------------------
print_gp_map:0 ambient=0.000000
print_gp_map:1 cpu0freq=1700000
print_gp_map:2 cpu0load=-1.000000
print_gp_map:3 cpu1freq=1700000
print_gp_map:4 cpu1load=-1.000000
print_gp_map:5 cpu2freq=1700000
print_gp_map:6 cpu2load=-1.000000
print_gp_map:7 cpu3freq=1700000
print_gp_map:8 cpu3load=-1.000000
print_gp_map:9 cpu4freq=2343000
print_gp_map:10 cpu4load=-1.000000
print_gp_map:11 cpu5freq=2343000
print_gp_map:12 cpu5load=-1.000000
print_gp_map:13 cpu6freq=2343000
print_gp_map:14 cpu6load=-1.000000
print_gp_map:15 cpu7freq=2756000
print_gp_map:16 cpu7load=-1.000000
print_gp_map:17 current_now=-1213.000000
print_gp_map:18 gpufreq=260000000
print_gp_map:19 gpuload=0.000000
print_gp_map:20 pss=37589
print_gp_map:21 voltage_now=4.308090
----------------------------------print_gp_map END--------------------------------------
----------------------------------print_gp_map START------------------------------------
print_gp_map:0 ambient=0.000000
print_gp_map:1 cpu0freq=1700000
print_gp_map:2 cpu0load=13.592233
print_gp_map:3 cpu1freq=1700000
print_gp_map:4 cpu1load=42.718445
print_gp_map:5 cpu2freq=1700000
print_gp_map:6 cpu2load=55.238094
print_gp_map:7 cpu3freq=1700000
print_gp_map:8 cpu3load=16.000000
print_gp_map:9 cpu4freq=826000
print_gp_map:10 cpu4load=0.000000
print_gp_map:11 cpu5freq=826000
print_gp_map:12 cpu5load=1.923077
print_gp_map:13 cpu6freq=826000
print_gp_map:14 cpu6load=0.961538
print_gp_map:15 cpu7freq=2756000
print_gp_map:16 cpu7load=99.038460
print_gp_map:17 current_now=-1139.000000
print_gp_map:18 gpufreq=260000000
print_gp_map:19 gpuload=0.000000
print_gp_map:20 pss=37589
print_gp_map:21 voltage_now=4.308323
----------------------------------print_gp_map END--------------------------------------
:/data/local/tmp #
```
**3、执行完毕后会在data/local/tmp生成data.csv文件，每次执行命令覆盖写入**
```bash
:/data/local/tmp # cat data.csv
ambient,cpu0freq,cpu0load,cpu1freq,cpu1load,cpu2freq,cpu2load,cpu3freq,cpu3load,cpu4freq,cpu4load,cpu5freq,cpu5load,cpu6freq,cpu6load,cpu7freq,cpu7load,current_now,gpufreq,gpuload,pss,voltage_now
0.000000,1700000,-1.000000,1700000,-1.000000,1700000,-1.000000,1700000,-1.000000,2343000,-1.000000,2343000,-1.000000,2343000,-1.000000,2756000,-1.000000,-1213.000000,260000000,0.000000,37589,4.308090
0.000000,1700000,13.592233,1700000,42.718445,1700000,55.238094,1700000,16.000000,826000,0.000000,826000,1.923077,826000,0.961538,2756000,99.038460,-1139.000000,260000000,0.000000,37589,4.308323
:/data/local/tmp #
```
---

## 选项说明

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
| -f1   | 采集fps(视频应用)        |否|

---
## 构建方式
> 当前未在系统内集成，需拉取openHarmony代码到本地，将本代码拷贝到目录里，并修改项目build.gn文件以及对应子系统的bundle.json文件，然后编译。
> 例：编RK3568版本
第一步：将本代码放置\foundation\graphic\standard\rosen\samples\opensourcesp目录下
第二步：修改本代码中的build.gn文件
第三步：修改对应子系统bundle.json文件  \foundation\graphic\standard
在文件中加入代码路径以及编译出的二进制文件名称
"build": {
    "sub_component": [
        ....,
        "//foundation/graphic/standard/rosen/samples/opensourcesp:SP_daemon"
    ]
}
第四步：执行全量编译命令： ./build.sh --product-name rk3568 --ccache 

### 推送方式

> 可以使用项目目录里面已经编译好的SP_daemon推到RK3568、Hi3516试用<br>
```shell
hdc_std shell mount -o rw,remount /
hdc_std file send SP_daemon /data/local/tmp
hdc_std shell chmod a+x /data/local/tmp/SP_daemon
```