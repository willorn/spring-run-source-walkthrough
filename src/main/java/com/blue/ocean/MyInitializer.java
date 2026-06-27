package com.blue.ocean;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

// 这是一个 Spring 容器初始化器。
//
// 它的作用：
// 在容器 refresh 之前，对 ConfigurableApplicationContext 做预处理。
//
// 这个类本身不是靠 @Component 扫描进来的，
// 而是通过 META-INF/spring.factories 注册后，
// 被 SpringApplication 构造器中的 getSpringFactoriesInstances(...) 找到。
//
// 教学关键点：
// 1. Spring Boot 在“构造 SpringApplication 对象”时就会收集初始化器
// 2. 真正执行 initialize(...) 则是在 run() -> prepareContext(...) -> applyInitializers(...) 阶段
public class MyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // 这里能拿到容器和环境，适合做“refresh 前”的定制动作。
        ConfigurableEnvironment env = applicationContext.getEnvironment();

        // 这里先用打印演示初始化器确实参与了启动流程。
        // 如果需要，也可以在这里注册属性源、激活 profile、附加 BeanFactoryPostProcessor 等。
        System.out.println("初始化器生效：app.env=" + env.getProperty("app.env"));
    }
}
