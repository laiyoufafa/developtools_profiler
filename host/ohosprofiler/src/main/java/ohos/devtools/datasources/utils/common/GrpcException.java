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

package ohos.devtools.datasources.utils.common;

/**
 * grpc Custom exception class
 */
public class GrpcException extends Exception {
    /**
     * GrpcException
     */
    public GrpcException() {
        super();
    }

    /**
     * GrpcException
     *
     * @param message Grpc message
     */
    public GrpcException(String message) {
        super(message);
    }

    /**
     * GrpcException
     *
     * @param message Grpc message
     * @param cause   Throwable cause
     */
    public GrpcException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * GrpcException
     *
     * @param cause Throwable cause
     */
    public GrpcException(Throwable cause) {
        super(cause);
    }

}
