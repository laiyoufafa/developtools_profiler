# 性能调优组件<a name="ZH-CN_TOPIC_0000001149491319"></a>

-   [简介](#section6874544183112)
-   [架构图](#section1514713331342)
-   [目录](#section1742612449345)
-   [说明](#section2165102016359)
    -   [接口说明](#section558917318367)
    -   [使用说明](#section681316903611)
    -   [调测验证](#section35362541215)

-   [相关仓](#section1293495681320)

## 简介<a name="section6874544183112"></a>

性能调优组件包含系统和应用调优框架，旨在为开发者提供一套性能调优平台，可以用来分析内存、性能等问题。

该组件整体分为PC端和设备端两部分，PC端最终作为deveco studio的插件进行发布，内部主要包括分为UI绘制、设备管理、进程管理、插件管理、数据导入、数据存储、 数据分析、Session管理、配置管理等模块；设备端主要包括命令行工具、服务进程、插件集合、应用程序组件等模块。设备端提供了插件扩展能力，对外提供了插件接口，基于该扩展能力可以按需定义自己的能力，并集成到框架中来，目前基于插件能力已经完成了实时内存插件,trace插件。下文会重点对设备端提供的插件能力进行介绍。

## 架构图<a name="section1514713331342"></a>

![](figures/zh-cn_image_0000001162598155.png)

## 目录<a name="section1742612449345"></a>

```
/developtools/profiler
├── device                       # 设备侧代码目录
│   ├── base                     # 基础功能代码
│   │   ├── include              # 基础功能的头文件代码目录
│   |   ├── src                  # 基础功能的源文件代码目录
│   |   └── test                 # 基础功能的测试代码目录
│   ├── cmds                     # 对外命令行模块的代码
│   |   ├── include              # 对外命令行模块的头文件代码目录
│   |   ├── src                  # 对外命令行模块的源文件代码目录
│   |   └── test                 # 对外命令行模块的测试代码目录  
│   └── plugins                  # 插件代码目录
│       ├── api                  # 插件模块对外提供的接口文件代码目录
│       |   ├── include          # 插件模块对外提供的接口头文件代码目录
│       |   └── src              # 插件模块对外提供的接口源文件代码目录
│       ├── memory_plugin        # 内存插件模块代码目录
│       |   ├── include          # 内存插件模块头文件代码目录
│       |   ├── src              # 内存插件模块源文件代码目录
│       |   └── test             # 内存插件模块测试代码目录
│       └── trace_plugin         # trace插件模块代码目录
│           ├── include          # trace插件模块头文件代码目录
│           ├── src              # trace插件模块源文件代码目录
│           └── test             # trace插件模块测试代码目录
├── host                         # 主机侧代码目录
│   └── ohosprofiler             # 主机侧调优模块代码目录
│       └── src                  # 主机侧调优模块源文件代码目录
├── protos                       # 项目中的proto格式文件的代码目录
│   └── innerkits                # 对内部子系统暴露的头文件存放目录
│       └── builtin              # JS应用框架对外暴露JS三方module API接口存放目录
├── trace_analyzer               # bytrace解析模块的代码目录
│   ├── include                  # bytrace解析模块的公共头文件存放目录
│   └── src                      # bytrace解析模块功能源文件存放目录
├── interfaces                   # 项目中接口的代码目录
│   ├── innerkits                # 模块间接口的代码目录
│   └── kits                     # 对外提供接口存放目录
```

## 说明<a name="section2165102016359"></a>

下面针对设备端对外提供的插件扩展能力进行接口和使用说明。

### 接口说明<a name="section558917318367"></a>

下面是设备端插件模块对外提供的接口：

-   PluginModuleCallbacks为插件模块对外提供的回调接口，插件管理模块通过该回调接口列表与每一个插件模块进行交互，每一个新增插件都需要实现该接口列表中的函数。

**表 1**  PluginModuleCallbacks接口列表

<a name="table214mcpsimp"></a>
<table><thead align="left"><tr id="row221mcpsimp"><th class="cellrowborder" valign="top" width="30%" id="mcps1.2.4.1.1"><p id="p223mcpsimp"><a name="p223mcpsimp"></a><a name="p223mcpsimp"></a>接口名</p>
</th>
<th class="cellrowborder" valign="top" width="30.020000000000003%" id="mcps1.2.4.1.2"><p id="p225mcpsimp"><a name="p225mcpsimp"></a><a name="p225mcpsimp"></a>类型</p>
</th>
<th class="cellrowborder" valign="top" width="39.98%" id="mcps1.2.4.1.3"><p id="p227mcpsimp"><a name="p227mcpsimp"></a><a name="p227mcpsimp"></a>描述</p>
</th>
</tr>
</thead>
<tbody><tr id="row229mcpsimp"><td class="cellrowborder" valign="top" width="30%" headers="mcps1.2.4.1.1 "><p id="p231mcpsimp"><a name="p231mcpsimp"></a><a name="p231mcpsimp"></a>PluginModuleCallbacks::onPluginSessionStart</p>
</td>
<td class="cellrowborder" valign="top" width="30.020000000000003%" headers="mcps1.2.4.1.2 "><p id="p233mcpsimp"><a name="p233mcpsimp"></a><a name="p233mcpsimp"></a>int (*PluginSessionStartCallback)(const uint8_t* configData, uint32_t configSize);</p>
</td>
<td class="cellrowborder" valign="top" width="39.98%" headers="mcps1.2.4.1.3 "><a name="ul1467809165112"></a><a name="ul1467809165112"></a><ul id="ul1467809165112"><li>功能：<p id="p1208131116548"><a name="p1208131116548"></a><a name="p1208131116548"></a>插件会话开始接口，开始插件会话时会被调用，用来下发插件配置</p>
</li><li>输入参数：<p id="p104087130518"><a name="p104087130518"></a><a name="p104087130518"></a>configData：配置信息内存块起始地址</p>
<p id="p32731219115114"><a name="p32731219115114"></a><a name="p32731219115114"></a>configSize：配置信息内存块字节数</p>
</li><li>返回值：<p id="p4892128135110"><a name="p4892128135110"></a><a name="p4892128135110"></a>0：成功</p>
<p id="p1621513010517"><a name="p1621513010517"></a><a name="p1621513010517"></a>-1：失败</p>
</li></ul>
</td>
</tr>
<tr id="row236mcpsimp"><td class="cellrowborder" valign="top" width="30%" headers="mcps1.2.4.1.1 "><p id="p238mcpsimp"><a name="p238mcpsimp"></a><a name="p238mcpsimp"></a>PluginModuleCallbacks::onPluginReportResult</p>
</td>
<td class="cellrowborder" valign="top" width="30.020000000000003%" headers="mcps1.2.4.1.2 "><p id="p240mcpsimp"><a name="p240mcpsimp"></a><a name="p240mcpsimp"></a>int (*PluginReportResultCallback)(uint8_t* bufferData, uint32_t bufferSize);</p>
</td>
<td class="cellrowborder" valign="top" width="39.98%" headers="mcps1.2.4.1.3 "><a name="ul1046263617524"></a><a name="ul1046263617524"></a><ul id="ul1046263617524"><li>功能：<p id="p621321105415"><a name="p621321105415"></a><a name="p621321105415"></a>插件结果上报接口类型，当任务下发后，框架采集任务会周期性调用此接口请求回填数据</p>
</li><li>输入参数:<p id="p12796339115211"><a name="p12796339115211"></a><a name="p12796339115211"></a>bufferData: 存放结果的内存缓冲区起始地址</p>
<p id="p1163214085219"><a name="p1163214085219"></a><a name="p1163214085219"></a>bufferSize: 存放结果的内存缓冲区的字节数</p>
</li><li>返回值：<p id="p114391444135212"><a name="p114391444135212"></a><a name="p114391444135212"></a>大于0：已经填充的内存字节数</p>
<p id="p117881419526"><a name="p117881419526"></a><a name="p117881419526"></a>等于0：没有填充任何内容</p>
<p id="p7337174625218"><a name="p7337174625218"></a><a name="p7337174625218"></a>小于0：失败</p>
</li></ul>
</td>
</tr>
<tr id="row243mcpsimp"><td class="cellrowborder" valign="top" width="30%" headers="mcps1.2.4.1.1 "><p id="p245mcpsimp"><a name="p245mcpsimp"></a><a name="p245mcpsimp"></a>PluginModuleCallbacks::onPluginSessionStop</p>
</td>
<td class="cellrowborder" valign="top" width="30.020000000000003%" headers="mcps1.2.4.1.2 "><p id="p247mcpsimp"><a name="p247mcpsimp"></a><a name="p247mcpsimp"></a>int (*PluginSessionStopCallback)();</p>
</td>
<td class="cellrowborder" valign="top" width="39.98%" headers="mcps1.2.4.1.3 "><a name="ul975785275211"></a><a name="ul975785275211"></a><ul id="ul975785275211"><li>功能：<p id="p11471347135914"><a name="p11471347135914"></a><a name="p11471347135914"></a>采集会话结束接口</p>
</li><li>返回值：<p id="p4301105945210"><a name="p4301105945210"></a><a name="p4301105945210"></a>0：成功</p>
<p id="p145541757125214"><a name="p145541757125214"></a><a name="p145541757125214"></a>-1：失败</p>
</li></ul>
</td>
</tr>
<tr id="row250mcpsimp"><td class="cellrowborder" valign="top" width="30%" headers="mcps1.2.4.1.1 "><p id="p252mcpsimp"><a name="p252mcpsimp"></a><a name="p252mcpsimp"></a>PluginModuleCallbacks::onRegisterWriterStruct</p>
</td>
<td class="cellrowborder" valign="top" width="30.020000000000003%" headers="mcps1.2.4.1.2 "><p id="p254mcpsimp"><a name="p254mcpsimp"></a><a name="p254mcpsimp"></a>int (*RegisterWriterStructCallback)(WriterStruct* writer);</p>
</td>
<td class="cellrowborder" valign="top" width="39.98%" headers="mcps1.2.4.1.3 "><a name="ul11459712105318"></a><a name="ul11459712105318"></a><ul id="ul11459712105318"><li>功能：<p id="p16725350135912"><a name="p16725350135912"></a><a name="p16725350135912"></a>采集框架注册写数据接口，当插件管理模块向插件注册此接口，插件可以主动调用write句柄，进行写入数据</p>
</li><li>输入参数：<p id="p741312324531"><a name="p741312324531"></a><a name="p741312324531"></a>writer 写者指针</p>
</li><li>返回值：<p id="p1937282325319"><a name="p1937282325319"></a><a name="p1937282325319"></a>0：成功</p>
<p id="p3613421185314"><a name="p3613421185314"></a><a name="p3613421185314"></a>-1：失败</p>
</li></ul>
</td>
</tr>
</tbody>
</table>

-   WriterStruct是上面onRegisterWriterStruct接口中的参数，主要实现写数据接口，将插件中采集的数据通过该接口进行写入。

**表 2**  WriterStruct接口列表

<a name="table1469161115240"></a>
<table><thead align="left"><tr id="row124691911182413"><th class="cellrowborder" valign="top" width="30%" id="mcps1.2.4.1.1"><p id="p14469151118247"><a name="p14469151118247"></a><a name="p14469151118247"></a>接口名</p>
</th>
<th class="cellrowborder" valign="top" width="30.020000000000003%" id="mcps1.2.4.1.2"><p id="p114691119249"><a name="p114691119249"></a><a name="p114691119249"></a>类型</p>
</th>
<th class="cellrowborder" valign="top" width="39.98%" id="mcps1.2.4.1.3"><p id="p134701611172413"><a name="p134701611172413"></a><a name="p134701611172413"></a>描述</p>
</th>
</tr>
</thead>
<tbody><tr id="row5470201102420"><td class="cellrowborder" valign="top" width="30%" headers="mcps1.2.4.1.1 "><p id="p144702115242"><a name="p144702115242"></a><a name="p144702115242"></a>WriterStruct::write</p>
</td>
<td class="cellrowborder" valign="top" width="30.020000000000003%" headers="mcps1.2.4.1.2 "><p id="p347010119246"><a name="p347010119246"></a><a name="p347010119246"></a>long (*WriteFuncPtr)(WriterStruct* writer, const void* data, size_t size);</p>
</td>
<td class="cellrowborder" valign="top" width="39.98%" headers="mcps1.2.4.1.3 "><a name="ul1783144245320"></a><a name="ul1783144245320"></a><ul id="ul1783144245320"><li>功能：<p id="p11675175517597"><a name="p11675175517597"></a><a name="p11675175517597"></a>写接口，将插件中采集的数据通过writer进行写入</p>
</li><li>输入参数：<p id="p1812314462537"><a name="p1812314462537"></a><a name="p1812314462537"></a>writer：写者指针</p>
<p id="p498224618534"><a name="p498224618534"></a><a name="p498224618534"></a>data：数据缓冲区首字节指针</p>
<p id="p368519478533"><a name="p368519478533"></a><a name="p368519478533"></a>size:  数据缓冲区的字节数</p>
</li><li>返回值：<p id="p162857527531"><a name="p162857527531"></a><a name="p162857527531"></a>0：成功</p>
<p id="p101121150185318"><a name="p101121150185318"></a><a name="p101121150185318"></a>-1：失败</p>
</li></ul>
</td>
</tr>
<tr id="row84706116243"><td class="cellrowborder" valign="top" width="30%" headers="mcps1.2.4.1.1 "><p id="p2047081142415"><a name="p2047081142415"></a><a name="p2047081142415"></a>WriterStruct::flush</p>
</td>
<td class="cellrowborder" valign="top" width="30.020000000000003%" headers="mcps1.2.4.1.2 "><p id="p194111838267"><a name="p194111838267"></a><a name="p194111838267"></a>bool (*FlushFuncPtr)(WriterStruct* writer);</p>
</td>
<td class="cellrowborder" valign="top" width="39.98%" headers="mcps1.2.4.1.3 "><a name="ul9562185717538"></a><a name="ul9562185717538"></a><ul id="ul9562185717538"><li>功能：<p id="p744185910594"><a name="p744185910594"></a><a name="p744185910594"></a>触发数据上传接口</p>
</li><li>输入参数:<p id="p69601111504"><a name="p69601111504"></a><a name="p69601111504"></a>writer：写者指针</p>
</li><li>返回值：<p id="p970375016"><a name="p970375016"></a><a name="p970375016"></a>true：成功</p>
<p id="p1314495407"><a name="p1314495407"></a><a name="p1314495407"></a>false：失败</p>
</li></ul>
</td>
</tr>
</tbody>
</table>

-   下面是插件模块对外提供的总入口，主要包括表1中的插件模块回调函数以及插件名称、插件模块需要申请的内存大小。

**表 3**  PluginModuleStruct接口列表

<a name="table14418172393018"></a>
<table><thead align="left"><tr id="row14188236300"><th class="cellrowborder" valign="top" width="30%" id="mcps1.2.4.1.1"><p id="p041820239304"><a name="p041820239304"></a><a name="p041820239304"></a>接口名</p>
</th>
<th class="cellrowborder" valign="top" width="30.020000000000003%" id="mcps1.2.4.1.2"><p id="p34181823153010"><a name="p34181823153010"></a><a name="p34181823153010"></a>类型</p>
</th>
<th class="cellrowborder" valign="top" width="39.98%" id="mcps1.2.4.1.3"><p id="p10418023123011"><a name="p10418023123011"></a><a name="p10418023123011"></a>描述</p>
</th>
</tr>
</thead>
<tbody><tr id="row16418623143016"><td class="cellrowborder" valign="top" width="30%" headers="mcps1.2.4.1.1 "><p id="p625018817318"><a name="p625018817318"></a><a name="p625018817318"></a>PluginModuleStruct::callbacks</p>
</td>
<td class="cellrowborder" valign="top" width="30.020000000000003%" headers="mcps1.2.4.1.2 "><p id="p162501689312"><a name="p162501689312"></a><a name="p162501689312"></a>PluginModuleCallbacks*</p>
</td>
<td class="cellrowborder" valign="top" width="39.98%" headers="mcps1.2.4.1.3 "><p id="p122501085317"><a name="p122501085317"></a><a name="p122501085317"></a>功能：定义插件回调函数列表</p>
</td>
</tr>
<tr id="row241952313015"><td class="cellrowborder" valign="top" width="30%" headers="mcps1.2.4.1.1 "><p id="p325018863113"><a name="p325018863113"></a><a name="p325018863113"></a>PluginModuleStruct::name</p>
</td>
<td class="cellrowborder" valign="top" width="30.020000000000003%" headers="mcps1.2.4.1.2 "><p id="p4250138123112"><a name="p4250138123112"></a><a name="p4250138123112"></a>C style string</p>
</td>
<td class="cellrowborder" valign="top" width="39.98%" headers="mcps1.2.4.1.3 "><p id="p425019883115"><a name="p425019883115"></a><a name="p425019883115"></a>功能：定义插件名称</p>
</td>
</tr>
<tr id="row49481758173013"><td class="cellrowborder" valign="top" width="30%" headers="mcps1.2.4.1.1 "><p id="p1125010813316"><a name="p1125010813316"></a><a name="p1125010813316"></a>PluginModuleStruct::resultBufferSizeHint</p>
</td>
<td class="cellrowborder" valign="top" width="30.020000000000003%" headers="mcps1.2.4.1.2 "><p id="p142509810319"><a name="p142509810319"></a><a name="p142509810319"></a>uint32_t</p>
</td>
<td class="cellrowborder" valign="top" width="39.98%" headers="mcps1.2.4.1.3 "><p id="p1525112853115"><a name="p1525112853115"></a><a name="p1525112853115"></a>功能：用于提示插件管理模块调用数据上报接口时使用的内存缓冲区字节数</p>
</td>
</tr>
</tbody>
</table>

### 使用说明<a name="section681316903611"></a>

下面介绍在设备端基于性能调优框架提供的插件能力，新增一个插件涉及到的关键开发步骤：

1.  编写proto数据定义文件_plugin\_data.proto_，定义数据源格式，数据源格式决定了插件上报哪些数据：

    ```
    message PluginData {
        int32 pid = 1;
        string name = 2;
        uint64 count1 = 3;
        uint64 count2 = 4;
        uint64 count3 = 5;
        ......
    }
    ```

2.  编写数据源配置文件_plugin\_config.proto_，采集的行为可以根据配置进行变化，可以设置数据源上报间隔等信息：

    ```
    message PluginConfig {
        int32 pid = 1;
        bool report_interval = 2;
        int report_counter_id_1 = 3;
        int report_counter_id_2 = 4;
        ......
    }
    ```

3.  定义PluginModuleCallbacks实现插件回调接口；定义PluginModuleStruct类型的g\_pluginModule全局变量，注册插件信息。

    ```
    static PluginModuleCallbacks callbacks = {
        PluginSessionStart,
        PluginReportResult,
        PluginSessionStop,
    };
    PluginModuleStruct g_pluginModule = {&callbacks, "test-plugin", MAX_BUFFER_SIZE};
    ```

4.  通过PluginSessionStart（名字可以自己定义）实现插件回调接口列表的onPluginSessionStart接口，主要处理插件的开始流程。

    ```
    int PluginSessionStart(const uint8_t* configData, uint32_t configSize)
    {
        ......
        return 0;
    } 
    ```

5.  通过PluginReportResult（名字可以自己定义）实现插件回调接口列表的onPluginReportResult接口，将插件内部采集的信息通过该接口进行上报：

    ```
    int PluginReportResult(uint8_t* bufferData, uint32_t bufferSize)
    {
        ......
        return 0;
    } 
    ```

6.  通过PluginSessionStop（名字可以自己定义）实现插件回调接口列表的onPluginSessionStop接口，主要进行插件停止后的操作流程。

    ```
    int PluginSessionStop()
    {
        ......
        return 0;
    }
    ```

7.  编写proto gn构建脚本, 生成protobuf源文件，protobuf源文件编译生成目标文件:

    ```
    action("plugin_cpp_gen") {
      script = "${OHOS_PROFILER_DIR}/build/protoc.sh"  //依赖的编译工具链
      sources = [   //定义的插件相关的proto文件，比如插件配置文件、插件数据对应的proto文件
        "plugin_data.proto",
        "plugin_config.proto",
      ]
      outputs = [    //通过protoc编译生成的结果文件
        "plugin_data.pb.h",
        "plugin_data.pb.cc",
        "plugin_config.pb.h",
        "plugin_config.pb.cc",
      ]
      args = [
        "--cpp_out",
        "$proto_rel_out_dir",
        "--proto_path",
        rebase_path(".", root_build_dir),
      ]
      deps = [
        "${OHOS_PROFILER_3RDPARTY_PROTOBUF_DIR}:protoc(${host_toolchain})",
      ]
    }
    ohos_source_set("plug_cpp") {   //将定义的proto文件生成cpp文件
      deps = [
        ":plugin_cpp_gen",
      ]
      public_deps = [
        "${OHOS_PROFILER_3RDPARTY_PROTOBUF_DIR}:protobuf",
        "${OHOS_PROFILER_3RDPARTY_PROTOBUF_DIR}:protobuf_lite",
      ]
      include_dirs = [ "$proto_out_dir" ]
      sources = [   //目标plug_cpp中包括的源文件
        "plugin_data.pb.h",
        "plugin_data.pb.cc",
        "plugin_config.pb.h",
        "plugin_config.pb.cc",
      ]
    }
    ```

8.  编写插件GN构建脚本:

    ```
    ohos_shared_library("***plugin") {
      output_name = "***plugin"
      sources = [
        "src/***plugin.cpp",  //插件中的源文件
      ]
      include_dirs = [
        "../api/include",
        "${OHOS_PROFILER_DIR}/device/base/include",
      ]
      deps = [
        "${OHOS_PROFILER_3RDPARTY_PROTOBUF_DIR}:protobuf_lite",
        "${OHOS_PROFILER_3RDPARTY_PROTOBUF_DIR}:protoc_lib",
        "${OHOS_PROFILER_DIR}/protos/types/plugins/**:plug_cpp",  //上面ohos_source_set中生成的plug_cpp
      ]
      install_enable = true
      subsystem_name = "${OHOS_PROFILER_SUBSYS_NAME}"
    }
    ```


### 调测验证：<a name="section35362541215"></a>

插件动态库生成后，可以自己编写测试代码，通过dlopen加载动态库，并调用上面代码中实现的插件模块回调函数进行验证。

```
int main(int argc, char** argv)
{
    void* handle;
    PluginModuleStruct* memplugin;
    handle = dlopen("./libplugin.z.so", RTLD_LAZY);   //动态打开上面生成的插件动态库
    if (handle == nullptr) {
        HILOGD("dlopen err:%s.", dlerror());
        return 0;
    }
     memplugin = (PluginModuleStruct*)dlsym(handle, "g_pluginModule");  //获取开发步骤3中定义的g_pluginModule全局变量
     //check memplugin->callbacks   // 通过该指针调用上面开发步骤3中定义的回调函数
     return 0;
```

## 相关仓<a name="section1293495681320"></a>

[研发工具链子系统]()

**developtools\_profiler**

[developtools\_hdc](https://gitee.com/openharmony/developtools_hdc)

[developtools\_bytrace](https://gitee.com/openharmony/developtools_bytrace)
