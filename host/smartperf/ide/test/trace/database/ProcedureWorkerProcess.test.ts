// @ts-ignore
import {proc, ProcessStruct} from "../../../dist/trace/database/ProcedureWorkerProcess.js";
// @ts-ignore
import {Rect} from "../../../dist/trace/component/trace/timer-shaft/Rect.js";

describe(' ProcessTest', () => {

    it('ProcessTest01', () => {
        let dataList = new Array();
        dataList.push({startTime: 0, dur: 10, frame: {x:0, y:9, width:10, height:10}})
        dataList.push({startTime: 1, dur: 111})
        let rect = new Rect(0, 10, 10, 10);
        proc(dataList, new Set(), 1, 100254, 100254, rect)
    })

    it('ProcessTest02', () => {
        let dataList = new Array();
        dataList.push({startTime: 0, dur: 10, frame: {x:0, y:9, width:10, height:10}})
        dataList.push({startTime: 1, dur: 111, frame: {x:0, y:9, width:10, height:10}})
        let rect = new Rect(0, 10, 10, 10);
        proc(dataList, new Set(), 1, 100254, 100254, rect)
    })

    it('ProcessTest03', () => {
        const canvas = document.createElement('canvas');
        canvas.width = 1;
        canvas.height = 1;
        const ctx = canvas.getContext('2d');

        const data = {
            frame: {
                x: 20,
                y: 20,
                width: 100,
                height: 100
            },
            startNS: 200,
            value: 50
        }
        expect(ProcessStruct.draw(ctx, data)).toBeUndefined()
    })

    it('ProcessTest04', () => {
        const node = {
            frame: {
                x: 20,
                y: 20,
                width: 100,
                height: 100
            },
            startNS: 200,
            value: 50
        }
        const frame = {
            x: 20,
            y: 20,
            width: 100,
            height: 100
        }
        expect(ProcessStruct.setFrame(node, 1,1,1,frame)).toBeUndefined()
    })
});