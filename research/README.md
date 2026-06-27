# Spring `run()` 源码研究

这个目录只保留和 `SpringApplication.run(...)` 主流程直接相关的少量源码切片，
用于阅读、写注释和建立启动主线认知。

对应版本：

- Spring Boot: `2.3.12.RELEASE`
- Spring Framework: `5.2.15.RELEASE`

当前保留的核心文件：

- `spring-boot-2.3.12/.../SpringApplication.java`
- `spring-boot-2.3.12/.../WebApplicationType.java`
- `spring-framework-5.2.15/.../AbstractApplicationContext.java`
- `spring-framework-5.2.15/.../DefaultListableBeanFactory.java`
- `spring-framework-5.2.15/.../BeanDefinition.java`
- `spring-framework-5.2.15/.../BeanPostProcessor.java`

## 推荐阅读顺序

### 1. 先看 demo 入口

- `../src/main/java/com/blue/ocean/BlueOceanApplication.java`

重点入口：

```java
SpringApplication.run(BlueOceanApplication.class, args);
```

### 2. 看 Spring Boot 如何组织启动

- `spring-boot-2.3.12/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/SpringApplication.java`

重点看：

- 构造器 `SpringApplication(...)`
- 方法 `run(String... args)`
- `prepareEnvironment(...)`
- `prepareContext(...)`
- `refreshContext(...)`

### 3. 看应用类型如何被推断

- `spring-boot-2.3.12/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/WebApplicationType.java`

重点看：

- `deduceFromClasspath()`

### 4. 看 Spring 容器真正启动的主干

- `spring-framework-5.2.15/spring-context/src/main/java/org/springframework/context/support/AbstractApplicationContext.java`

重点看：

- `refresh()`

### 5. 看 BeanDefinition 存放和注册位置

- `spring-framework-5.2.15/spring-beans/src/main/java/org/springframework/beans/factory/support/DefaultListableBeanFactory.java`

重点看：

- `beanDefinitionMap`
- `beanDefinitionNames`
- `registerBeanDefinition(...)`

### 6. 看 BeanDefinition 和 BeanPostProcessor 的职责

- `spring-framework-5.2.15/spring-beans/src/main/java/org/springframework/beans/factory/config/BeanDefinition.java`
- `spring-framework-5.2.15/spring-beans/src/main/java/org/springframework/beans/factory/config/BeanPostProcessor.java`

## 和当前 demo 的对应关系

当前项目里的扩展点可以这样映射回启动链路：

- `MyInitializer` 对应 `SpringApplication.run()` 里的 `prepareContext(...) -> applyInitializers(...)`
- `MyListener` 对应 `AbstractApplicationContext.refresh()` 完成阶段附近发布的容器事件
- `spring.factories` 对应 Spring Boot 早期通过 `SpringFactoriesLoader` 收集初始化器和监听器

## 说明

这里只保留了研究主线需要的源码文件，不再提交整套 Spring 源码快照。

当前 demo 运行时依赖的仍然是 Maven 仓库里的 Spring jar，所以：

- 这些源码文件适合阅读、标注和断点分析
- 修改这些研究文件，不会直接改变当前 demo 的运行结果

两个上游源码目录中的 `LICENSE.txt` 已保留，用于说明源码来源。
