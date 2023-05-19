# 编译UT
本文档给了编译trace_streamer的UT代码的方法。  
直接运行./build.sh test 即可编译代码，此代码不包含src/parser/htrace_parser目录下代码。如要编译src/parser/htrace_parser目录代码，直接运行./build.sh testpb即可。
编译ut时，可能会遇到一些问题，需要将部分代码做如下处理：
v412.pb.h 大约第36行，添加如下内容：
```
#ifdef major
#undef major
#endif
#ifdef minor
#undef minor
#endif
```
js_heap_result.pb.cc文件中schemas替换成resultSchemas。
js_heap_result.pb.cc文件中file_default_instances替换成result_file_default_instances。
gtest-port.h 第286行。
```
#include <sstream>
```
修改为：
```
#undef private
#define private private
#include <sstream>
#undef private
#define private public
```

在ut代码编译完成之后，直接运行./test.sh，可以执行所有ut，显示正确与否。  
在ut执行之后，直接运行./lcov.sh，可以生成覆盖率报告。  
覆盖率报告位于out/test/html目录。  