// @ts-ignore
import {CpuFreqStruct, freq} from "../../../dist/trace/database/ProcedureWorkerFreq.js";
// @ts-ignore
import {Rect} from "../../../dist/trace/component/trace/timer-shaft/Rect.js";

describe('freqTest', () => {

    it('freqTest01', () => {
        let dataList = new Array();
        dataList.push({startTime: 0, dur: 10, frame: {x:0, y:9, width:10, height:10}})
        dataList.push({startTime: 1, dur: 111})
        let rect = new Rect(0, 10, 10, 10);
        freq(dataList, new Set(), 1, 100254, 100254, rect)
    })

    it('freqTest02', () => {
        let dataList = new Array();
        dataList.push({startTime: 0, dur: 10, frame: {x:0, y:9, width:10, height:10}})
        dataList.push({startTime: 1, dur: 111, frame: {x:0, y:9, width:10, height:10}})
        let rect = new Rect(0, 10, 10, 10);
        freq(dataList, new Set(), 1, 100254, 100254, rect)
    })

    it('freqTest03', () => {
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

        expect(CpuFreqStruct.draw(ctx, data)).toBeUndefined()

    })
    it('freqTest04', () => {
        const canvas = document.createElement('canvas');
        canvas.width = 1;
        canvas.height = 1;
        const ctx = canvas.getContext('2d');
        const Sourcedata = {
            frame: {
                x: 20,
                y: 20,
                width: 100,
                height: 100
            },
            maxFreq: 200,
            value: 50
        }
        expect(CpuFreqStruct.draw(ctx,Sourcedata)).toBeUndefined()})

});