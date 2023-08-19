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
package lyzzcw.work.rpc.flow.processor.print;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.flow.processor.FlowPostProcessor;
import lyzzcw.work.rpc.protocol.header.RpcHeader;
import lyzzcw.work.rpc.spi.annotation.SPIClass;

/**
 * @author lzy
 * @version 1.0.0
 * @description 打印处理
 */
@SPIClass
@Slf4j
public class PrintFlowPostProcessor implements FlowPostProcessor {
    @Override
    public void postRpcHeaderProcessor(RpcHeader rpcHeader) {
        log.info(getRpcHeaderString(rpcHeader));
    }

    private String getRpcHeaderString(RpcHeader rpcHeader){
        StringBuilder sb = new StringBuilder();
        sb.append("magic: " + rpcHeader.getMagic());
        sb.append(", requestId: " + rpcHeader.getRequestId());
        sb.append(", msgType: " + rpcHeader.getMsgType());
        sb.append(", serializationType: " + rpcHeader.getSerializationType());
        sb.append(", status: " + rpcHeader.getStatus());
        sb.append(", msgLen: " + rpcHeader.getMsgLen());

        return sb.toString();
    }
}
