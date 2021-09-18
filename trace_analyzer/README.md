# trace_streamer开发环境搭建和编译运行指引

本应用使用gn作为构建工具，支持在linux环境同时编译linux，windows和mac使用QtCreator作为开发IDE
## 1、开发环境
ubuntu使用vscode，windows和mac使用QtCreator
## 2、参与版本编译
在上级目录的ohos.build文件中，module_list列表中添加
```
"//developtools/profiler/trace_analyzer:trace_streamer"
```
在test_list列表中添加"//developtools/profiler/trace_analyzer/test:unittest"来编译UT
在根目录third_party/sqlite/BUILD.gn文件中，在ohos_shared_library("sqlite")选型中添加
```
visibility += [ "//developtools/profiler/trace_analyzer/*" ]
```
去除
```
cflags_c = [
    "-fvisibility=hidden",
  ]
```
# 对外部的依赖
本应用依赖与sqlite，protobuf(htrace解析部分依赖) 

本应用同时依赖于//developtools/profiler/protos/types/plugins/ftrace_data目录下的部分对象ftrace_data_cpp编译目标来支持htrace的解析 

ts.gni文件用来区别独立编译和build目录下的ohos.gni用来支持独立编译，开发者需自行编译相关依赖

### 2.1、 编译linux版应用
在根目录下执行相关命令进行编译

### 2.2、编译Windows版和Mac应用
在项目目录下有pro文件，为QtCreator的工程文件，但部分内容赖在于上面所添加的外部依赖，如果要编译相关平台应用，开发者需自行补充相关工程文件，或者在论坛留言
### 2.3、开始编译

```sh
参与版本编译即可
```

### 3、运行程序
#### 3.1 linux系统

```sh
# Linux 主机可以直接执行：
out/linux/trace_streamer
```
#### 3.2 windows系统
```
Windows环境执行,需添加相关依赖文件
```
