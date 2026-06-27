package com.blue.ocean.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

// 第二个监听器，用来说明：
// spring.factories 同一个 key 下可以配置多个实现类，
// Spring Boot 启动时会把它们一起加载进来。
public class MyListener1 implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("监听器 MyListener1 生效：同类型监听器可以同时注册。");
    }
}
