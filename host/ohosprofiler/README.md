# Host Subsystem

## Brief Introduction

The tool chain consistency platform is divided into two parts: Host subsystem and equipment terminal system. 
The final component of host subsystem is ide tool software,It is divided into UI drawing, device management, 
process management, plug-in management, data import, data storage, data analysis, session management, configuration management and other modules.


## 目录

```
ohos.devtools/
├── views          #GUI module
│   ├── layout     #Layout module
│   ├── charts     #Chart module
│   ├── common     #Common module
│   └── resource   #Resource module
├── services       #Services module
│   ├── memory     #Memory module
│   ├── diskio     #diskio module
│   ├── network    #network module
│   ├── ftrace     #ftrace module
│   ├── bytrace    #bytrace module
│   ├── hiperf     #hiperf module
│   └── power      #power module
├── database       #database module
│   ├── transport  #transport module
│   └── utils      #utils module
├──
```

## USE

1.To prepare development tools, you can use IntelliJ idea Community Edition or IntelliJ idea Ultimate Edition

2.Using the development tool to import a project, if you are using it for the first time, you need to load ideaic. 
In fact, this file is IntelliJ idea community version, when debugging plug-ins, idea will synchronously start a 
community version of idea with plug-ins installed, which is relatively large and time-consuming

3.Local packaging: after entering the main interface, select gradle in the right column and click build plug under IntelliJ.
If the execution is successful, the build file will be created in the root directory of the project by default, 
and the packaged plug-ins will be in the directory of build / distributions.The plug-in without dependency is jar package, 
and the plug-in with dependency is ZIP format. Either one can be installed directly in idea plug.
Open file ➡ Settings ➡ Plugins, select the gear button, and select Install plug from disk

For issues related to idea platform, see
https://plugins.jetbrains.com/docs/intellij/welcome.html?from=jetbrains.org


## License

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
