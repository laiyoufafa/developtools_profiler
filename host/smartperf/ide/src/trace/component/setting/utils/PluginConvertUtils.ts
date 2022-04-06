/*
 * Copyright (C) 2022 Huawei Device Co., Ltd.
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

export class PluginConvertUtils {
    private static crlf: string = "\n";
    private static leftBrace: string = "{"
    private static rightBrace: string = "}"

    public static createHdcCmd(requestString: string, time: number) {
        return "hiprofiler_cmd \\" + this.crlf
            + "  -c - \\" + this.crlf
            + "  -o /data/local/tmp/hiprofiler_data.htrace \\" + this.crlf
            + "  -t " + time + " \\" + this.crlf
            + "<<CONFIG"
            + requestString
            + "CONFIG"
    }

    public static BeanToCmdTxt(bean: any, needColon: boolean): string {
        return this.handleObj(bean, 0, needColon);
    }

    private static handleObj(bean: object, indentation: number, needColon: boolean): string {
        let prefixText: string = "";
        if (indentation == 0) {
            prefixText = prefixText + this.crlf;
        } else {
            prefixText = prefixText + " " + this.leftBrace + this.crlf;
        }
        for (const [key, value] of Object.entries(bean)) {
            const repeatedKey = Array.isArray(value);
            if (repeatedKey) {
                prefixText = prefixText + this.handleArray(key, value, indentation, needColon);
            } else {
                switch (typeof value) {
                    case "bigint":
                        prefixText = prefixText + ' '.repeat(indentation + 1) + this.humpToSnake(key) + ": " + value.toString() + this.crlf
                        break
                    case "boolean":
                        prefixText = prefixText + ' '.repeat(indentation + 1) + this.humpToSnake(key) + ": " + value.toString() + this.crlf
                        break
                    case "number":
                        if (value == 0) {
                            break;
                        }
                        prefixText = prefixText + ' '.repeat(indentation + 1) + this.humpToSnake(key) + ": " + value.toString() + this.crlf
                        break
                    case "string":
                        if (value == '') {
                            break
                        }
                        if (value.startsWith("LOG_")) {
                            prefixText = prefixText + ' '.repeat(indentation + 1) + this.humpToSnake(key) + ": " + value.toString() + this.crlf
                        } else {
                            prefixText = prefixText + ' '.repeat(indentation + 1) + this.humpToSnake(key) + ": \"" + value.toString() + "\"" + this.crlf
                        }
                        break
                    case "object":
                    default:
                        if (needColon) {
                            prefixText = prefixText + ' '.repeat(indentation + 1) + this.humpToSnake(key) + ": " + this.handleObj(value, indentation + 1, needColon) + "" + this.crlf
                        } else {
                            prefixText = prefixText + ' '.repeat(indentation + 1) + this.humpToSnake(key) + this.handleObj(value, indentation + 1, needColon) + "" + this.crlf
                        }
                }
            }
        }
        if (indentation == 0) {
            return prefixText
        } else {
            return prefixText + ' '.repeat(indentation) + this.rightBrace;
        }
    }

    private static handleArray(key: string, arr: Array<any>, indentation: number, needColon: boolean): string {
        let text = "";
        arr.forEach(arrValue => {
            switch (typeof arrValue) {
                case "bigint":
                    text = text + ' '.repeat(indentation + 1) + this.humpToSnake(key) + ": " + arrValue.toString() + this.crlf
                    break
                case "boolean":
                    text = text + ' '.repeat(indentation + 1) + this.humpToSnake(key) + ": " + arrValue.toString() + this.crlf
                    break
                case "number":
                    text = text + ' '.repeat(indentation + 1) + this.humpToSnake(key) + ": " + arrValue.toString() + this.crlf
                    break
                case "string":
                    if (arrValue == '') {
                        break
                    }
                    if (arrValue.startsWith("VMEMPLUGIN") || arrValue.startsWith("MEMPLUGIN")) {
                        text = text + ' '.repeat(indentation + 1) + this.humpToSnake(key) + ": " + arrValue.toString() + this.crlf
                    } else {
                        text = text + ' '.repeat(indentation + 1) + this.humpToSnake(key) + ": \"" + arrValue.toString() + "\"" + this.crlf
                    }
                    break
                case "object":
                default:
                    if (needColon) {
                        text = text + ' '.repeat(indentation + 1) + this.humpToSnake(key) + ": " + this.handleObj(arrValue, indentation + 1, needColon) + "" + this.crlf
                    } else {
                        text = text + ' '.repeat(indentation + 1) + this.humpToSnake(key) + this.handleObj(arrValue, indentation + 1, needColon) + "" + this.crlf
                    }
            }
        })
        return text;
    }

    private static humpToSnake(humpString: string): string {
        return humpString.replace(/[A-Z]/g, (value) => '_' + value.toLowerCase());
    }
}
