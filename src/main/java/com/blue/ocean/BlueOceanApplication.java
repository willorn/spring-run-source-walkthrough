package com.blue.ocean;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// 这是 Spring Boot 的启动入口。
// 课堂上可以从这里顺着往下讲：
// 1. main 方法调用 SpringApplication.run(...)
// 2. run 内部会先创建 SpringApplication 对象
// 3. 构造器阶段会提前收集初始化器、监听器等扩展点
// 4. run 阶段再准备环境、创建容器、执行 refresh、完成启动
@SpringBootApplication
@EnableScheduling
public class BlueOceanApplication {
    public static void main(String[] args) {
        // 这里是最常见的启动写法。
        // 它等价于：
        // SpringApplication application = new SpringApplication(BlueOceanApplication.class);
        // application.run(args);
        //
        // 注意：
        // run(...) 不是一上来就 refresh 容器，
        // 它前面还会先完成环境准备、监听器回调、上下文创建、source 装载等步骤。
        SpringApplication.run(BlueOceanApplication.class,args);
    }
}
