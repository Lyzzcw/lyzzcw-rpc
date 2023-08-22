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
package lyzzcw.work.rpc.fusing.counter;


import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.constant.RpcConstants;
import lyzzcw.work.rpc.fusing.base.AbstractFusingInvoker;
import lyzzcw.work.rpc.spi.annotation.SPIClass;

/**
 * @author lzy
 * @version 1.0.0
 * @description 在一段时间内基于错误数量的熔断策略
 */
@SPIClass
@Slf4j
public class CounterFusingInvoker extends AbstractFusingInvoker {

    @Override
    public boolean invokeFusingStrategy() {
        boolean result = false;
        switch (fusingStatus.get()){
            //关闭状态
            case RpcConstants.FUSING_STATUS_CLOSED:
                result =  this.invokeClosedFusingStrategy();
                break;
            //半开启状态
            case RpcConstants.FUSING_STATUS_HALF_OPEN:
                result = this.invokeHalfOpenFusingStrategy();
                break;
            //开启状态
            case RpcConstants.FUSING_STATUS_OPEN:
                result = this.invokeOpenFusingStrategy();
                break;
            default:
                result = this.invokeClosedFusingStrategy();
                break;
        }
        log.info("execute counter fusing strategy, current fusing status is {}", fusingStatus.get());
        return result;
    }

    @Override
    public double getFailureStrategyValue() {
        return currentFailureCounter.doubleValue();
    }
}
