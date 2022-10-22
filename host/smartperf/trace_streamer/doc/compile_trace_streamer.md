# 如何独立编译Trace_streamer
尽管本工具(trace_streamer)是在ohos工具箱中的一员，但你依然可以独立编译此工具。

本工具可以编译linux, mac, windows, WebAssembly版本。

本工具默认编译方式是使用gn
+ 编译方式
```
./build.sh linux/wasm/macx
```
如果需要编译WebAssembly版本，您需要在prebuilts/目录下安装emsdk
```
git clone https://github.com/juj/emsdk.git --depth=1
cd emsdk
git pull
./emsdk update # this may not work, ignore it
./emsdk install latest
./emsdk activate latest
安装之后，您需要将upstream目录复制到prebuilts/emsdk/emsdk，node复制到prebuilts/emsdk/node
```
安装之后，目录结构当如：
```
prebuilts/emsdk
├── prebuilts/emsdk/emsdk
│   ├── prebuilts/emsdk/emsdk/bin
│   ├── prebuilts/emsdk/emsdk/emscripten
│   │   ├── prebuilts/emsdk/emsdk/emscripten/cache
│   │   ├── prebuilts/emsdk/emsdk/emscripten/cmake
│   │   ├── prebuilts/emsdk/emsdk/emscripten/docs
│   │   ├── prebuilts/emsdk/emsdk/emscripten/media
│   │   ├── prebuilts/emsdk/emsdk/emscripten/node_modules
│   │   ├── prebuilts/emsdk/emsdk/emscripten/__pycache__
│   │   ├── prebuilts/emsdk/emsdk/emscripten/src
│   │   ├── prebuilts/emsdk/emsdk/emscripten/system
│   │   ├── prebuilts/emsdk/emsdk/emscripten/tests
│   │   ├── prebuilts/emsdk/emsdk/emscripten/third_party
│   │   └── prebuilts/emsdk/emsdk/emscripten/tools
│   ├── prebuilts/emsdk/emsdk/include
│   │   └── prebuilts/emsdk/emsdk/include/c++
│   └── prebuilts/emsdk/emsdk/lib
│       └── prebuilts/emsdk/emsdk/lib/clang
└── prebuilts/emsdk/node
    └── prebuilts/emsdk/node/14.18.2_64bit
        ├── prebuilts/emsdk/node/14.18.2_64bit/bin
        ├── prebuilts/emsdk/node/14.18.2_64bit/include
        ├── prebuilts/emsdk/node/14.18.2_64bit/lib
        └── prebuilts/emsdk/node/14.18.2_64bit/share
```
之后调用
```
./build.sh wasm进行编译，您需要将sh脚本进行部分修改，因为这个脚本内置了一些库的下载和解析方式
```