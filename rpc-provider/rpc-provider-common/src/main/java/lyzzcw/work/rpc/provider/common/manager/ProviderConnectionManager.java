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
package lyzzcw.work.rpc.provider.common.manager;


import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.constant.RpcConstants;

import lyzzcw.work.rpc.protocol.RpcProtocol;
import lyzzcw.work.rpc.protocol.enums.RpcType;
import lyzzcw.work.rpc.protocol.header.RpcHeader;
import lyzzcw.work.rpc.protocol.header.RpcHeaderFactory;
import lyzzcw.work.rpc.protocol.request.RpcRequest;
import lyzzcw.work.rpc.protocol.response.RpcResponse;
import lyzzcw.work.rpc.provider.common.cache.ProviderChannelCache;

import java.util.Set;

/**
 * @author lzy
 * @version 1.0.0
 * @description 服务消费者连接管理器
 */
@Slf4j
public class ProviderConnectionManager {
    /**
     * 扫描并移除不活跃的连接
     */
    public static void scanNotActiveChannel(){
        Set<Channel> channelCache = ProviderChannelCache.getActiveChannelCache();
        if (channelCache == null || channelCache.isEmpty()) return;
        channelCache.stream().forEach((channel) -> {
            if (!channel.isOpen() || !channel.isActive()){
                channel.close();
                ProviderChannelCache.remove(channel);
            }
        });
    }

    /**
     * 发送ping消息
     */
    public static void broadcastPingMessageFromProvider(){
        Set<Channel> channelCache = ProviderChannelCache.getActiveChannelCache();
        if (channelCache == null || channelCache.isEmpty()) return;
        RpcHeader header = RpcHeaderFactory.getRequestHeader(RpcConstants.SERIALIZATION_PROTOSTUFF, RpcType.HEARTBEAT_PROVIDER_TO_CONSUMER_PING.getType());
        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<RpcResponse>();
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setResult(RpcConstants.HEARTBEAT_PING);
        responseRpcProtocol.setHeader(header);
        responseRpcProtocol.setBody(rpcResponse);
        channelCache.stream().forEach((channel) -> {
            if (channel.isOpen() && channel.isActive()){
               log.info("send heartbeat message to service consumer, the consumer is: {}, the heartbeat message is: {}", channel.remoteAddress(), RpcConstants.HEARTBEAT_PING);
               channel.writeAndFlush(responseRpcProtocol).addListener(
                       (ChannelFutureListener) channelFuture -> {
                           //记录发送ping次数
                           if(ProviderChannelCache.incPendingPong(channel)){
                               log.warn("pending heartbeat message pong over limit:{}", channel.id().toString());
                                channel.close();
                           }
                       }
               );
            }
        });
    }
}
