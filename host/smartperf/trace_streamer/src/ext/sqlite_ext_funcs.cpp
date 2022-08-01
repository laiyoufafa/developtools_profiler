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
#include "sqlite_ext_funcs.h"
#include <cmath>
#include <memory>
#include "log.h"
#include "sqlite3.h"
namespace SysTuning {
namespace base {
/*
** Return a stdev value
*/
static void sqliteExtStdevFinalize(sqlite3_context* context)
{
    StdevCtx* ptr = static_cast<StdevCtx*>(sqlite3_aggregate_context(context, 0));
    if (ptr && ptr->cntValue > 1) {
        sqlite3_result_double(context, sqrt(ptr->rSValue / (ptr->cntValue - 1)));
    } else {
        sqlite3_result_double(context, 0.0);
    }
}
/*
** called each value received during a calculation of stdev or variance
*/
static void sqliteExtStdevNextStep(sqlite3_context* context, int argc, sqlite3_value** argv)
{
    double deltaValue;
    double x;

    TS_ASSERT(argc == 1);
    StdevCtx* ptr = static_cast<StdevCtx*>(sqlite3_aggregate_context(context, sizeof(StdevCtx)));
    if (SQLITE_NULL != sqlite3_value_numeric_type(argv[0])) {
        ptr->cntValue++;
        x = sqlite3_value_double(argv[0]);
        deltaValue = (x - ptr->rMValue);
        ptr->rMValue += deltaValue / ptr->cntValue;
        ptr->rSValue += deltaValue * (x - ptr->rMValue);
    }
}

enum Type {
    tsNull = 0,
    tsLong,
    tsDouble,
    tsString,
    tsBytes,
};

struct TSSqlValue {
    TSSqlValue() = default;

    static TSSqlValue Long(int64_t v)
    {
        TSSqlValue value;
        value.longValue = v;
        value.type = Type::tsLong;
        return value;
    }

    static TSSqlValue Double(double v)
    {
        TSSqlValue value;
        value.doubleValue = v;
        value.type = Type::tsDouble;
        return value;
    }

    static TSSqlValue String(const char* v)
    {
        TSSqlValue value;
        value.stringValue = v;
        value.type = Type::tsString;
        return value;
    }

    static TSSqlValue Bytes(const void* v, size_t size)
    {
        TSSqlValue value;
        value.bytesValue = v;
        value.bytesCount = size;
        value.type = Type::tsBytes;
        return value;
    }

    double GetDouble() const
    {
        return doubleValue;
    }
    int64_t GetLong() const
    {
        return longValue;
    }
    const char* GetString() const
    {
        return stringValue;
    }
    const void* GetBytes() const
    {
        return bytesValue;
    }

    bool isNull() const
    {
        return type == Type::tsNull;
    }

    union {
        const char* stringValue;
        int64_t longValue;
        double doubleValue;
        const void* bytesValue;
    };
    size_t bytesCount = 0;
    Type type = tsNull;
};

TSSqlValue SqliteValueToTSSqlValue(sqlite3_value* value)
{
    TSSqlValue sqlValue;
    switch (sqlite3_value_type(value)) {
        case SQLITE_INTEGER:
            sqlValue.type = Type::tsLong;
            sqlValue.longValue = sqlite3_value_int64(value);
            break;
        case SQLITE_FLOAT:
            sqlValue.type = Type::tsDouble;
            sqlValue.doubleValue = sqlite3_value_double(value);
            break;
        case SQLITE_TEXT:
            sqlValue.type = Type::tsString;
            sqlValue.stringValue = reinterpret_cast<const char*>(sqlite3_value_text(value));
            break;
        case SQLITE_BLOB:
            sqlValue.type = Type::tsBytes;
            sqlValue.bytesValue = sqlite3_value_blob(value);
            sqlValue.bytesCount = static_cast<size_t>(sqlite3_value_bytes(value));
            break;
    }
    return sqlValue;
}
class JsonBuild {
public:
    JsonBuild() = default;
    void AppendHead()
    {
        body_ += "{";
    }
    void AppendTail()
    {
        body_ += "}";
    }
    void AppendCommon()
    {
        body_ += ",";
    }
    bool AppendSqlValue(const std::string& field_name, const TSSqlValue& value)
    {
        body_ += "\"" + field_name + "\":";
        return AppendSqlValue(value);
    }
    bool AppendSqlValue(const TSSqlValue& value)
    {
        switch (value.type) {
            case tsLong:
                body_ += std::to_string(value.longValue) + ",";
                break;
            case tsDouble:
                body_ += std::to_string(value.doubleValue) + ",";
                break;
            case tsString:
                body_ += "\"" + std::string(value.stringValue) + "\"" + ",";
                break;
            case tsBytes:
                body_ += "\"" + std::string(static_cast<const char*>(value.bytesValue), value.bytesCount) + "\"" + ",";
                break;
            case tsNull:
                body_ += std::to_string(0) + ",";
                break;
        }
        return true;
    }
    std::string body_;
    bool poped_ = false;
    void PopLast()
    {
        body_.pop_back();
    }
    const std::string& Body()
    {
        return body_;
    }
};

void BuildJson(sqlite3_context* ctx, int argc, sqlite3_value** argv)
{
    const auto* fn_ctx = static_cast<const JsonBuild*>(sqlite3_user_data(ctx));
    if (argc % 2 != 0) {
        TS_LOGI("BuildJson arg number error");
        sqlite3_result_error(ctx, "BuildJson arg number error", -1);
        return;
    }

    JsonBuild builder;
    builder.AppendHead();
    for (int i = 0; i < argc; i += 2) {
        if (sqlite3_value_type(argv[i]) != SQLITE_TEXT) {
            TS_LOGI("BuildJson: Invalid args argc:%d, %d", argc, sqlite3_value_type(argv[i]));
            sqlite3_result_error(ctx, "BuildJson: Invalid args", -1);
            return;
        }

        auto* key = reinterpret_cast<const char*>(sqlite3_value_text(argv[i]));
        auto value = SqliteValueToTSSqlValue(argv[i + 1]);
        auto status = builder.AppendSqlValue(key, value);
        if (!status) {
            TS_LOGI("AppendSqlValueError");
            sqlite3_result_error(ctx, "AppendSqlValueError", -1);
            return;
        }
    }
    builder.PopLast();
    builder.AppendTail();
    std::string raw = builder.Body();
    if (raw.empty()) {
        sqlite3_result_blob(ctx, "", 0, nullptr);
        return;
    }
    std::unique_ptr<uint8_t[]> data(static_cast<uint8_t*>(malloc(raw.size())));
    memcpy(data.get(), raw.data(), raw.size());
    sqlite3_result_blob(ctx, data.release(), static_cast<int>(raw.size()), free);
}

void RepeatedJsonStep(sqlite3_context* ctx, int argc, sqlite3_value** argv)
{
    auto** jsonBuild = static_cast<JsonBuild**>(sqlite3_aggregate_context(ctx, sizeof(JsonBuild*)));

    if (*jsonBuild == nullptr) {
        *jsonBuild = new JsonBuild();
    }
    JsonBuild* builder = *jsonBuild;
    builder->AppendHead();
    for (int i = 0; i < argc; i += 2) {
        if (sqlite3_value_type(argv[i]) != SQLITE_TEXT) {
            TS_LOGI("BuildJson: Invalid args argc:%d, %d", argc, sqlite3_value_type(argv[i]));
            sqlite3_result_error(ctx, "BuildJson: Invalid args", -1);
            return;
        }

        auto* key = reinterpret_cast<const char*>(sqlite3_value_text(argv[i]));
        auto value = SqliteValueToTSSqlValue(argv[i + 1]);
        auto status = builder->AppendSqlValue(key, value);
        if (!status) {
            TS_LOGI("AppendSqlValueError");
            sqlite3_result_error(ctx, "AppendSqlValueError", -1);
            return;
        }
    }
    builder->PopLast();
    builder->AppendTail();
    builder->AppendCommon();
}
void RepeatedFieldStep(sqlite3_context* ctx, int argc, sqlite3_value** argv)
{
    if (argc != 1) {
        TS_LOGE(
            "RepeatedField only support one arg, you can use BuildJson or BuildRepeatedJson function for multi args");
        return;
    }
    auto** jsonBuild = static_cast<JsonBuild**>(sqlite3_aggregate_context(ctx, sizeof(JsonBuild*)));

    if (*jsonBuild == nullptr) {
        *jsonBuild = new JsonBuild();
    }
    JsonBuild* builder = *jsonBuild;
    for (int i = 0; i < argc; i++) {
        auto value = SqliteValueToTSSqlValue(argv[i]);
        auto status = builder->AppendSqlValue(value);
        if (!status) {
            sqlite3_result_error(ctx, "error", -1);
        }
    }
}

void RepeatedFieldFinal(sqlite3_context* ctx)
{
    auto** jsonBuilder = static_cast<JsonBuild**>(sqlite3_aggregate_context(ctx, 0));

    if (jsonBuilder == nullptr) {
        sqlite3_result_null(ctx);
        return;
    }

    std::unique_ptr<JsonBuild> builder(*jsonBuilder);
    std::string raw = builder->Body();
    raw.pop_back();
    if (raw.empty()) {
        sqlite3_result_null(ctx);
        return;
    }

    std::unique_ptr<uint8_t[]> data(static_cast<uint8_t*>(malloc(raw.size())));
    memcpy(data.get(), raw.data(), raw.size());
    sqlite3_result_blob(ctx, data.release(), static_cast<int>(raw.size()), free);
}

void RepeatedJsonFinal(sqlite3_context* ctx)
{
    auto** jsonBuilder = static_cast<JsonBuild**>(sqlite3_aggregate_context(ctx, 0));

    if (jsonBuilder == nullptr) {
        sqlite3_result_null(ctx);
        return;
    }

    std::unique_ptr<JsonBuild> builder(*jsonBuilder);
    builder->PopLast();
    std::string raw = builder->Body();
    if (raw.empty()) {
        sqlite3_result_null(ctx);
        return;
    }

    std::unique_ptr<uint8_t[]> data(static_cast<uint8_t*>(malloc(raw.size())));
    memcpy(data.get(), raw.data(), raw.size());
    sqlite3_result_blob(ctx, data.release(), static_cast<int>(raw.size()), free);
}
void ts_create_extend_function(sqlite3* db)
{
    sqlite3_create_function(db, "stdev", -1, SQLITE_UTF8, nullptr, 0, sqliteExtStdevNextStep, sqliteExtStdevFinalize);
    auto ret = sqlite3_create_function_v2(db, "RepeatedField", 1, SQLITE_UTF8, nullptr, nullptr, RepeatedFieldStep,
                                          RepeatedFieldFinal, nullptr);
    if (ret) {
        TS_LOGF("Error while initializing RepeatedField");
    }
    ret = sqlite3_create_function_v2(db, "BuildRepeatedJson", -1, SQLITE_UTF8, nullptr, nullptr, RepeatedJsonStep,
                                     RepeatedJsonFinal, nullptr);
    if (ret) {
        TS_LOGF("Error while initializing BuildRepeatedJson");
    }
    std::unique_ptr<JsonBuild> ctx(new JsonBuild());
    ret = sqlite3_create_function_v2(db, "BuildJson", -1, SQLITE_UTF8, ctx.release(), BuildJson, nullptr, nullptr,
                                     [](void* ptr) { delete static_cast<JsonBuild*>(ptr); });
    if (ret != SQLITE_OK) {
        TS_LOGF("Error while initializing BuildJson");
    }
}
} // namespace base
} // namespace SysTuning