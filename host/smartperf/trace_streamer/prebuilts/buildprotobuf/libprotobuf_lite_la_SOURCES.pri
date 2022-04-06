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
    message("qmake" $${PROTOBUDIR}"/src")
win32 {
SOURCES +=  $${PROTOBUDIR}/src/google/protobuf/io/io_win32.cc
}
SOURCES +=                                  \
    $${PROTOBUDIR}/src/google/protobuf/stubs/bytestream.cc                          \
    $${PROTOBUDIR}/src/google/protobuf/stubs/common.cc                              \
    $${PROTOBUDIR}/src/google/protobuf/stubs/int128.cc                              \
#    $${PROTOBUDIR}/src/google/protobuf/stubs/once.cc                              \
    $${PROTOBUDIR}/src/google/protobuf/stubs/int128.h                               \
#    $${PROTOBUDIR}/src/google/protobuf/io/io_win32.cc                               \
    $${PROTOBUDIR}/src/google/protobuf/stubs/status.cc                              \
    $${PROTOBUDIR}/src/google/protobuf/stubs/statusor.cc                            \
    $${PROTOBUDIR}/src/google/protobuf/stubs/statusor.h                             \
    $${PROTOBUDIR}/src/google/protobuf/stubs/stringpiece.cc                         \
    $${PROTOBUDIR}/src/google/protobuf/stubs/stringprintf.cc                        \
    $${PROTOBUDIR}/src/google/protobuf/stubs/structurally_valid.cc                  \
    $${PROTOBUDIR}/src/google/protobuf/stubs/strutil.cc                             \
    $${PROTOBUDIR}/src/google/protobuf/stubs/time.cc                                \
#    $${PROTOBUDIR}/src/google/protobuf/any_lite.cc                                  \
    $${PROTOBUDIR}/src/google/protobuf/arena.cc                                     \
#    $${PROTOBUDIR}/src/google/protobuf/arenastring.cc                                     \
    $${PROTOBUDIR}/src/google/protobuf/extension_set.cc                             \
    $${PROTOBUDIR}/src/google/protobuf/generated_enum_util.cc                       \
    $${PROTOBUDIR}/src/google/protobuf/generated_message_util.cc                    \
#    $${PROTOBUDIR}/src/google/protobuf/generated_message_table_driven_lite.cc       \
    $${PROTOBUDIR}/src/google/protobuf/implicit_weak_message.cc                     \
    $${PROTOBUDIR}/src/google/protobuf/message_lite.cc                              \
    $${PROTOBUDIR}/src/google/protobuf/parse_context.cc                             \
    $${PROTOBUDIR}/src/google/protobuf/repeated_field.cc                            \
#    $${PROTOBUDIR}/src/google/protobuf/stubs/atomicops_internals_x86_gcc.cc                            \
#    $${PROTOBUDIR}/src/google/protobuf/stubs/atomicops_internals_x86_msvc.cc                            \
    $${PROTOBUDIR}/src/google/protobuf/wire_format_lite.cc                          \
    $${PROTOBUDIR}/src/google/protobuf/io/coded_stream.cc                           \
    $${PROTOBUDIR}/src/google/protobuf/io/strtod.cc                                 \
    $${PROTOBUDIR}/src/google/protobuf/io/zero_copy_stream.cc                       \
    $${PROTOBUDIR}/src/google/protobuf/io/zero_copy_stream_impl.cc                  \
    $${PROTOBUDIR}/src/google/protobuf/io/zero_copy_stream_impl_lite.cc

HEADERS += \
    $${PROTOBUDIR}/src/google/protobuf/stubs/bytestream.h                           \
    $${PROTOBUDIR}/src/google/protobuf/stubs/hash.h                                 \
    $${PROTOBUDIR}/src/google/protobuf/io/coded_stream_inl.h                        \
#    $${PROTOBUDIR}/src/google/protobuf/generated_message_table_driven_lite.h        \
    $${PROTOBUDIR}/src/google/protobuf/stubs/time.h                                 \
    $${PROTOBUDIR}/src/google/protobuf/stubs/stringprintf.h                         \
    $${PROTOBUDIR}/src/google/protobuf/stubs/stringpiece.h                          \
    $${PROTOBUDIR}/src/google/protobuf/stubs/status.h                               \
    $${PROTOBUDIR}/src/google/protobuf/stubs/status_macros.h                        \
#    $${PROTOBUDIR}/src/google/protobuf/io/io_win32.h                                \
    $${PROTOBUDIR}/src/google/protobuf/stubs/map_util.h                             \
    $${PROTOBUDIR}/src/google/protobuf/stubs/mathutil.h
