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
package lyzzcw.work.rpc.demo.spring.annotation.consumer;


import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.demo.spring.annotation.consumer.config.SpringAnnotationConsumerConfig;
import lyzzcw.work.rpc.demo.spring.annotation.consumer.service.UserServiceImpl;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author lzy
 * @version 1.0.0
 * @description 基于Spring注解的消费者测试类
 */
@Slf4j
public class SpringAnnotationConsumerTest {
    @Test
    public void testInterfaceRpc() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringAnnotationConsumerConfig.class);
        UserServiceImpl userService = context.getBean(UserServiceImpl.class);
        log.info("返回的结果数据===>>> " + userService.getUser("lyzzcw",123L));
    }
}
