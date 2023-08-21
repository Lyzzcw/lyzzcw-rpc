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
package lyzzcw.work.rpc.disuse.lru.strategy;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.disuse.api.DisuseStrategy;
import lyzzcw.work.rpc.disuse.api.connection.ConnectionInfo;
import lyzzcw.work.rpc.spi.annotation.SPIClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author lzy
 * @version 1.0.0
 * @description 判断最近被使用的时间，目前最远的数据优先被淘汰。
 */
@SPIClass
@Slf4j
public class LruDisuseStrategy implements DisuseStrategy {
    private static final Comparator<ConnectionInfo> lastUseTimeComparator = (o1, o2) -> {
        //先判断最新使用时间
        if(o1.getLastUseTime() - o2.getLastUseTime() > 0){
            return 1;
        }else if(o1.getLastUseTime() - o2.getLastUseTime() == 0){
            //判断使用次数
            return o1.getUseCount() - o2.getUseCount() > 0 ? 1 : -1;
        }else {
            return -1;
        }
    };
    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        log.info("execute lru disuse strategy...");
        if (connectionList.isEmpty()) return null;
        Collections.sort(connectionList, lastUseTimeComparator);
        return connectionList.get(0);
    }

//    public static void main(String[] args) {
//        System.out.println(1692588439486L);
//        List<ConnectionInfo> list = new ArrayList<>();
//        ConnectionInfo c1 = new ConnectionInfo();
//        c1.setLastUseTime(1692588439486L);
//        c1.incrementUseCount();
//        list.add(c1);
//
//        ConnectionInfo c2 = new ConnectionInfo();
//        c2.setLastUseTime(1692588439486L);
//        c2.incrementUseCount();
//        c2.incrementUseCount();
//        c2.incrementUseCount();
//        list.add(c2);
//
//        ConnectionInfo c3 = new ConnectionInfo();
//        c3.setLastUseTime(1692588439499L);
//        c3.incrementUseCount();
//        c3.incrementUseCount();
//        c3.incrementUseCount();
//        list.add(c3);
//
//        Collections.sort(list,lastUseTimeComparator);
//        System.out.println(list);
//    }
}
