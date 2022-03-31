/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "parse_plugin_config.h"

#include "all_plugin_config.pb.h"
#include "cpu_plugin_config.pb.h"
#include "diskio_plugin_config.pb.h"
#include "google/protobuf/text_format.h"
#include "hidump_plugin_config.pb.h"
#include "hilog_plugin_config.pb.h"
#include "memory_plugin_common.pb.h"
#include "memory_plugin_config.pb.h"
#include "network_plugin_config.pb.h"
#include "native_hook_config.pb.h"
#include "process_plugin_config.pb.h"
#include "trace_plugin_config.pb.h"

using google::protobuf::TextFormat;

namespace {
constexpr int REMAINDER = 2;
}

ParsePluginConfig& ParsePluginConfig::GetInstance()
{
    static ParsePluginConfig parsePluginConfig;
    return parsePluginConfig;
}

std::string ParsePluginConfig::GetPluginsConfig(std::string& content)
{
    std::string pluginConfig = "";
    std::string pluginName = "";
    size_t beginPos = 0;
    size_t endPos = 0;
    for (int i = 0; content.size() > 0; i++) {
        // 先获取pluginName，再获取configData
        std::string destStr = (i % REMAINDER) ? "config_data" : "plugin_name";
        beginPos = content.find(destStr);
        if (beginPos == std::string::npos) {
            break;
        }
        pluginConfig += content.substr(0, beginPos);
        content = content.substr(beginPos + destStr.size(), content.size());
        destStr = (i % REMAINDER) ? "{" : "\"";
        beginPos = content.find(destStr);
        if (beginPos == std::string::npos) {
            break;
        }
        content = content.substr(beginPos + 1, content.size());
        destStr = (i % REMAINDER) ? "}" : "\"";
        endPos = content.find(destStr);
        if (endPos == std::string::npos) {
            break;
        }
        std::string contentStr = content.substr(0, endPos);
        if (i % REMAINDER == 0) {    // set plugin-name
            pluginName = contentStr;

            if (pluginName == "") {
                return "";
            }
            pluginConfig += "name: \"" + pluginName + "\"";
        } else {    // save config_data
            pluginConfigMap.insert(
                {pluginName, contentStr}
            );
            pluginConfig += "config_data: \"\"";
        }

        content = content.substr(endPos + 1, content.size());
    }

    pluginConfig += content;
    return pluginConfig;
}

bool ParsePluginConfig::SetSerializePluginsConfig(const std::string& pluginName, ProfilerPluginConfig& pluginConfig)
{
    bool ret = false;
    if (pluginConfigMap.count(pluginName) == 0) {
        printf("unknown plugin: %s\n", pluginName.c_str());
        return ret;
    }

    // 将pluginConfigMap中保存的configData序列化后写入pluginConfig
    if (pluginName == "cpu-plugin") {
        ret = SetSerializeCpuConfig(pluginName, pluginConfig);
    } else if (pluginName == "diskio-plugin") {
        ret = SetSerializeDiskioConfig(pluginName, pluginConfig);
    } else if (pluginName == "ftrace-plugin") {
        ret = SetSerializeFtraceConfig(pluginName, pluginConfig);
    } else if (pluginName == "hidump-plugin") {
        ret = SetSerializeHidumpConfig(pluginName, pluginConfig);
    } else if (pluginName == "hilog-plugin") {
        ret = SetSerializeHilogConfig(pluginName, pluginConfig);
    } else if (pluginName == "memory-plugin") {
        ret = SetSerializeMemoryConfig(pluginName, pluginConfig);
    } else if (pluginName == "nativehook") {
        ret = SetSerializeHookConfig(pluginName, pluginConfig);
    } else if (pluginName == "network-plugin") {
        ret = SetSerializeNetworkConfig(pluginName, pluginConfig);
    } else if (pluginName == "process-plugin") {
        ret = SetSerializeProcessConfig(pluginName, pluginConfig);
    } else {
        printf("unsupport plugin: %s\n", pluginName.c_str());
    }

    return ret;
}

bool ParsePluginConfig::SetSerializeCpuConfig(const std::string& pluginName, ProfilerPluginConfig& pluginConfig)
{
    std::string configData = pluginConfigMap[pluginName];
    auto cpuConfigNolite = std::make_unique<CpuPluginConfig>();
    if (!TextFormat::ParseFromString(configData, cpuConfigNolite.get())) {
        printf("cpu parse failed!\n");
        return false;
    }

    CpuConfig cpuConfigLite;
    cpuConfigLite.set_pid(cpuConfigNolite->pid());

    std::vector<uint8_t> configDataVec(cpuConfigLite.ByteSizeLong());
    if (cpuConfigLite.SerializeToArray(configDataVec.data(), configDataVec.size()) <= 0) {
        printf("cpu serialize failed!\n");
        return false;
    }
    pluginConfig.set_config_data((const void*)configDataVec.data(), configDataVec.size());
    return true;
}

bool ParsePluginConfig::SetSerializeDiskioConfig(const std::string& pluginName, ProfilerPluginConfig& pluginConfig)
{
    std::string configData = pluginConfigMap[pluginName];
    auto diskioConfigNolite = std::make_unique<DiskioPluginConfig>();
    if (!TextFormat::ParseFromString(configData, diskioConfigNolite.get())) {
        printf("diskio parse failed!\n");
        return false;
    }

    DiskioConfig diskioConfigLite;
    diskioConfigLite.set_unspeci_fied(diskioConfigNolite->unspeci_fied());

    std::vector<uint8_t> configDataVec(diskioConfigLite.ByteSizeLong());
    if (diskioConfigLite.SerializeToArray(configDataVec.data(), configDataVec.size()) <= 0) {
        printf("diskio serialize failed!\n");
        return false;
    }
    pluginConfig.set_config_data((const void*)configDataVec.data(), configDataVec.size());
    return true;
}

bool ParsePluginConfig::SetSerializeFtraceConfig(const std::string& pluginName, ProfilerPluginConfig& pluginConfig)
{
    std::string configData = pluginConfigMap[pluginName];
    auto ftraceConfigNolite = std::make_unique<FtracePluginConfig>();
    if (!TextFormat::ParseFromString(configData, ftraceConfigNolite.get())) {
        printf("ftrace parse failed!\n");
        return false;
    }

    int i = 0;
    TracePluginConfig ftraceConfigLite;
    for (i = 0; i < ftraceConfigNolite->ftrace_events().size(); i++) {
        ftraceConfigLite.add_ftrace_events(ftraceConfigNolite->ftrace_events(i));
    }
    for (i = 0; i < ftraceConfigNolite->hitrace_categories().size(); i++) {
        ftraceConfigLite.add_hitrace_categories(ftraceConfigNolite->hitrace_categories(i));
    }
    for (i = 0; i < ftraceConfigNolite->hitrace_apps().size(); i++) {
        ftraceConfigLite.add_hitrace_apps(ftraceConfigNolite->hitrace_apps(i));
    }
    ftraceConfigLite.set_buffer_size_kb(ftraceConfigNolite->buffer_size_kb());
    ftraceConfigLite.set_flush_interval_ms(ftraceConfigNolite->flush_interval_ms());
    ftraceConfigLite.set_flush_threshold_kb(ftraceConfigNolite->flush_threshold_kb());
    ftraceConfigLite.set_parse_ksyms(ftraceConfigNolite->parse_ksyms());
    ftraceConfigLite.set_clock(ftraceConfigNolite->clock());
    ftraceConfigLite.set_trace_period_ms(ftraceConfigNolite->trace_period_ms());
    ftraceConfigLite.set_raw_data_prefix(ftraceConfigNolite->raw_data_prefix());
    ftraceConfigLite.set_trace_duration_ms(ftraceConfigNolite->trace_duration_ms());
    ftraceConfigLite.set_debug_on(ftraceConfigNolite->debug_on());
    ftraceConfigLite.set_flush_cache_data(ftraceConfigNolite->flush_cache_data());
    ftraceConfigLite.set_hitrace_time(ftraceConfigNolite->hitrace_time());

    std::vector<uint8_t> configDataVec(ftraceConfigLite.ByteSizeLong());
    if (ftraceConfigLite.SerializeToArray(configDataVec.data(), configDataVec.size()) <= 0) {
        printf("ftrace serialize failed!\n");
        return false;
    }
    pluginConfig.set_config_data((const void*)configDataVec.data(), configDataVec.size());
    return true;
}

bool ParsePluginConfig::SetSerializeHidumpConfig(const std::string& pluginName, ProfilerPluginConfig& pluginConfig)
{
    std::string configData = pluginConfigMap[pluginName];
    auto hidumpConfigNolite = std::make_unique<HidumpPluginConfig>();
    if (!TextFormat::ParseFromString(configData, hidumpConfigNolite.get())) {
        printf("hidump parse failed!\n");
        return false;
    }

    HidumpConfig hidumpConfigLite;
    hidumpConfigLite.set_report_fps(hidumpConfigNolite->report_fps());

    std::vector<uint8_t> configDataVec(hidumpConfigLite.ByteSizeLong());
    if (hidumpConfigLite.SerializeToArray(configDataVec.data(), configDataVec.size()) <= 0) {
        printf("hidump serialize failed!\n");
        return false;
    }
    pluginConfig.set_config_data((const void*)configDataVec.data(), configDataVec.size());
    return true;
}

bool ParsePluginConfig::SetSerializeHilogConfig(const std::string& pluginName, ProfilerPluginConfig& pluginConfig)
{
    std::string configData = pluginConfigMap[pluginName];
    auto hilogConfigNolite = std::make_unique<HilogPluginConfig>();
    if (!TextFormat::ParseFromString(configData, hilogConfigNolite.get())) {
        printf("hilog parse failed!\n");
        return false;
    }

    HilogConfig hilogConfigLite;
    hilogConfigLite.set_device_type(static_cast<Type>(hilogConfigNolite->device_type()));
    hilogConfigLite.set_log_level(static_cast<Level>(hilogConfigNolite->log_level()));
    hilogConfigLite.set_pid(hilogConfigNolite->pid());
    hilogConfigLite.set_need_record(hilogConfigNolite->need_record());
    hilogConfigLite.set_need_clear(hilogConfigNolite->need_clear());

    std::vector<uint8_t> configDataVec(hilogConfigLite.ByteSizeLong());
    if (hilogConfigLite.SerializeToArray(configDataVec.data(), configDataVec.size()) <= 0) {
        printf("hilog serialize failed!\n");
        return false;
    }
    pluginConfig.set_config_data((const void*)configDataVec.data(), configDataVec.size());
    return true;
}

bool ParsePluginConfig::SetSerializeMemoryConfig(const std::string& pluginName, ProfilerPluginConfig& pluginConfig)
{
    std::string configData = pluginConfigMap[pluginName];
    auto memoryConfigNolite = std::make_unique<MemoryPluginConfig>();
    if (!TextFormat::ParseFromString(configData, memoryConfigNolite.get())) {
        printf("memory parse failed!\n");
        return false;
    }

    MemoryConfig memoryConfigLite;
    memoryConfigLite.set_report_process_tree(memoryConfigNolite->report_process_tree());
    memoryConfigLite.set_report_sysmem_mem_info(memoryConfigNolite->report_sysmem_mem_info());
    memoryConfigLite.set_report_sysmem_vmem_info(memoryConfigNolite->report_sysmem_vmem_info());
    memoryConfigLite.set_report_process_mem_info(memoryConfigNolite->report_process_mem_info());
    memoryConfigLite.set_report_app_mem_info(memoryConfigNolite->report_app_mem_info());
    memoryConfigLite.set_report_app_mem_by_memory_service(memoryConfigNolite->report_app_mem_by_memory_service());
    int i = 0;
    for (i = 0; i < memoryConfigNolite->sys_meminfo_counters().size(); i++) {
        memoryConfigLite.add_sys_meminfo_counters(
            static_cast<SysMeminfoType>(memoryConfigNolite->sys_meminfo_counters(i)));
    }
    for (i = 0; i < memoryConfigNolite->sys_vmeminfo_counters().size(); i++) {
        memoryConfigLite.add_sys_vmeminfo_counters(
            static_cast<SysVMeminfoType>(memoryConfigNolite->sys_vmeminfo_counters(i)));
    }

    std::vector<uint8_t> configDataVec(memoryConfigLite.ByteSizeLong());
    if (memoryConfigLite.SerializeToArray(configDataVec.data(), configDataVec.size()) <= 0) {
        printf("memory serialize failed!\n");
        return false;
    }
    pluginConfig.set_config_data((const void*)configDataVec.data(), configDataVec.size());
    return true;
}

bool ParsePluginConfig::SetSerializeHookConfig(const std::string& pluginName, ProfilerPluginConfig& pluginConfig)
{
    std::string configData = pluginConfigMap[pluginName];
    auto hookConfigNolite = std::make_unique<HookPluginConfig>();
    if (!TextFormat::ParseFromString(configData, hookConfigNolite.get())) {
        printf("nativedaemon parse failed!\n");
        return false;
    }

    NativeHookConfig hookConfigLite;
    hookConfigLite.set_pid(hookConfigNolite->pid());
    hookConfigLite.set_save_file(hookConfigNolite->save_file());
    hookConfigLite.set_file_name(hookConfigNolite->file_name());
    hookConfigLite.set_filter_size(hookConfigNolite->filter_size());
    hookConfigLite.set_smb_pages(hookConfigNolite->smb_pages());
    hookConfigLite.set_max_stack_depth(hookConfigNolite->max_stack_depth());
    hookConfigLite.set_process_name(hookConfigNolite->process_name());

    std::vector<uint8_t> configDataVec(hookConfigLite.ByteSizeLong());
    if (hookConfigLite.SerializeToArray(configDataVec.data(), configDataVec.size()) <= 0) {
        printf("nativedaemon serialize failed!\n");
        return false;
    }
    pluginConfig.set_config_data((const void*)configDataVec.data(), configDataVec.size());
    return true;
}

bool ParsePluginConfig::SetSerializeNetworkConfig(const std::string& pluginName, ProfilerPluginConfig& pluginConfig)
{
    std::string configData = pluginConfigMap[pluginName];
    auto networkConfigNolite = std::make_unique<NetworkPluginConfig>();
    if (!TextFormat::ParseFromString(configData, networkConfigNolite.get())) {
        printf("network parse failed!\n");
        return false;
    }

    NetworkConfig networkConfigLite;
    for (int i = 0; i < networkConfigNolite->pid().size(); i++) {
        networkConfigLite.add_pid(networkConfigNolite->pid(i));
    }

    std::vector<uint8_t> configDataVec(networkConfigLite.ByteSizeLong());
    if (networkConfigLite.SerializeToArray(configDataVec.data(), configDataVec.size()) <= 0) {
        printf("network serialize failed!\n");
        return false;
    }
    pluginConfig.set_config_data((const void*)configDataVec.data(), configDataVec.size());
    return true;
}

bool ParsePluginConfig::SetSerializeProcessConfig(const std::string& pluginName, ProfilerPluginConfig& pluginConfig)
{
    std::string configData = pluginConfigMap[pluginName];
    auto processConfigNolite = std::make_unique<ProcessPluginConfig>();
    if (!TextFormat::ParseFromString(configData, processConfigNolite.get())) {
        printf("process parse failed!\n");
        return false;
    }

    ProcessConfig processConfigLite;
    processConfigLite.set_report_process_tree(processConfigNolite->report_process_tree());

    std::vector<uint8_t> configDataVec(processConfigLite.ByteSizeLong());
    if (processConfigLite.SerializeToArray(configDataVec.data(), configDataVec.size()) <= 0) {
        printf("process serialize failed!\n");
        return false;
    }
    pluginConfig.set_config_data((const void*)configDataVec.data(), configDataVec.size());
    return true;
}

