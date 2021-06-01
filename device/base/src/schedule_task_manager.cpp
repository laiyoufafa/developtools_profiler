/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "schedule_task_manager.h"

#include <ctime>
#include <iostream>
#include <mutex>

#include "logging.h"
#include "securec.h"

namespace {
constexpr std::chrono::milliseconds POLL_INTERVAL = std::chrono::milliseconds(5000);
constexpr std::chrono::milliseconds MIN_REPEAT_INTERVAL = std::chrono::milliseconds(10);
constexpr std::chrono::milliseconds ZERO_INTERVAL = std::chrono::milliseconds(0);
} // namespace

ScheduleTaskManager& ScheduleTaskManager::GetInstance()
{
    static ScheduleTaskManager instance;
    return instance;
}

ScheduleTaskManager::ScheduleTaskManager()
{
    scheduleThread_ = std::thread(&ScheduleTaskManager::ScheduleThread, this);
}

ScheduleTaskManager::~ScheduleTaskManager()
{
    Shutdown();
    if (scheduleThread_.joinable()) {
        scheduleThread_.join();
    }
}

void ScheduleTaskManager::Shutdown()
{
    std::lock_guard<std::mutex> guard(taskMutex_);
    runScheduleThread_ = false;
    taskCv_.notify_one();
}

std::chrono::milliseconds ScheduleTaskManager::NormalizeInterval(std::chrono::milliseconds interval)
{
    if (interval <= ZERO_INTERVAL) {
        return ZERO_INTERVAL;
    }
    if (interval < MIN_REPEAT_INTERVAL) {
        return MIN_REPEAT_INTERVAL;
    }
    return interval / MIN_REPEAT_INTERVAL * MIN_REPEAT_INTERVAL;
}

bool ScheduleTaskManager::ScheduleTask(const std::string& name,
                                       const std::function<void(void)>& callback,
                                       const std::chrono::milliseconds& repeatInterval)
{
    return ScheduleTask(name, callback, repeatInterval, repeatInterval);
}

bool ScheduleTaskManager::ScheduleTask(const std::string& name,
                                       const std::function<void(void)>& callback,
                                       const std::chrono::milliseconds& repeatInterval,
                                       std::chrono::milliseconds initialDelay)
{
    auto task = std::make_shared<Task>();

    task->name = name;
    task->callback = callback;
    task->initialDelay = initialDelay;
    task->repeatInterval = NormalizeInterval(repeatInterval);

    std::lock_guard<std::mutex> guard(taskMutex_);
    if (taskMap_.count(name) > 0) {
        HILOG_WARN(LOG_CORE, "task name %s already exists!", name.c_str());
        return false;
    }

    taskMap_[name] = task;
    timeMap_.insert(std::make_pair(Clock::now() + initialDelay, task));
    taskCv_.notify_one();

    HILOG_DEBUG(LOG_CORE, "add schedule %s, total: %zu ", name.c_str(), taskMap_.size());
    return true;
}

bool ScheduleTaskManager::UnscheduleTask(const std::string& name)
{
    HILOG_DEBUG(LOG_CORE, "del schedule %s, total: %zu", name.c_str(), taskMap_.size());
    std::unique_lock<std::mutex> lck(taskMutex_);
    auto it = taskMap_.find(name);
    if (it != taskMap_.end()) {
        taskMap_.erase(it);
        return true;
    }
    return false;
}

bool ScheduleTaskManager::TakeFront(TimePoint& time, WeakTask& task)
{
    std::unique_lock<std::mutex> lck(taskMutex_);

    // thread wait until task insert or shutdown
    while (timeMap_.empty() && runScheduleThread_) {
        taskCv_.wait_for(lck, POLL_INTERVAL);
    }

    if (!runScheduleThread_) {
        return false;
    }

    time = timeMap_.begin()->first;
    task = timeMap_.begin()->second;
    timeMap_.erase(timeMap_.begin());
    return true;
}

void ScheduleTaskManager::DumpTask(const SharedTask& task)
{
    if (task) {
        long msecs = std::chrono::duration_cast<ms>(task->lastRunTime.time_since_epoch()).count();
        HILOG_DEBUG(LOG_CORE, "{name = %s, interval = %lld, delay = %lld, lastRun = %ld}",
            task->name.c_str(), task->repeatInterval.count(), task->initialDelay.count(), msecs);
    }
}

void ScheduleTaskManager::ScheduleThread()
{
    while (true) {
        // take front task from task queue
        TimePoint targetTime;
        WeakTask targetTask;
        if (!TakeFront(targetTime, targetTask)) {
            break;
        }

        // delay to target time
        auto currentTime = Clock::now();
        if (targetTime >= currentTime) {
            std::this_thread::sleep_for(targetTime - currentTime);
        }

        // promote to shared_ptr
        auto task = targetTask.lock();
        DumpTask(task);

        if (task != nullptr) {
            // call task callback
            task->callback();
            task->lastRunTime = currentTime;

            // re-insert task to map if it's a repeat task
            if (task->repeatInterval.count() != 0) {
                std::unique_lock<std::mutex> guard(taskMutex_);
                timeMap_.insert(std::make_pair(targetTime + task->repeatInterval, task));
            }
        }
    }
}
