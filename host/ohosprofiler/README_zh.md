# HOST子系统

## 简介

工具链一致性平台整体分为HOST子系统和设备端子系统两部分。HOST子系统最终组件为IDE工具软件，
内部又分为UI绘制、设备管理、进程管理、插件管理、数据导入、数据存储、 数据分析、Session管理、配置管理等模块。



## 目录

```
ohos.devtools/
├── views          #GUI视图管理模块
│   ├── layout     #UI布局框架
│   ├── charts     #Chart绘制模块
│   ├── common     #UI公共模块
│   └── resource   #UI资源模块
├── services       #业务管理模块
│   ├── memory     #Memory业务模块
│   ├── diskio     #diskio业务模块
│   ├── network    #network业务模块
│   ├── ftrace     #ftrace业务模块
│   ├── bytrace    #bytrace业务模块
│   ├── hiperf     #hiperf业务模块
│   └── power      #power业务模块
├── database       #数据管理模块
│   ├── transport  #数据通信模块
│   └── utils      #工具类模块
├──
```

## 使用

1.准备开发工具，可以使用【IntelliJ IDEA社区版】或者【IntelliJ IDEA旗舰版】

2.使用开发工具导入项目，如果你是第一次使用是需要加载ideaIC的，这个文件其实就是
IntelliJ Idea社区版，在插件调试时Idea会同步启动一个安装了插件的社区版Idea，由于比较大，会比较耗时

3.本地打包：进入主界面后，选择右边栏Gradle，在intellij下点击buildPlugin。
执行成功默认会在项目根目录创建build文件，打包后的插件就在\build\distributions目录下。
无依赖的插件是JAR包，带有依赖的插件是ZIP格式。无论哪种都可以直接在IDEA Plugin中安装。
打开File➡Settings➡Plugins，选择齿轮按钮，选择Install Plugin from Disk（从本地磁盘中安装）

IDEA Platform相关问题请参见：
https://plugins.jetbrains.com/docs/intellij/welcome.html?from=jetbrains.org


## 版权声明

```
# Copyright (c) 2021 Huawei Device Co., Ltd.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
```
