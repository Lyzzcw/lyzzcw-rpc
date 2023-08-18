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
package lyzzcw.work.rpc.demo.spring.boot.consumer;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.demo.spring.boot.consumer.service.UserServiceImpl;
import lyzzcw.work.rpc.demo.entity.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author lzy
 * @version 1.0.0
 * @description 服务消费者基于SpringBoot的启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"lyzzcw.work.rpc"})
@Slf4j
public class SpringBootConsumerDemoStarter {
    public static void main(String[] args){
        ConfigurableApplicationContext context = SpringApplication.run(SpringBootConsumerDemoStarter.class, args);
        UserServiceImpl userService = context.getBean(UserServiceImpl.class);
        User user = userService.getUser("lyzzcw",12345L);
        log.info("返回的结果数据===>>> " + user);
    }
}
