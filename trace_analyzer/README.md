# trace_streamer开发环境搭建和编译运行指引

本应用使用gn作为构建工具，支持在linux环境同时编译linux，windows环境的应用，后期会根据开发者需求添加更多的支持。
## 1、开发环境
需要Ubuntu20.04，或者将gcc升级到9.3.0版本（后续会降低版本要求）
## 2、编译
编译前，需搭建本地gn编译环境
```sh
# gn 和 ninja 工具包
buildtools/linux64/
gn 555198版本
ninja 1.10.0版本
clang-format version 10.0.0-4ubuntu1版本
buildtools/clang/bin
buildtools/clang/lib
sqlite文件位于
buildtools/sqlite
```
### 2.1、 编译linux版应用
无需前置操作

### 2.2、编译Windows版应用

需下载MinGW编译工具链
```sh
# 安装 mingw-w64 包，用于在 Linux主机上生成 Windows 目标文件：
sudo apt install mingw-w64
```

### 2.3、开始编译

```sh
# 生成各平台的ninja编译规则文件
tools/build_all_configs.py

# 编译Windows系统的目标文件，windows运行依赖的库在tools/trace_win32_lib
tools/ninja -C out/win_mingw trace_streamer -j 5

# 编译Linux系统的目标文件
tools/ninja -C out/linux_clang trace_streamer -j 5
tools/ninja -C out/debug_linux_clang trace_streamer -j 5
```

### 3、运行程序
#### 3.1 linux系统

```sh
# Linux 主机可以直接执行：
out/linux_clang/trace_streamer
```
#### 3.2 windows系统
```
Windows环境执行,需下载mingw环境，或者使用 wine 执行 
```
