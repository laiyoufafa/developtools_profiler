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

package ohos.devtools.views.trace.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Data tools
 *
 * @date 2021/04/22 12:25
 */
public final class DataUtils {
    private DataUtils() {
    }

    /**
     * Get the json object
     *
     * @return JsonObject
     */
    public static JsonObject getJson() {
        JsonObject jsonObject = null;
        try (InputStream STREAM = DataUtils.class.getClassLoader().getResourceAsStream("data.json")) {
            jsonObject = new Gson().fromJson(new InputStreamReader(STREAM, "UTF-8"), JsonObject.class);
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            unsupportedEncodingException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return jsonObject;
    }
}
