# Copyright (C) 2021 Huawei Device Co., Ltd.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
QT -= gui core
TEMPLATE = app
#TEMPLATE = lib
CONFIG += c++17 console
#CONFIG += c++17 lib
#CONFIG += c++17
CONFIG += WITHRPC
TARGET = trace_streamer
DEFINES += SUPPORTTHREAD

#CONFIG += release

DEFINES += HAVE_PTHREAD
DEFINES += _LIBCPP_DISABLE_AVAILABILITY

DEFINES += HAVE_PTHREAD
ROOTSRCDIR = $$PWD/../

#QMAKE_CXXFLAGS =-ftrapv -fPIE -fstack-protector-strong -fstack-protector-all -D_FORTIFY_SOURCE=2 -O2
#QMAKE_CFLAGS=-ftrapv -fPIE -fstack-protector-strong -fstack-protector-all -D_FORTIFY_SOURCE=2 -O2
!unix{
#QMAKE_LFLAGS=-fpie  -Wl,-rpath=\$ORIGIN/.
} else {
#QMAKE_LFLAGS=-fpie
#QMAKE_LFLAGS=-fpie -Wl,-z,noexecstack -Wl,-z,now -Wl,-rpath=\$ORIGIN/. -Wl,-z,relro
}
include($$PWD/multi_platform/global.pri)
INCLUDEPATH += $$PWD/include \
    $$PWD/../third_party/protobuf/src \
    $$PWD/../third_party/sqlite/include \
    $$PWD/../third_party/protogen/gen \
    $$PWD/../third_party/protogen/gen/types/plugins/memory_data \
    $$PWD/../third_party/protogen/gen/types/plugins/ftrace_data \
    $$PWD/../third_party/protogen/gen/types/plugins/hilog_data \
    $$PWD/../third_party/protogen/gen/types/plugins/native_hook \
    $$PWD/../third_party/protogen/gen/types/plugins/hidump_data


include($$PWD/trace_streamer/trace_streamer.pri)
include($$PWD/base/base.pri)
WITHRPC{
DEFINES += WIN32_LEAN_AND_MEAN
include($$PWD/rpc/rpc.pri)
INCLUDEPATH += $$PWD/rpc
!unix{
LIBS += -lws2_32
}
}
include($$PWD/filter/filter.pri)
include($$PWD/parser/parser.pri)
include($$PWD/multi_platform/protogen.pri)
include($$PWD/table/table.pri)
include($$PWD/trace_data/trace_data.pri)
include($$PWD/cfg/cfg.pri)
include($$PWD/ext/sqlite_ext.pri)

unix{
LIBS += -L$$DESTDIR/ -lstdc++ \
        -L$${ROOTSRCDIR}/lib/$${DESTFOLDER} -lprotobuf -lsqlite -ldl
} else {
LIBS += -L$$DESTDIR/ -lstdc++ \
        -L$${ROOTSRCDIR}/lib/$${DESTFOLDER} -lprotobuf -lsqlite
}
INCLUDEPATH +=$$PWD/include

SOURCES += \
    main.cpp
