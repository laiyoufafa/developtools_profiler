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
    message("qmake" $${PROTOBUDIR}"/src/google/protobuf/compiler/")
SOURCES +=                                         \
    $${PROTOBUDIR}/src/google/protobuf/compiler/code_generator.cc                   \
    $${PROTOBUDIR}/src/google/protobuf/compiler/command_line_interface.cc           \
    $${PROTOBUDIR}/src/google/protobuf/compiler/plugin.cc                           \
    $${PROTOBUDIR}/src/google/protobuf/compiler/plugin.pb.cc                        \
    $${PROTOBUDIR}/src/google/protobuf/compiler/subprocess.cc                       \
    $${PROTOBUDIR}/src/google/protobuf/compiler/zip_writer.cc                       \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_enum.cc                     \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_enum_field.cc               \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_extension.cc                \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_field.cc                    \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_file.cc                     \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_generator.cc                \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_helpers.cc                  \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_map_field.cc                \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_message.cc                  \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_message_field.cc            \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_padding_optimizer.cc        \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_primitive_field.cc          \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_service.cc                  \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_string_field.cc             \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_context.cc                \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_enum.cc                   \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_enum_lite.cc              \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_enum_field.cc             \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_enum_field_lite.cc        \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_extension.cc              \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_extension_lite.cc         \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_field.cc                  \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_file.cc                   \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_generator.cc              \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_generator_factory.cc      \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_helpers.cc                \
#    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_lazy_message_field.cc                \
#    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_lazy_message_field_lite.cc                \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_map_field.cc              \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_map_field_lite.cc         \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_message_lite.cc           \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_message_builder.cc        \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_message_builder_lite.cc   \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_message_field.cc          \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_message_field_lite.cc     \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_name_resolver.cc          \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_primitive_field.cc        \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_primitive_field_lite.cc   \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_shared_code_generator.cc  \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_service.cc                \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_string_field.cc           \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_string_field_lite.cc      \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_doc_comment.cc            \
    $${PROTOBUDIR}/src/google/protobuf/compiler/js/js_generator.cc                  \
    $${PROTOBUDIR}/src/google/protobuf/compiler/js/well_known_types_embed.cc        \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_enum.cc       \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_enum_field.cc \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_extension.cc  \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_field.cc      \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_file.cc       \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_generator.cc  \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_helpers.cc    \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_map_field.cc  \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_message.cc    \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_message_field.cc \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_oneof.cc      \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_primitive_field.cc \
#    $${PROTOBUDIR}/src/google/protobuf/compiler/php/php_generator.cc                \
    $${PROTOBUDIR}/src/google/protobuf/compiler/python/python_generator.cc          \
    $${PROTOBUDIR}/src/google/protobuf/compiler/ruby/ruby_generator.cc              \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_doc_comment.cc        \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_enum.cc               \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_enum_field.cc         \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_field_base.cc         \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_generator.cc          \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_message.cc                \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_helpers.cc            \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_map_field.cc          \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_message.cc            \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_message_field.cc      \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_primitive_field.cc    \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_reflection_class.cc     \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_repeated_enum_field.cc \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_repeated_message_field.cc \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_repeated_primitive_field.cc \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_source_generator_base.cc \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_wrapper_field.cc      \

HEADERS += \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_padding_optimizer.h         \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_primitive_field.h           \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_options.h                   \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_service.h                   \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_string_field.h              \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_enum_field.h              \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_enum_field_lite.h         \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_context.h                 \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_enum.h                    \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_enum_lite.h               \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_extension.h               \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_field.h                   \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_map_field.h               \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_map_field_lite.h          \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_helpers.h                 \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_file.h                    \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_message_field.h           \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_generator_factory.h       \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_extension_lite.h          \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_message_field_lite.h      \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_message.h                 \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_message_lite.h            \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_message_builder.h         \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_message_builder_lite.h    \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_name_resolver.h           \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_options.h                 \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_primitive_field.h         \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_primitive_field_lite.h    \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_shared_code_generator.h   \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_service.h                 \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_string_field.h            \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_enum.h        \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_string_field_lite.h       \
    $${PROTOBUDIR}/src/google/protobuf/compiler/scc.h                               \
    $${PROTOBUDIR}/src/google/protobuf/compiler/subprocess.h                        \
    $${PROTOBUDIR}/src/google/protobuf/compiler/zip_writer.h                        \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_enum.h                      \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_enum_field.h                \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_extension.h                 \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_field.h                     \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_file.h                      \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_helpers.h                   \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_map_field.h                 \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_message.h                   \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_message_field.h             \
    $${PROTOBUDIR}/src/google/protobuf/compiler/cpp/cpp_message_layout_helper.h     \
    $${PROTOBUDIR}/src/google/protobuf/compiler/java/java_doc_comment.h             \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_enum_field.h  \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_extension.h   \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_field.h       \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_file.h        \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_helpers.h     \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_map_field.h   \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_message.h     \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_message_field.h \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_nsobject_methods.h  \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_oneof.h       \
    $${PROTOBUDIR}/src/google/protobuf/compiler/objectivec/objectivec_primitive_field.h \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_doc_comment.h         \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_wrapper_field.h \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_repeated_enum_field.h \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_repeated_message_field.h \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_repeated_primitive_field.h \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_source_generator_base.h \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_message_field.h       \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_options.h             \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_primitive_field.h     \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_reflection_class.h      \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_enum.h                \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_enum_field.h          \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_field_base.h          \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_helpers.h             \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_map_field.h           \
    $${PROTOBUDIR}/src/google/protobuf/compiler/csharp/csharp_message.h             \
