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

package ohos.devtools.datasources.transport.hdc;

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * CmdExecutors
 *
 * @since 2021/9/20
 */
public class CmdExecutors {
    private static final Logger LOGGER = LoggerFactory.getLogger(CmdExecutors.class);

    static ExecutorService pool =
        new ThreadPoolExecutor(0, Integer.MAX_VALUE, 3L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

    /**
     * executeCommand
     *
     * @param command command
     * @param timeout timeout
     * @return ExecResult
     */
    public ExecResult executeCommand(ArrayList<String> command, long timeout) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("executeCommand");
        }
        Process process = null;
        InputStream pIn = null;
        InputStream pErr = null;
        IoStreamConsumer outputGobbler = null;
        IoStreamConsumer errorGobbler = null;
        Future<Integer> executeFuture = null;
        try {
            process = new ProcessBuilder(command).start();
            Process prcess = process;
            pIn = process.getInputStream();
            outputGobbler = new IoStreamConsumer(pIn);
            outputGobbler.start();
            pErr = process.getErrorStream();
            errorGobbler = new IoStreamConsumer(pErr);
            errorGobbler.start();
            Callable<Integer> call = new Callable<Integer>() {
                /**
                 * call
                 * @return Integer
                 * @throws InterruptedException InterruptedException
                 */
                public Integer call() throws InterruptedException {
                    return prcess.exitValue();
                }
            };
            prcess.waitFor();
            executeFuture = pool.submit(call);
            int exitCode = executeFuture.get(timeout, TimeUnit.SECONDS);
            ArrayList<String> content = outputGobbler.getContent();
            return new ExecResult(exitCode, content);
        } catch (IOException | TimeoutException | ExecutionException | InterruptedException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("command [" + command + "] failed.", exception);
            }
            return getExecResult(outputGobbler, errorGobbler);
        } finally {
            if (executeFuture != null) {
                executeFuture.cancel(true);
            }
            closeIO(pIn);
            closeIO(pErr);
            if (process != null) {
                process.destroy();
            }
        }
    }

    private void closeIO(InputStream pIn) {
        if (pIn != null) {
            try {
                pIn.close();
            } catch (IOException ioException) {
                LOGGER.error("ioException.", ioException);
            }
        }
    }

    @NotNull
    private ExecResult getExecResult(IoStreamConsumer outputGobbler, IoStreamConsumer errorGobbler) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getExecResult");
        }
        if (errorGobbler.getContent().size() > 0 && outputGobbler.getContent().size() > 0) {
            ArrayList<String> result = new ArrayList<>();
            result.addAll(outputGobbler.getContent());
            result.addAll(errorGobbler.getContent());
            return new ExecResult(-1, result);
        } else if (errorGobbler.getContent().size() > 0) {
            return new ExecResult(-1, errorGobbler.getContent());
        } else {
            return new ExecResult(-1, outputGobbler.getContent());
        }
    }
}
