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
#ifndef EVENT_FORMATTER_H
#define EVENT_FORMATTER_H

#include <functional>
#include <map>
#include "ftrace_namespace.h"
#include "trace_plugin_result.pb.h"

FTRACE_NS_BEGIN
using FormatEventFn = std::function<std::string(const FtraceEvent&)>;
using CheckEventFn = std::function<bool(const FtraceEvent&)>;

class EventFormatterRegisterar;

class EventFormatter {
public:
    static EventFormatter& GetInstance();

    // std::string FormatResult(const TracePluginResult& result);

    std::string FormatEvent(const FtraceEvent& ftraceEvent);

protected:
    friend class EventFormatterRegisterar;
    void RegisterEvent(const std::string& name, const CheckEventFn& check, const FormatEventFn& format);
    void UnregisterEvent(const std::string& name);

private:
    struct EventRegistry {
        CheckEventFn checkEvent_;
        FormatEventFn formatEvent_;
    };
    std::map<std::string, EventRegistry> eventRegistries_;
};

class EventFormatterRegisterar {
public:
    EventFormatterRegisterar(const std::string& name, const CheckEventFn& check, const FormatEventFn& format);
    ~EventFormatterRegisterar();

private:
    std::string name_;
};
FTRACE_NS_END

#define REGISTER_FTRACE_EVENT_FORMATTER(event, check, format) \
    static EventFormatterRegisterar ftrace##event##FormatterRegister##__LINE__(#event, check, format);

#endif // EVENT_FORMATTER_H