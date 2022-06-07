// @ts-ignore
import {debug, error, info, log, trace, warn} from "../../dist/log/Log.js";

describe(' logTest', () => {

    it('LogTest01', () => {
        error("111")
    })
    it('LogTest02', () => {
        warn("111")
    })
    it('LogTest03', () => {
        info("111")
    })
    it('LogTest04', () => {
        debug("111")
    })
    it('LogTest05', () => {
        trace("111")
    })
    it('LogTest05', () => {
        log("111")
    })
});