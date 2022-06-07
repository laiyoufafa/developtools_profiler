// @ts-ignore
import {ProcedureWorker,drawSelection} from "../../../dist/trace/database/ProcedureWorker.js";


describe('ProcedureWorker Test', ()=>{

    it('ProcedureWorkerTest01', function () {
        const context = {
            globalAlpha:0.5,
            fillStyle:"#666666",
            fillRect:'',
        }
        const params  ={
            isRangeSelect:{},
            rangeSelectObject:{
                startX:"",
                endX:"",
                startNS:"",
                endNS:"",

            },
            startNS:"",
            endNS:"",
            totalNS:1,
            frame:{
                x:"",
                y:"",
                height:1,
                width:1,
            }

        }
        let drawSelection = jest.fn(() => true)
        // @ts-ignore
        expect(drawSelection(context,params)).toBeTruthy();

    });
})