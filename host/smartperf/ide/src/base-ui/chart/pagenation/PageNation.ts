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

export class PageNation {
    element: any;
    pageInfo: any;
    first: any;
    prev: any;
    next: any;
    last: any;
    inputBox: any;
    btn: any;
    list: any;
    origin: HTMLElement | undefined;
    static BtnBackColor = '#6c9bfa';
    static BtnColor = '#fff';
    constructor(selector: any, options = {}) {
        //最大容器
        this.element = selector;
        // 默认值
        this.pageInfo = {
            current: 1,
            total: 100,
            pageSize: 15,
        };
        //等待创建的元素
        this.first = null;
        this.prev = null;
        this.next = null;
        this.last = null;
        // 输入框
        this.inputBox = null;
        // 跳转按钮
        this.btn = null;
        // 中间的按钮组
        this.list = null;
        this.setPageOptions(options);
        this.setItemStyles();
        this.createPageElement();
        this.bindPageHtml();
        this.bindPageEvent();
    }

    setPageOptions(options: any) {
        // 当前页
        this.pageInfo.current = options.current || 1;
        // 一页显示多少条
        this.pageInfo.pageSize = options.pageSize || 15;
        if (options.totalpage) {
            //用户传递了多少页
            this.pageInfo.totalpage = options.totalpage;
        } else {
            //没有传递多少页
            if (options.total) {
                // 如果传递了总条数
                this.pageInfo.totalpage = Math.ceil(
                    options.total / this.pageInfo.pageSize
                );
            } else {
                // 如果没有传递总条数
                this.pageInfo.totalpage = 9;
            }
        }
        this.pageInfo.first = options.first || 'Home';
        this.pageInfo.change = options.change || function () {};
    }

    setElementStyles(ele: any, styles: any) {
        for (let key in styles) {
            ele.style[key] = styles[key];
        }
    }

    setItemStyles() {
        this.setElementStyles(this.element, {
            margin: '50px auto',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
        });
    }

    // 创建元素 首页 上一页 按钮组  下一页 尾页 输入框 按钮
    createPageElement() {
        //首页
        this.origin = document.createElement('p');
        this.setElementStyles(this.origin, {
            'border-radius': '5px',
            padding: '5px',
            border: '1px solid #333',
            cursor: 'pointer',
            margin: '0 10px',
        });

        this.first = this.origin.cloneNode(true);
        this.first.innerText = this.pageInfo.first;
        this.first.name = 'first';
        this.element.appendChild(this.first);

        this.prev = this.origin.cloneNode(true);
        this.prev.innerText = 'Previous';
        this.prev.name = 'prev';
        this.element.appendChild(this.prev);

        // 创建ul
        this.list = document.createElement('ul');
        this.setElementStyles(this.list, {
            display: 'flex',
            padding: '0',
        });
        this.element.appendChild(this.list);
        this.next = this.origin.cloneNode(true);
        this.next.innerText = 'Next';
        this.next.name = 'next';
        this.element.appendChild(this.next);
        this.last = this.origin.cloneNode(true);
        this.last.innerText = 'Last';
        this.last.name = 'last';
        this.element.appendChild(this.last);
        // 创建输入框
        this.inputBox = document.createElement('input');
        this.inputBox.value = this.pageInfo.current;
        this.setElementStyles(this.inputBox, {
            width: '50px',
            height: '30px',
            textAlign: 'center',
            outline: 'none',
            margin: '0 10px',
            'border-radius': '5px',
        });
        this.element.appendChild(this.inputBox);
        // 创建按钮
        this.btn = document.createElement('button');
        this.btn.innerText = 'Jump';
        this.btn.name = 'goto';
        this.setElementStyles(this.btn, {
            height: '34px',
            width: '50px',
            cursor: 'pointer',
            'border-radius': '5px',
        });
        this.element.appendChild(this.btn);
    }

    // 判断首页 上一页 下一页 尾页 是否可以点击
    bindPageHtml() {
        const { current, totalpage } = this.pageInfo;
        const disable = { backgroundColor: '#ccc', cursor: 'not-allowed' };
        const enable = {
            backgroundColor: PageNation.BtnColor,
            cursor: 'pointer',
        };
        // 判断当前页是否是第一页  如果是第一页  那么首页和上一页就不能点击
        if (current <= 1) {
            this.setElementStyles(this.first, disable);
            this.setElementStyles(this.prev, disable);
        } else {
            this.setElementStyles(this.first, enable);
            this.setElementStyles(this.prev, enable);
        }
        // 判断当前页是否是最后一页  如果是最后一页  那么下一页和尾页就不能点击
        if (current >= totalpage) {
            this.setElementStyles(this.next, disable);
            this.setElementStyles(this.last, disable);
        } else {
            this.setElementStyles(this.next, enable);
            this.setElementStyles(this.last, enable);
        }
        this.inputBox.value = current;
        //渲染的时候判断ul列表的显示情况
        this.bindPageList();
        this.pageInfo.change(this.pageInfo.current);
    }

    bindPageList() {
        // clear ul里面的内容
        this.list.innerHTML = '';
        //每次加载之前先清空ul里面的内容
        const { pageSize, current, totalpage } = this.pageInfo;
        const origin = document.createElement('li');
        origin.dataset.name = 'item';
        this.setElementStyles(origin, {
            listStyle: 'none',
            'border-radius': '5px',
            border: '1px solid #333',
            padding: '5px 10px',
            margin: '0 5px',
            cursor: 'pointer',
        });
        if (totalpage <= 9) {
            for (let i = 0; i < totalpage; i++) {
                const li = origin.cloneNode(true);
                // @ts-ignore
                li.innerText = i + 1;
                if (i + 1 === current) {
                    this.setElementStyles(li, {
                        backgroundColor: PageNation.BtnBackColor,
                        color: PageNation.BtnColor,
                    });
                }
                this.list.appendChild(li);
            }
            return;
        }
        // 左边5个 中间 ... 右边2个
        if (this.bindLeftList(current, totalpage, origin)) {
            return;
        }
        // 当前页面 大于5页 小于倒数第5页
        for (let i = 0; i < 2; i++) {
            const li = origin.cloneNode(true);
            // @ts-ignore
            li.innerText = i + 1;
            if (i + 1 === current) {
                this.setElementStyles(li, {
                    backgroundColor: PageNation.BtnBackColor,
                    color: PageNation.BtnColor,
                });
            }
            this.list.appendChild(li);
        }
        var span = document.createElement('span');
        span.innerText = '...';
        this.list.appendChild(span);
        for (let i = current - 3; i < current + 2; i++) {
            const li = origin.cloneNode(true);
            // @ts-ignore
            li.innerText = i + 1;
            if (i + 1 === current) {
                this.setElementStyles(li, {
                    backgroundColor: PageNation.BtnBackColor,
                    color: PageNation.BtnColor,
                });
            }
            this.list.appendChild(li);
        }
        var span = document.createElement('span');
        span.innerText = '...';
        this.list.appendChild(span);
        for (let i = totalpage - 2; i < totalpage; i++) {
            const li = origin.cloneNode(true);
            // @ts-ignore
            li.innerText = i + 1;
            if (i + 1 === current) {
                this.setElementStyles(li, {
                    backgroundColor: PageNation.BtnBackColor,
                    color: PageNation.BtnColor,
                });
            }
            this.list.appendChild(li);
        }
    }

    bindLeftList(
        current: number,
        totalpage: number,
        origin: HTMLElement
    ): boolean {
        if (current < 5) {
            // 左边5个 中间 ... 右边2个
            for (let i = 0; i < 5; i++) {
                const li = origin.cloneNode(true);
                // @ts-ignore
                li.innerText = i + 1;
                if (i + 1 === current) {
                    this.setElementStyles(li, {
                        backgroundColor: PageNation.BtnBackColor,
                        color: PageNation.BtnColor,
                    });
                }
                this.list.appendChild(li);
            }
            var span = document.createElement('span');
            span.innerText = '...';
            this.list.appendChild(span);
            for (let i = totalpage - 2; i < totalpage; i++) {
                const li = origin.cloneNode(true);
                // @ts-ignore
                li.innerText = i + 1;
                if (i + 1 === current) {
                    this.setElementStyles(li, {
                        backgroundColor: PageNation.BtnBackColor,
                        color: PageNation.BtnColor,
                    });
                }
                this.list.appendChild(li);
            }
            return true;
        }
        if (current == 5) {
            // 左边5个 中间 ... 右边2个
            for (let i = 0; i < 7; i++) {
                const li = origin.cloneNode(true);
                // @ts-ignore
                li.innerText = i + 1;
                if (i + 1 === current) {
                    this.setElementStyles(li, {
                        backgroundColor: PageNation.BtnBackColor,
                        color: PageNation.BtnColor,
                    });
                }
                this.list.appendChild(li);
            }
            var span = document.createElement('span');
            span.innerText = '...';
            this.list.appendChild(span);

            for (let i = totalpage - 2; i < totalpage; i++) {
                const li = origin.cloneNode(true);
                // @ts-ignore
                li.innerText = i + 1;
                if (i + 1 === current) {
                    this.setElementStyles(li, {
                        backgroundColor: PageNation.BtnBackColor,
                        color: PageNation.BtnColor,
                    });
                }
                this.list.appendChild(li);
            }
            return true;
        }
        // 当前页面 大于倒数第5页
        if (current > totalpage - 4) {
            // 左边5个 中间 ... 右边2个
            for (let i = 0; i < 2; i++) {
                const li = origin.cloneNode(true);
                // @ts-ignore
                li.innerText = i + 1;
                if (i + 1 === current) {
                    this.setElementStyles(li, {
                        backgroundColor: PageNation.BtnBackColor,
                        color: PageNation.BtnColor,
                    });
                }
                this.list.appendChild(li);
            }
            var span = document.createElement('span');
            span.innerText = '...';
            this.list.appendChild(span);
            for (let i = totalpage - 5; i < totalpage; i++) {
                const li = origin.cloneNode(true);
                // @ts-ignore
                li.innerText = i + 1;
                if (i + 1 === current) {
                    this.setElementStyles(li, {
                        backgroundColor: PageNation.BtnBackColor,
                        color: PageNation.BtnColor,
                    });
                }
                this.list.appendChild(li);
            }
            return true;
        }
        if (current == totalpage - 4) {
            // 左边5个 中间 ... 右边2个
            for (let i = 0; i < 2; i++) {
                const li = origin.cloneNode(true);
                // @ts-ignore
                li.innerText = i + 1;
                if (i + 1 === current) {
                    this.setElementStyles(li, {
                        backgroundColor: PageNation.BtnBackColor,
                        color: PageNation.BtnColor,
                    });
                }
                this.list.appendChild(li);
            }
            var span = document.createElement('span');
            span.innerText = '...';
            this.list.appendChild(span);
            for (let i = totalpage - 7; i < totalpage; i++) {
                const li = origin.cloneNode(true);
                // @ts-ignore
                li.innerText = i + 1;
                if (i + 1 === current) {
                    this.setElementStyles(li, {
                        backgroundColor: PageNation.BtnBackColor,
                        color: PageNation.BtnColor,
                    });
                }
                this.list.appendChild(li);
            }
            return true;
        }
        if (current == totalpage - 4) {
            // 左边5个 中间 ... 右边2个
            for (let i = 0; i < 2; i++) {
                const li = origin.cloneNode(true);
                // @ts-ignore
                li.innerText = i + 1;
                if (i + 1 === current) {
                    this.setElementStyles(li, {
                        backgroundColor: PageNation.BtnBackColor,
                        color: PageNation.BtnColor,
                    });
                }
                this.list.appendChild(li);
            }
            var span = document.createElement('span');
            span.innerText = '...';
            this.list.appendChild(span);
            for (let i = totalpage - 7; i < totalpage; i++) {
                const li = origin.cloneNode(true);
                // @ts-ignore
                li.innerText = i + 1;
                if (i + 1 === current) {
                    this.setElementStyles(li, {
                        backgroundColor: PageNation.BtnBackColor,
                        color: PageNation.BtnColor,
                    });
                }
                this.list.appendChild(li);
            }
            return true;
        }
        return false;
    }

    bindPageEvent() {
        this.element.addEventListener(
            'click',
            (event: {
                target: {
                    name: string;
                    dataset: { name: string };
                    innerText: number;
                };
            }) => {
                if (event.target.name === 'first') {
                    if (this.pageInfo.current === 1) return;
                    this.pageInfo.current = 1;
                    this.bindPageHtml();
                }
                if (event.target.name === 'prev') {
                    if (this.pageInfo.current === 1) return;
                    this.pageInfo.current--;
                    this.bindPageHtml();
                }
                if (event.target.name === 'next') {
                    if (this.pageInfo.current === this.pageInfo.totalpage)
                        return;
                    this.pageInfo.current++;
                    this.bindPageHtml();
                }
                if (event.target.name === 'last') {
                    if (this.pageInfo.current === this.pageInfo.totalpage)
                        return;
                    this.pageInfo.current = this.pageInfo.totalpage;
                    this.bindPageHtml();
                }
                if (event.target.name === 'goto') {
                    // 拿到你文本的内容
                    let page = this.inputBox.value - 0;
                    if (isNaN(page)) {
                        page = 1;
                    }
                    if (page <= 1) {
                        page = 1;
                    }
                    if (page >= this.pageInfo.totalpage) {
                        page = this.pageInfo.totalpage;
                    }
                    this.pageInfo.current = page;
                    this.bindPageHtml();
                }
                if (event.target.dataset.name === 'item') {
                    this.pageInfo.current = event.target.innerText - 0;
                    this.bindPageHtml();
                }
            }
        );
    }
}
