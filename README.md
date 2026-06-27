# Spring Run Source Walkthrough

这个仓库是一个围绕 `SpringApplication.run(...)` 主流程的源码学习样例。

它不是为了做功能复杂的业务项目，而是为了把下面这条启动链路讲清楚：

```java
SpringApplication.run(BlueOceanApplication.class, args);
```

我把一个最小可运行的 Spring Boot demo、若干启动扩展点，以及少量带中文注释的 Spring 源码切片放在同一个仓库里，方便按主线阅读、断点和做讲解。

## 项目目标

这个仓库主要回答 3 个问题：

1. `SpringApplication.run(...)` 到底做了什么
2. Spring 容器真正启动的核心为什么落在 `refresh()`
3. 当前 demo 里的 `MyInitializer`、`MyListener`、`spring.factories` 是怎么挂到启动流程里的

## 版本信息

- Spring Boot: `2.3.12.RELEASE`
- Spring Framework: `5.2.15.RELEASE`
- JDK: `8`

阅读源码时尽量和这组版本保持一致，否则很多文章里的细节会对不上。

## 推荐阅读顺序

### 1. 先看项目入口

- [BlueOceanApplication.java](src/main/java/com/blue/ocean/BlueOceanApplication.java)

只盯住这一句：

```java
SpringApplication.run(BlueOceanApplication.class, args);
```

### 2. 再看 Spring Boot 如何组织启动

- [SpringApplication.java](research/spring-boot-2.3.12/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/SpringApplication.java)

重点方法：

- `SpringApplication(...)`
- `run(String... args)`
- `prepareEnvironment(...)`
- `prepareContext(...)`
- `refreshContext(...)`

### 3. 看应用类型如何被推断

- [WebApplicationType.java](research/spring-boot-2.3.12/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/WebApplicationType.java)

重点看：

- `deduceFromClasspath()`

### 4. 看 Spring 容器真正启动的主干

- [AbstractApplicationContext.java](research/spring-framework-5.2.15/spring-context/src/main/java/org/springframework/context/support/AbstractApplicationContext.java)

重点看：

- `refresh()`

### 5. 再补 BeanDefinition 和 BeanPostProcessor 这层基础

- [DefaultListableBeanFactory.java](research/spring-framework-5.2.15/spring-beans/src/main/java/org/springframework/beans/factory/support/DefaultListableBeanFactory.java)
- [BeanDefinition.java](research/spring-framework-5.2.15/spring-beans/src/main/java/org/springframework/beans/factory/config/BeanDefinition.java)
- [BeanPostProcessor.java](research/spring-framework-5.2.15/spring-beans/src/main/java/org/springframework/beans/factory/config/BeanPostProcessor.java)

## 当前 demo 里的扩展点

这个项目里放了两类值得对照着看的扩展机制。

### Spring 启动扩展点

- [MyInitializer.java](src/main/java/com/blue/ocean/MyInitializer.java)
- [MyListener.java](src/main/java/com/blue/ocean/listener/MyListener.java)
- [MyListener1.java](src/main/java/com/blue/ocean/listener/MyListener1.java)
- [spring.factories](src/main/resources/META-INF/spring.factories)

它们对应的启动位置大致是：

- `MyInitializer` 挂在 `prepareContext(...) -> applyInitializers(...)`
- `MyListener` / `MyListener1` 通过 `spring.factories` 在启动早期被加载
- `ContextRefreshedEvent` 对应 `AbstractApplicationContext.refresh()` 末尾阶段

### Java SPI 示例

- [MyService.java](src/main/java/com/blue/ocean/spi/MyService.java)
- [MyServiceImpl1.java](src/main/java/com/blue/ocean/spi/MyServiceImpl1.java)
- [MyServiceImpl2.java](src/main/java/com/blue/ocean/spi/MyServiceImpl2.java)
- [SpiDemo.java](src/main/java/com/blue/ocean/spi/SpiDemo.java)
- [META-INF/services/com.blue.ocean.spi.MyService](src/main/resources/META-INF/services/com.blue.ocean.spi.MyService)

这部分适合拿来对比：

- 标准 Java SPI 怎么通过 `ServiceLoader` 工作
- Spring 风格的扩展点怎么通过 `spring.factories` 工作

## 目录说明

```text
spring-demo-blue-ocean/
├─ src/                  # 当前可运行 demo
├─ docs/                 # 启动主线与 SPI 讲解文档
├─ labs/                 # Maven 依赖冲突小实验
└─ research/             # 精简后的 Spring 源码研究切片
```

`research/` 里没有保留整套 Spring 源码，只保留了和 `run()` 主流程直接相关的 6 个核心类，以及上游许可证文件。

## 如何运行

在项目根目录执行：

```bash
mvn spring-boot:run
```

或者：

```bash
mvn package
java -jar target/spring-demo-blue-ocean-1.0-SNAPSHOT.jar
```

## 推荐搭配阅读

- [docs/spring-startup-reading-path.md](docs/spring-startup-reading-path.md)
- [docs/spring-startup-and-spi-guide.md](docs/spring-startup-and-spi-guide.md)
- [research/README.md](research/README.md)

## 说明

`research/` 下的源码文件主要用于阅读、标注和断点分析。

当前 demo 运行时依赖的仍然是 Maven 仓库中的 Spring jar，这意味着：

- 修改 `research/` 下的源码，不会直接改变 demo 的运行结果
- 如果后续要验证“改源码后运行行为变化”，需要再单独做本地构建和依赖替换
