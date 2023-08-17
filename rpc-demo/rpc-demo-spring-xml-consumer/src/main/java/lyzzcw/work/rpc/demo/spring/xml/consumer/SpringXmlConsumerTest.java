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
package lyzzcw.work.rpc.demo.spring.xml.consumer;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.consumer.RpcClient;
import lyzzcw.work.rpc.demo.api.UserService;
import lyzzcw.work.rpc.demo.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author lzy
 * @version 1.0.0
 * @description 测试服务消费者整合Spring XML
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:consumer-spring.xml")
@Slf4j
public class SpringXmlConsumerTest {

    @Autowired
    private RpcClient rpcClient;

    @Test
    public void testInterfaceRpc() throws Exception {
        UserService userService = rpcClient.create(UserService.class);
        User user = userService.getUserInfo("lyzzcw",123456L);
        log.info("返回的结果数据===>>> " + user);
        //rpcClient.shutdown();
        while (true){
            Thread.sleep(1000);
        }
    }
}
