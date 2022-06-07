//@ts-ignore
import {SpHiPerf} from '../../../../dist/trace/component/hiperf/SpHiPerf.js'

describe('SpHiPerf Test',()=>{
    it('SpHiPerfTest01 ', function () {
        let  spHiPerf =new SpHiPerf();
        expect(spHiPerf.init()).toBeUndefined()
    });

    it('SpHiPerfTest02 ', function () {
        let  spHiPerf =new SpHiPerf();
        expect(spHiPerf.initFolder()).toBeUndefined()
    });

    it('SpHiPerfTest03 ', function () {
        let  spHiPerf =new SpHiPerf();
        expect(spHiPerf.initCpuMerge()).toBeUndefined()
    });
    it('SpHiPerfTest04 ', function () {
        let  spHiPerf =new SpHiPerf();
        expect(spHiPerf.initCpu()).toBeUndefined()
    });
    it('SpHiPerfTest04 ', function () {
        let  spHiPerf =new SpHiPerf();
        expect(spHiPerf.initProcess()).toBeUndefined()
    });
})