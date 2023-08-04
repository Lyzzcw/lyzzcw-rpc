/**
 * Copyright 2020-9999 the original author or authors.
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
package lyzzcw.work.rpc.proxy.api.future;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.protocol.RpcProtocol;
import lyzzcw.work.rpc.protocol.enums.RpcStatus;
import lyzzcw.work.rpc.protocol.header.RpcHeader;
import lyzzcw.work.rpc.protocol.request.RpcRequest;
import lyzzcw.work.rpc.protocol.response.RpcResponse;
import lyzzcw.work.rpc.proxy.api.callback.AsyncRpcCallback;
import lyzzcw.work.rpc.threadpool.ConcurrentThreadPool;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author lzy
 * @version 1.0.0
 * @description RPC框架获取异步结果的自定义Future
 */
@Slf4j
public class RpcFuture extends CompletableFuture<Object> {

    private Sync sync;
    private RpcProtocol<RpcRequest> requestRpcProtocol;
    private RpcProtocol<RpcResponse> responseRpcProtocol;
    private long startTime;
    //默认超时时间5s
    private long responseTimeThreshold = 5000;
    //回调接口
    private List<AsyncRpcCallback> pendingCallbacks = Lists.newArrayList();
    //添加和执行回调方法时，执行加锁操作
    private ReentrantLock lock = new ReentrantLock();

    private ConcurrentThreadPool concurrentThreadPool;

    public RpcFuture(RpcProtocol<RpcRequest> requestRpcProtocol, ConcurrentThreadPool concurrentThreadPool) {
        this.sync = new Sync();
        this.requestRpcProtocol = requestRpcProtocol;
        this.startTime = System.currentTimeMillis();
        this.concurrentThreadPool = concurrentThreadPool;
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(-1);
        return this.getResult(this.responseRpcProtocol);
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        if (success) {
            return this.getResult(this.responseRpcProtocol);
        } else {
            throw new RuntimeException("Timeout exception. Request id: " + this.requestRpcProtocol.getHeader().getRequestId()
                    + ". Request class name: " + this.requestRpcProtocol.getBody().getClassName()
                    + ". Request method: " + this.requestRpcProtocol.getBody().getMethodName());
        }
    }

    /**
     * 获取最终结果
     */
    private Object getResult(RpcProtocol<RpcResponse> responseRpcProtocol){
        if (responseRpcProtocol == null){
            return null;
        }
        RpcHeader header = responseRpcProtocol.getHeader();
        //服务提供者抛出了异常
        if ((byte) RpcStatus.FAIL.getCode() == header.getStatus()){
            throw new RuntimeException("rpc provider throws exception...");
        }
        return responseRpcProtocol.getBody().getResult();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    //唤醒阻塞线程获取响应的结果数据
    public void done(RpcProtocol<RpcResponse> responseRpcProtocol) {
        this.responseRpcProtocol = responseRpcProtocol;
        sync.release(1);
        invokeCallbacks();
        // Threshold
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > this.responseTimeThreshold) {
            log.warn("Service response time is too slow. Request id = " + responseRpcProtocol.getHeader().getRequestId() + ". Response Time = " + responseTime + "ms");
        }
    }

    private void invokeCallbacks() {
        lock.lock();
        try {
            for (final AsyncRpcCallback callback : pendingCallbacks) {
                runCallback(callback);
            }
        } finally {
            lock.unlock();
        }
    }

    public RpcFuture addCallback(AsyncRpcCallback callback) {
        lock.lock();
        try {
            if (isDone()) {
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    private void runCallback(final AsyncRpcCallback callback) {
        final RpcResponse res = this.responseRpcProtocol.getBody();
        concurrentThreadPool.submit(() -> {
            if (!res.isError()) {
                callback.onSuccess(res.getResult());
            } else {
                callback.onException(new RuntimeException("Response error", new Throwable(res.getError())));
            }
        });
    }

    static class Sync extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = 1L;

        //future status
        private final int done = 1;
        private final int pending = 0;

        //独占方式。尝试获取资源，成功则返回true，失败则返回false。
        protected boolean tryAcquire(int acquires) {
            return getState() == done;
        }
        //独占方式。尝试释放资源，成功则返回true，失败则返回false。
        protected boolean tryRelease(int releases) {
            if (getState() == pending) {
                if (compareAndSetState(pending, done)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isDone() {
            getState();
            return getState() == done;
        }
    }
}
