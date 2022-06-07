// @ts-ignore
import {fps, FpsStruct} from "../../../dist/trace/database/ProcedureWorkerFps.js";
// @ts-ignore
import {Rect} from "../../../dist/trace/component/trace/timer-shaft/Rect.js";

describe(' FPSTest', () => {

    it('FpsTest01', () => {
        let dataList = new Array();
        dataList.push({startTime: 0, dur: 10, frame: {x:0, y:9, width:10, height:10}})
        dataList.push({startTime: 1, dur: 111})
        let rect = new Rect(0, 10, 10, 10);
        fps(dataList, new Set(), 1, 100254, 100254, rect)
    })

    it('FpsTest02', () => {
        let dataList = new Array();
        dataList.push({startTime: 0, dur: 10, frame: {x:0, y:9, width:10, height:10}})
        dataList.push({startTime: 1, dur: 111, frame: {x:0, y:9, width:10, height:10}})
        let rect = new Rect(0, 10, 10, 10);
        fps(dataList, new Set(), 1, 100254, 100254, rect)
    })

    it('FpsTest03', () => {
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

        expect(FpsStruct.draw(ctx, data)).toBeUndefined()
    })


    it('FpsTest04', () => {
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
        new FpsStruct(1);
        FpsStruct.hoverFpsStruct = jest.fn(() => {startNS:200})
        FpsStruct.a = jest.fn(() => data);
        expect(FpsStruct.draw(ctx, data)).toBeUndefined()
    })
    it('FpsTest05 ', () => {
        const canvas = document.createElement('canvas');
        canvas.width = 1;
        canvas.height = 1;
        const ctx = canvas.getContext('2d');
        const Sourcedate = {
            frame: {
                x: 20,
                y: 20,
                width: 100,
                height: 100
            },
            maxFps: 200,
            value: 50
            }
            expect(FpsStruct.draw(ctx,Sourcedate)).toBeUndefined()



    });
});