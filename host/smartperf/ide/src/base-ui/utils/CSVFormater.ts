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

export class JSonToCSV {
    static setDataConver(obj: any) {
        let that = this;
        let bw = this.browser();
        if (bw['ie'] < 9) return;
        let data = obj['data'],
            Show =
                typeof obj['showLabel'] === 'undefined'
                    ? true
                    : obj['showLabel'],
            fileName = (obj['fileName'] || 'UserExport') + '.csv',
            columns = obj['columns'] || {
                title: [],
                key: [],
                formatter: undefined,
            };
        let ShowLabel = typeof Show === 'undefined' ? true : Show;
        let row = '',
            CSV = '',
            key;
        // 如果要现实表头文字
        if (ShowLabel) {
            // 如果有传入自定义的表头文字
            if (columns.title.length) {
                columns.title.map(function (n: any) {
                    row += n + ',';
                });
            } else {
                // 如果没有，就直接取数据第一条的对象的属性
                for (key in data[0]) row += key + ',';
            }
            row = row.slice(0, -1);
            CSV += row + '\r\n';
        }
        // 具体的数据处理
        data.map(function (n: any) {
            row = '';
            // 如果存在自定义key值
            if (columns.key.length) {
                columns.key.map(function (m: any, idx: number) {
                    let strItem = n[m];
                    if (typeof n[m] == 'undefined') {
                        strItem = '';
                    } else if (typeof n[m] == 'object') {
                        strItem = JSON.stringify(n[m]);
                        strItem = strItem.replaceAll('"','');
                    }
                    if (idx === 0 && typeof n['depthCSV'] !== 'undefined') {
                        row +=
                            '"' +
                            that.treeDepth(n['depthCSV']) +
                            (typeof columns.formatter === 'function'
                                ? columns.formatter(m, n[m]) || n[m]
                                : strItem) +
                            '",';
                    } else {
                        row +=
                            '"' +
                            (typeof columns.formatter === 'function'
                                ? columns.formatter(m, n[m]) || n[m]
                                : strItem) +
                            '",';
                    }
                });
            } else {
                for (key in n) {
                    row +=
                        '"' +
                        (typeof columns.formatter === 'function'
                            ? columns.formatter(key, n[key]) || n[key]
                            : n[key]) +
                        '",';
                }
            }
            row.slice(0, row.length - 1); // 删除最后一个,
            CSV += row + '\r\n'; // 添加换行符号
        });
        if (!CSV) return;
        this.SaveAs(fileName, CSV);
    }

    static SaveAs(fileName: any, csvData: any) {
        let bw: any = this.browser();
        if (!bw['edge'] || !bw['ie']) {
            let alink: any = document.createElement('a');
            alink.id = 'linkDwnldLink';
            alink.href = this.getDownloadUrl(csvData);
            document.body.appendChild(alink);
            let linkDom: any = document.getElementById('linkDwnldLink');
            linkDom.setAttribute('download', fileName);
            linkDom.click();
            document.body.removeChild(linkDom);
        } else if (bw['ie'] >= 10 || bw['edge'] == 'edge') {
            let _utf = '\uFEFF';
            let _csvData = new Blob([_utf + csvData], {
                type: 'text/csv',
            });
            (navigator as any).msSaveBlob(_csvData, fileName);
        } else {
            let oWin: any = window.top?.open('about:blank', '_blank');
            oWin.document.write('sep=,\r\n' + csvData);
            oWin.document.close();
            oWin.document.execCommand('SaveAs', true, fileName);
            oWin.close();
        }
    }

    static getDownloadUrl(csvData: any) {
        let _utf = '\uFEFF';
        if (window.Blob && window.URL && (window.URL as any).createObjectURL) {
            var csvData: any = new Blob([_utf + csvData], {
                type: 'text/csv',
            });
            return URL.createObjectURL(csvData);
        }
    }

    static browser() {
        let Sys: any = {};
        let ua = navigator.userAgent.toLowerCase();
        let s;
        (s =
            ua.indexOf('edge') !== -1
                ? (Sys.edge = 'edge')
                : ua.match(/rv:([\d.]+)\) like gecko/))
            ? (Sys.ie = s[1])
            : (s = ua.match(/msie ([\d.]+)/))
            ? (Sys.ie = s[1])
            : (s = ua.match(/firefox\/([\d.]+)/))
            ? (Sys.firefox = s[1])
            : (s = ua.match(/chrome\/([\d.]+)/))
            ? (Sys.chrome = s[1])
            : (s = ua.match(/opera.([\d.]+)/))
            ? (Sys.opera = s[1])
            : (s = ua.match(/version\/([\d.]+).*safari/))
            ? (Sys.safari = s[1])
            : 0;
        return Sys;
    }

    static treeDepth(depth: number) {
        let str = '';
        for (let i = 0; i < depth; i++) {
            str += '    ';
        }
        return str;
    }

    static treeToArr(data: any) {
        const result: Array<any> = [];
        data.forEach((item: any) => {
            let depthCSV = 0;
            const loop = (data: any, depth: any) => {
                result.push({ depthCSV: depth, ...data });
                let child = data.children;
                if (child) {
                    for (let i = 0; i < child.length; i++) {
                        loop(child[i], depth + 1);
                    }
                }
            };
            loop(item, depthCSV);
        });
        return result;
    }

    static columnDatas(columns: Array<any>) {
        let titleList: Array<any> = [];
        let ketList: Array<any> = [];
        columns.forEach((column) => {
            let dataIndex = column.getAttribute('data-index');
            let columnName = column.getAttribute('title');
            if (columnName == '') {
                columnName = dataIndex;
            }
            if (columnName !== '  ') {
                titleList.push(columnName);
                ketList.push(dataIndex);
            }
        });
        return {
            titleList: titleList,
            ketList: ketList,
        };
    }

    static async csvExport(dataSource: {
        columns: any[];
        tables: any[];
        fileName: string;
    }) : Promise<string> {
        return new Promise((resolve) => {
            let data: any = this.columnDatas(dataSource.columns);
            let resultArr = JSonToCSV.treeToArr(dataSource.tables);
            JSonToCSV.setDataConver({
                data: resultArr,
                fileName: dataSource.fileName,
                columns: {
                    title: data.titleList,
                    key: data.ketList,
                },
            });
            resolve('ok');
        });
    }
}
