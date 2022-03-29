# 如何独立编译Trace_streamer
尽管本工具(trace_streamer)是在ohos工具箱中的一员，但你依然可以独立编译此工具。

本工具可以编译ohos, linux, mac, windows版本。

本工具默认编译方式是使用gn，具体编译方式在README.md文件中有说明.

本工具还支持使用QtCreator来编译。

src/trace_streamer.pro 是工程文件，编译本工具需要依赖Sqlite库和一堆
## 2 准备工程
### 2.1 获取基于proto的文件
序列化二进制的解析依赖于基于proto生成的.pb.cc文件。

这些文件需要你执行以下两步：
#### 2.1.1 预编译工程，获取proto的pb文件
1 编译整个profiler，然后在工程的out目录下搜索文件，比如trace_plugin_result.pb.cc

你将搜索到类似
```
./xxx/gen/cpp/developtools/profiler/protos/types/plugins/ftrace_data/trace_plugin_result.pb.cc
```
的文件
接下来，将整个xxx/gen/cpp/developtools/profiler/protos/* 目录拷贝到third_party/protogen/ 目录(使用 -rf选项)

你的目录结构当类似如下结构：
```
third_party/protogen/types/plugins/ftrace_data/*.pb.cc
third_party/sqlite/*.
third_party/protobuf/*
```
### 2.2 获取第三方依赖库
从third_party工程下获取protobuf目录到本地目录  
从third_party工程下获取sqlite3目录到本地目录  
之后，你的目录当如下所示  
trace_streamer/third_party/protobuf  
trace_streamer/third_party/sqlite 
# 3 （linux和ohos平台）使用gn编译TraceStreamer 
## 3.1 使用预先准备好的BUILD.GN
```
cp prebuilts/sqlite3build.gn third_party/sqlite/BUILD.gn
cp prebuilts/protobufbuild.gn third_party/protobuf/BUILD.gn
```
## 3.2 准备gn
在自己的项目中使用gn，必须遵循以下要求：  
在根目录创建.gn文件，该文件用于指定BUILDCONFIG.gn文件的位置；  
在BUILDCONFIG.gn中指定编译时使用的编译工具链；  
在独立的gn文件中定义编译使用的工具链；  
在项目根目录下创建BUILD.gn文件，指定编译的目标。  
```
cp prebuilts/gn ./
```
## 3.3 执行编译
./build.sh linux debug  
或./build.sh linux debug  
./build.sh将直接编译linux的release版本
# 4 编译Windows版本
## 4.1 编译依赖文件
### 4.1.1 编译SqliteLib
使用QtCreator打开prebuiltsprebuilts/buildprotobuf/sqlite.pro
### 4.1.2 编译ProtobufLib
使用QtCreator打开prebuilts/buildprotobuf/protobuf.pro
编译之后，文件结构当如下所示：
```
lib
├── linux
│   ├── libprotobuf.a
│   └── libsqlite.a
├── linux_debug
│   ├── libprotobuf.a
│   └── libsqlite.a
├── macx
│   ├── libprotobuf.a
│   └── libsqlite.a
├── macx_debug
│   ├── libprotobuf.a
│   └── libsqlite.a
├── windows
│   ├── libprotobuf.a
│   └── libsqlite.a
├── windows_debug
│   ├── libprotobuf.a
│   └── libsqlite.a
└── windows_release
```
## 4.2 编译TraceStreamer
之后，使用QtCreator打开src/trace_streamer.pro，选择合适的构建工具，执行 Ctrl + b 即可编译

编译之后的可执行文件位于out目录
```
- out
---- linux (Linux平台下QtCreator或gn生成)
---- macx (mac平台下QtCreator或gn生成)
---- windows (windows平台下QtCreator或gn生成)
```