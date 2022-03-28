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
#include "event_formatter.h"
#include "logging.h"

FTRACE_NS_BEGIN
EventFormatter& EventFormatter::GetInstance()
{
    static EventFormatter instance;
    return instance;
}

std::string EventFormatter::FormatEvent(const FtraceEvent& ftraceEvent)
{
    for (auto entry : eventRegistries_) {
        std::string name = entry.first;
        EventRegistry registry = entry.second;
        CHECK_TRUE(registry.checkEvent_ && registry.formatEvent_, "", "invalid registry of %s", name.c_str());
        if (registry.checkEvent_(ftraceEvent)) {
            return registry.formatEvent_(ftraceEvent);
        }
    }
    return "";
}

void EventFormatter::RegisterEvent(const std::string& name, const CheckEventFn& check, const FormatEventFn& format)
{
    EventRegistry registry = {check, format};
    eventRegistries_[name] = registry;
}

void EventFormatter::UnregisterEvent(const std::string& name)
{
    eventRegistries_.erase(name);
}

EventFormatterRegisterar::EventFormatterRegisterar(const std::string& name,
                                                   const CheckEventFn& check,
                                                   const FormatEventFn& format)
    : name_(name)
{
    EventFormatter::GetInstance().RegisterEvent(name, check, format);
}

EventFormatterRegisterar::~EventFormatterRegisterar()
{
    EventFormatter::GetInstance().UnregisterEvent(name_);
}
FTRACE_NS_END