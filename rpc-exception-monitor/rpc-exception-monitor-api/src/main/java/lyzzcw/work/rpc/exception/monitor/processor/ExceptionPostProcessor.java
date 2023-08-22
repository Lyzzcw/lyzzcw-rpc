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
package lyzzcw.work.rpc.exception.monitor.processor;


import lyzzcw.work.rpc.constant.RpcConstants;
import lyzzcw.work.rpc.spi.annotation.SPI;

/**
 * @author lzy
 * @version 1.0.0
 * @description 异常信息后置处理器
 */
@SPI(RpcConstants.EXCEPTION_POST_PROCESSOR_PRINT)
public interface ExceptionPostProcessor {

    /**
     * 处理异常信息，进行统计等
     */
    void postExceptionProcessor(Throwable e);
}
