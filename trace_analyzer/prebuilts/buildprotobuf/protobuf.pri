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
PROTOBUDIR = $$PWD/../../third_party/protobuf
    message("qmake" $${PROTOBUDIR}"/src/google/protobuf/")
SOURCES +=   \
$${PROTOBUDIR}/src/google/protobuf/any.cc \
$${PROTOBUDIR}/src/google/protobuf/any_lite.cc \
    $${PROTOBUDIR}/src/google/protobuf/any.pb.cc \
    $${PROTOBUDIR}/src/google/protobuf/api.pb.cc \
    $${PROTOBUDIR}/src/google/protobuf/compiler/importer.cc \
    $${PROTOBUDIR}/src/google/protobuf/compiler/parser.cc \
    $${PROTOBUDIR}/src/google/protobuf/descriptor.cc \
    $${PROTOBUDIR}/src/google/protobuf/descriptor.pb.cc \
    $${PROTOBUDIR}/src/google/protobuf/descriptor_database.cc \
    $${PROTOBUDIR}/src/google/protobuf/duration.pb.cc \
    $${PROTOBUDIR}/src/google/protobuf/dynamic_message.cc \
    $${PROTOBUDIR}/src/google/protobuf/empty.pb.cc \
    $${PROTOBUDIR}/src/google/protobuf/extension_set_heavy.cc \
    $${PROTOBUDIR}/src/google/protobuf/field_mask.pb.cc \
    $${PROTOBUDIR}/src/google/protobuf/generated_message_reflection.cc \
    $${PROTOBUDIR}/src/google/protobuf/generated_message_table_driven.cc \
    $${PROTOBUDIR}/src/google/protobuf/io/gzip_stream.cc \
    $${PROTOBUDIR}/src/google/protobuf/io/printer.cc \
    $${PROTOBUDIR}/src/google/protobuf/io/tokenizer.cc \
    $${PROTOBUDIR}/src/google/protobuf/map_field.cc \
    $${PROTOBUDIR}/src/google/protobuf/message.cc \
    $${PROTOBUDIR}/src/google/protobuf/reflection_ops.cc \
    $${PROTOBUDIR}/src/google/protobuf/service.cc \
    $${PROTOBUDIR}/src/google/protobuf/source_context.pb.cc \
    $${PROTOBUDIR}/src/google/protobuf/struct.pb.cc \
#    $${PROTOBUDIR}/src/google/protobuf/stubs/mathlimits.cc \
    $${PROTOBUDIR}/src/google/protobuf/stubs/substitute.cc \
    $${PROTOBUDIR}/src/google/protobuf/text_format.cc \
    $${PROTOBUDIR}/src/google/protobuf/timestamp.pb.cc \
    $${PROTOBUDIR}/src/google/protobuf/type.pb.cc \
    $${PROTOBUDIR}/src/google/protobuf/unknown_field_set.cc \
#    $${PROTOBUDIR}/src/google/protobuf/util/delimited_message_util.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/field_comparator.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/field_mask_util.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/internal/datapiece.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/internal/default_value_objectwriter.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/internal/error_listener.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/internal/field_mask_utility.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/internal/json_escaping.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/internal/json_objectwriter.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/internal/json_stream_parser.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/internal/object_writer.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/internal/proto_writer.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/internal/protostream_objectsource.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/internal/protostream_objectwriter.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/internal/type_info.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/internal/type_info_test_helper.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/internal/utility.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/json_util.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/message_differencer.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/time_util.cc \
    $${PROTOBUDIR}/src/google/protobuf/util/type_resolver_util.cc \
    $${PROTOBUDIR}/src/google/protobuf/wire_format.cc \
    $${PROTOBUDIR}/src/google/protobuf/wrappers.pb.cc
