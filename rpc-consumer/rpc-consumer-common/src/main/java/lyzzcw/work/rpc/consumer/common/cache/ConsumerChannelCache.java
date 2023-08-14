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
package lyzzcw.work.rpc.consumer.common.cache;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lzy
 * @version 1.0.0
 * @description 缓存连接成功的Channel
 */
@Slf4j
public class ConsumerChannelCache {

    //缓存连接成功的Channel
    private static volatile Set<Channel> activeChannelCache = new CopyOnWriteArraySet<>();

    public static void add(Channel channel){
        activeChannelCache.add(channel);
    }

    public static void remove(Channel channel){
        activeChannelCache.remove(channel);
        pendingPongsCache.remove(channel.id().toString());
        if(log.isDebugEnabled()) {
            log.debug("Removed channel,activeChannelCache:{}" +
                    "pendingPongsCache:{}",activeChannelCache,pendingPongsCache);
        }
    }

    public static Set<Channel> getActiveChannelCache(){
        return activeChannelCache;
    }

    //记录未收到pong心跳信息的channelId
    private static ConcurrentHashMap<String, AtomicInteger> pendingPongsCache = new ConcurrentHashMap<>();
    //3次未收到pong响应,断开连接
    private static final int MAX_PENDING_PONG = 3;

    /**
     * 发送ping消息后调用此方法
     * true -> 超过次数断开并重新发起连接
     * false -> 继续等待
     * @param channel
     * @return
     */
    public static boolean incPendingPong(Channel channel){
        int pendingNum = pendingPongsCache.computeIfAbsent(channel.id().toString(),
                k -> new AtomicInteger(0)).incrementAndGet();
        if(log.isDebugEnabled()){
            log.debug("pendingPongsCache details:{}",pendingPongsCache);
        }
        return pendingNum > MAX_PENDING_PONG;
    }

    /**
     * 收到PONG响应后 清空缓存次数
     * @param channel
     */
    public static void cleanPendingPong(Channel channel){
        pendingPongsCache.remove(channel.id().toString());
        if(log.isDebugEnabled()){
            log.debug("pendingPongsCache details:{}",pendingPongsCache);
        }
    }
}
