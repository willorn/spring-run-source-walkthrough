package com.blue.ocean.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

// 这是一个 Spring 事件监听器。
//
// 注册方式：
// 不是通过注解扫描，而是通过 META-INF/spring.factories 让 Spring Boot 在启动早期加载。
//
// 对应源码位置可以这样理解：
// 1. SpringApplication 构造器里先通过 SpringFactoriesLoader 收集监听器
// 2. run() 过程中这些监听器会逐步参与 starting / environmentPrepared / contextPrepared 等阶段
// 3. 当前这个监听器监听的是 ContextRefreshedEvent，因此它会在容器 refresh 完成后触发
//
// 一般在 ContextRefreshedEvent 这里适合做什么？
// 1. 做“容器级别”的收尾检查，例如确认关键 Bean 是否已经装配完成
// 2. 做依赖 Spring 容器的初始化动作，例如从容器里拿到某些组件后再执行启动逻辑
// 3. 做一些启动完成后的通知、日志打印、状态标记
//
// 为什么这些事情适合放在这里？
// 因为走到 ContextRefreshedEvent 时，前面的关键准备工作已经基本完成了：
// 1. Environment 已经准备好，配置项和 profile 已经可用了
// 2. ApplicationContext 已经创建好
// 3. BeanDefinition 已经加载完成
// 4. 非懒加载单例 Bean 已经实例化完成
// 5. BeanPostProcessor、事件广播器等基础设施已经就绪
//
// 所以，这个时机最大的特点是：
// “容器已经可用，可以放心依赖 Spring 容器里的 Bean 做事情”。
//
// 但它又不是整个 Spring Boot 启动链路的最后一步。
// 在它后面，SpringApplication.run() 还可能继续执行：
// 1. afterRefresh(context, args)
// 2. 发布 started 事件
// 3. 调用 ApplicationRunner / CommandLineRunner
// 4. 发布 running 事件
//
// 因此这里更适合做“容器刷新完成后立即可以做的事情”，
// 而不是那种必须等到整个应用完全进入 running 状态之后才做的动作。
//
// 教学上可以这样理解前因后果：
// - 前面：Spring 已经把容器搭好了，Bean 也准备得差不多了
// - 这里：通知你“容器这一轮 refresh 已经结束，你现在可以基于容器做事了”
// - 后面：Spring Boot 还会继续执行 Runner、发布更靠后的启动事件
public class MyListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // ContextRefreshedEvent 表示容器完成了一次 refresh。
        //
        // 如果把启动过程看成“搭舞台 -> 演员到位 -> 开场”，
        // 那这个事件更接近“舞台已经搭好，演员也基本到位，可以开始做开场前确认了”。
        //
        // 典型场景：
        // 1. 检查某些核心 Bean 是否存在
        // 2. 执行依赖容器的预热逻辑
        // 3. 输出启动完成的阶段性日志
        //
        // 不太适合放在这里的事情：
        // 1. 过重的阻塞任务，会拖慢启动收尾
        // 2. 必须等 ApplicationRunner / CommandLineRunner 跑完之后再做的逻辑
        System.out.println("监听器 MyListener 生效：容器已经完成 refresh。");
    }
}
