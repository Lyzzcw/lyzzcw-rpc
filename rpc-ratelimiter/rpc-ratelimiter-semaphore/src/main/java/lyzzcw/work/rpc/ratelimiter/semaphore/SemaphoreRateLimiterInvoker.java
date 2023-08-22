/**
 * Copyright 2022-9999 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lyzzcw.work.rpc.ratelimiter.semaphore;



import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.ratelimiter.base.AbstractRateLimiterInvoker;
import lyzzcw.work.rpc.spi.annotation.SPIClass;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lzy
 * @version 1.0.0
 * @description 基于Semaphore的限流策略
 */
@SPIClass
@Slf4j
public class SemaphoreRateLimiterInvoker extends AbstractRateLimiterInvoker {
    private Semaphore semaphore;
    private final AtomicInteger currentCounter = new AtomicInteger(0);
    private volatile long lastTimeStamp = System.currentTimeMillis();

    @Override
    public void init(int permits, int milliSeconds) {
        super.init(permits, milliSeconds);
        this.semaphore = new Semaphore(permits);
    }

    @Override
    public boolean tryAcquire() {
        log.info("execute semaphore rate limiter...");
        //获取当前时间
        long currentTimeStamp = System.currentTimeMillis();
        //超过一个时间周期
        if (currentTimeStamp - lastTimeStamp >= milliSeconds){
            //重置窗口开始时间
            lastTimeStamp = currentTimeStamp;
            //释放所有资源
            semaphore.release(currentCounter.get());
            //重置计数
            currentCounter.set(0);
        }
        boolean result = semaphore.tryAcquire();
        //成功获取资源
        if (result){
            currentCounter.incrementAndGet();
        }
        return result;
    }

    @Override
    public void release() {
        //TODO ignore
        //semaphore.release();
    }
}