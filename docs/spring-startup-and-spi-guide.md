# Spring 启动与 SPI 讲解文档

本文档结合当前项目，讲两件事：

1. 什么是 SPI，它在本项目里出现在哪里
2. `SpringApplication.run(...)` 在启动过程中做了什么

项目版本信息：

- Spring Boot: `2.3.12.RELEASE`
- Spring Framework: `5.2.15.RELEASE`
- 启动入口: `src/main/java/com/blue/ocean/BlueOceanApplication.java`

如果你准备结合源码讲解，建议优先看本机 Maven 仓库里的 source jar：

- Spring Boot 源码：
  `/Users/tianyi/.m2/repository/org/springframework/boot/spring-boot/2.3.12.RELEASE/spring-boot-2.3.12.RELEASE-sources.jar`
- spring-context 源码：
  `/Users/tianyi/.m2/repository/org/springframework/spring-context/5.2.15.RELEASE/spring-context-5.2.15.RELEASE-sources.jar`
- spring-beans 源码：
  `/Users/tianyi/.m2/repository/org/springframework/spring-beans/5.2.15.RELEASE/spring-beans-5.2.15.RELEASE-sources.jar`

为什么这里要特别强调版本？

因为你这份项目是 Spring Boot 2.x 系列，很多文章和视频可能讲的是 Boot 3.x / Spring 6.x。
如果版本不对，虽然类名经常还能对上，但源码细节、导包路径、自动配置实现方式都可能出现差异。
所以这份教程默认按下面这组版本来找源码：

- `SpringApplication` 看 `2.3.12.RELEASE`
- `AbstractApplicationContext`、`BeanDefinition`、`BeanPostProcessor` 看 `5.2.15.RELEASE`

## 1. 这个项目里有两类 SPI

这个项目很适合拿来区分两种常见的 SPI 思路。

### 1.1 标准 Java SPI

标准 Java SPI 依赖 JDK 自带的 `ServiceLoader`。

在本项目中，对应位置是：

- 接口：`src/main/java/com/blue/ocean/spi/MyService.java`
- 实现类：
  - `src/main/java/com/blue/ocean/spi/MyServiceImpl1.java`
  - `src/main/java/com/blue/ocean/spi/MyServiceImpl2.java`
- SPI 配置文件：`src/main/resources/META-INF/services/com.blue.ocean.spi.MyService`
- 调用示例：`src/main/java/com/blue/ocean/spi/SpiDemo.java`

它的工作方式是：

1. 调用方只面向接口 `MyService`
2. JDK 通过 `ServiceLoader.load(MyService.class)` 去类路径中查找
   `META-INF/services/com.blue.ocean.spi.MyService`
3. 配置文件中写了哪些实现类，JDK 就会尝试把它们加载出来
4. 调用方遍历这些实现类，而不需要手动 `new`

也就是说，SPI 的核心思想是：

> 接口由框架或调用方定义，实现由外部提供，发现过程通过约定的配置位置完成。

### 1.2 Spring 风格的 SPI

Spring 体系里也大量使用“约定位置 + 反射加载”的扩展机制。

在这个项目中，对应位置是：

- `src/main/resources/META-INF/spring.factories`
- `src/main/java/com/blue/ocean/MyInitializer.java`
- `src/main/java/com/blue/ocean/listener/MyListener.java`
- `src/main/java/com/blue/ocean/listener/MyListener1.java`

这里的作用是：

1. 在 `spring.factories` 里声明某个接口对应哪些实现类
2. Spring Boot 启动时读取这个文件
3. 找到 `ApplicationContextInitializer` 和 `ApplicationListener` 的实现类
4. 反射创建对象，加入启动流程

所以可以这样理解：

- Java SPI 是 JDK 官方给出的通用扩展机制
- Spring SPI 是 Spring 在框架内部大量使用的一套扩展装配方式
- 二者思想很像，差别主要在“配置文件位置”和“加载入口”

## 2. 如何使用标准 Java SPI

本项目已经给出一个最小示例。

### 2.1 第一步：定义接口

```java
public interface MyService {
    void say();
}
```

### 2.2 第二步：提供实现类

```java
public class MyServiceImpl1 implements MyService
public class MyServiceImpl2 implements MyService
```

### 2.3 第三步：创建 SPI 配置文件

路径必须是：

```text
META-INF/services/接口全限定名
```

本项目中的文件是：

```text
META-INF/services/com.blue.ocean.spi.MyService
```

文件内容是实现类全限定名：

```text
com.blue.ocean.spi.MyServiceImpl1
com.blue.ocean.spi.MyServiceImpl2
```

### 2.4 第四步：通过 ServiceLoader 加载

```java
ServiceLoader<MyService> services = ServiceLoader.load(MyService.class);
services.forEach(myService -> myService.say());
```

这样，调用方就不需要关心到底有哪些实现类。

## 3. 如何使用 Spring 风格 SPI

### 3.1 本项目的注册方式

`spring.factories` 中注册了：

```properties
org.springframework.context.ApplicationListener=\
com.blue.ocean.listener.MyListener,\
com.blue.ocean.listener.MyListener1

org.springframework.context.ApplicationContextInitializer=\
com.blue.ocean.MyInitializer
```

含义是：

- `ApplicationListener` 的实现类有两个
- `ApplicationContextInitializer` 的实现类有一个

### 3.2 Spring Boot 如何把它们加载进来

`SpringApplication` 构造器中有这样一段核心逻辑：

```java
this.webApplicationType = WebApplicationType.deduceFromClasspath();
setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
```

这里说明两件事：

1. `SpringApplication` 在构造阶段就开始收集初始化器和监听器
2. 这些对象的来源就是 `spring.factories`

继续往下看，`getSpringFactoriesInstances(...)` 内部会调用：

```java
SpringFactoriesLoader.loadFactoryNames(type, classLoader)
```

而 `SpringFactoriesLoader` 会去扫描类路径下所有：

```text
META-INF/spring.factories
```

然后按 key 找到实现类列表，再通过反射创建实例。

## 4. SpringApplication.run(...) 做了什么

本项目的入口是：

```java
SpringApplication.run(BlueOceanApplication.class, args);
```

这句代码最终等价于：

```java
new SpringApplication(BlueOceanApplication.class).run(args);
```

所以讲 `run` 时，最好分成两个阶段：

1. 构造器阶段
2. run 阶段

---

## 5. 第一阶段：构造器里做了什么

`SpringApplication` 构造器中的关键逻辑如下：

```java
this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
this.webApplicationType = WebApplicationType.deduceFromClasspath();
setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
this.mainApplicationClass = deduceMainApplicationClass();
```

可以把它讲成 5 个点：

### 5.1 保存主启动类

这里的 `primarySources` 就是 `BlueOceanApplication.class`。

它后面会被注册到容器中，作为配置源参与解析。

### 5.2 推断应用类型

通过类路径判断当前是：

- 普通应用
- Servlet Web 应用
- Reactive Web 应用

当前项目引入了 `spring-boot-starter-web`，所以会推断成 Servlet Web 应用。

### 5.3 收集初始化器

通过：

```java
getSpringFactoriesInstances(ApplicationContextInitializer.class)
```

去读取 `META-INF/spring.factories`，把 `MyInitializer` 找出来。

### 5.4 收集监听器

通过：

```java
getSpringFactoriesInstances(ApplicationListener.class)
```

把 `MyListener` 和 `MyListener1` 找出来。

### 5.5 推断 main 方法所在类

框架会从调用栈中推断主类，用于日志打印等用途。

---

## 6. 第二阶段：run 方法里做了什么

`run(String... args)` 的核心主线可以整理成下面这几步：

```java
configureHeadlessProperty();
SpringApplicationRunListeners listeners = getRunListeners(args);
listeners.starting();

ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
Banner printedBanner = printBanner(environment);
context = createApplicationContext();
prepareContext(context, environment, listeners, applicationArguments, printedBanner);
refreshContext(context);
afterRefresh(context, applicationArguments);
listeners.started(context);
callRunners(context, applicationArguments);
listeners.running(context);
```

下面按教学顺序拆开讲。

### 6.1 准备运行监听器

```java
SpringApplicationRunListeners listeners = getRunListeners(args);
listeners.starting();
```

这一步会加载 `SpringApplicationRunListener` 类型的实现，并发布“启动开始”事件。

这是更靠前的一层启动监听机制。

### 6.2 封装启动参数

```java
ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
```

把命令行参数封装起来，后面环境准备和 Runner 执行都要用。

### 6.3 准备环境

```java
ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
```

这是启动中非常重要的一步，主要包含：

1. 创建 Environment
2. 处理命令行参数
3. 处理 profile
4. 绑定 `spring.main.*` 配置到 `SpringApplication`
5. 发布 `environmentPrepared` 相关事件

你原来的表述里提到“准备变量”，这一步最接近这个意思。

更准确地说，它准备的是：

- 属性源
- profile
- 命令行参数
- 环境对象本身

### 6.4 打印 Banner

```java
Banner printedBanner = printBanner(environment);
```

如果项目有 banner，这里会处理打印。

### 6.5 创建 ApplicationContext

```java
context = createApplicationContext();
```

这里会根据应用类型选择合适的上下文实现。

对于当前项目，通常会创建 Servlet Web 场景下的 `ApplicationContext`。

### 6.6 准备上下文

```java
prepareContext(context, environment, listeners, applicationArguments, printedBanner);
```

这是启动过程中另一个关键大步骤，内部主要做几件事：

1. 把 `environment` 放进 `context`
2. 调用 `postProcessApplicationContext(context)`
3. 调用 `applyInitializers(context)` 执行初始化器
4. 注册一些启动阶段单例
5. 加载主配置源

其中第三步正是本项目 `MyInitializer` 生效的地方。

也就是说：

- 构造器阶段：收集初始化器
- `prepareContext(...)` 阶段：执行初始化器

### 6.7 加载配置源

在 `prepareContext(...)` 里有：

```java
Set<Object> sources = getAllSources();
load(context, sources.toArray(new Object[0]));
```

这里会把 `BlueOceanApplication.class` 这样的主配置类加载进容器。

注意，这一步不是“准备 JAR 包”。

更准确的说法是：

> 基于当前类路径中的依赖和主配置源，开始向容器注册 BeanDefinition 与配置来源。

JAR 包本身并不是在 `run()` 里“准备出来”的，而是在应用启动前就已经位于类路径上了。

`run()` 做的是：

- 使用类加载器和类路径中的内容
- 根据约定扫描和加载配置
- 把这些内容组织进 Spring 容器的启动流程

### 6.8 执行 refresh

```java
refreshContext(context);
```

最终会调用：

```java
applicationContext.refresh();
```

这一步才真正进入大家熟悉的 Spring 容器刷新流程，例如：

1. 准备 BeanFactory
2. 执行 BeanFactoryPostProcessor
3. 注册 BeanPostProcessor
4. 初始化消息源、事件派发器
5. 实例化非懒加载单例 Bean
6. 发布容器刷新完成事件

你的 `MyListener` / `MyListener1` 监听的是 `ContextRefreshedEvent`，
因此会在这个阶段结束后被触发。

---

## 6.10 讲 Spring 容器启动时，几个不能绕过的源码点

如果你要把“Spring 容器到底怎么启动起来”讲清楚，这几个类和字段最好不要跳过。

### A. BeanDefinition：Bean 的“配方”长什么样

核心源码入口：

- `org.springframework.beans.factory.config.BeanDefinition`

源码位置：

- `spring-beans-5.2.15.RELEASE-sources.jar`
- 包路径：`org/springframework/beans/factory/config/BeanDefinition.java`

怎么理解它？

`BeanDefinition` 不是 Bean 对象本身，而是 Bean 的元数据描述。

它关心的是：

- Bean 的类名
- scope
- lazy-init
- dependsOn
- 工厂方法
- 构造参数
- 属性注入信息

教学里可以直接给一句话：

> Spring 在真正 new 出 Bean 之前，先把 Bean 当成一份“配置说明书”来管理，这份说明书就是 BeanDefinition。

你在源码里重点看这些方法就够了：

- `setBeanClassName(...)` / `getBeanClassName()`
- `setScope(...)` / `getScope()`
- `setLazyInit(...)`
- `setDependsOn(...)`
- `setFactoryBeanName(...)`
- `setFactoryMethodName(...)`

### B. beanDefinitionMap：Spring 把 BeanDefinition 放在哪里

核心源码入口：

- `org.springframework.beans.factory.support.DefaultListableBeanFactory`

源码位置：

- `spring-beans-5.2.15.RELEASE-sources.jar`
- 包路径：`org/springframework/beans/factory/support/DefaultListableBeanFactory.java`

你讲课时最值得点出来的字段有两个：

```java
private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
private volatile List<String> beanDefinitionNames = new ArrayList<>(256);
```

怎么理解它们？

- `beanDefinitionMap`：按 beanName -> BeanDefinition 存放元数据
- `beanDefinitionNames`：保存注册顺序，后续很多遍历都要用到它

也就是说：

> Spring 容器在启动早期，首先管理的不是“Bean 实例集合”，而是“BeanDefinition 集合”。

你再往下看这几个方法，就能把“放进去”和“取出来”串起来：

- `registerBeanDefinition(String beanName, BeanDefinition beanDefinition)`
- `getBeanDefinition(String beanName)`
- `getBeanDefinitionNames()`

教学上可以这样讲：

1. 配置类解析、扫描、自动配置导入等动作，先产出 BeanDefinition
2. BeanDefinition 被注册进 `DefaultListableBeanFactory`
3. 后面 refresh 到实例化阶段，Spring 再根据这些定义去创建 Bean

### C. BeanPostProcessor：Bean 创建前后谁在拦截

核心源码入口：

- `org.springframework.beans.factory.config.BeanPostProcessor`

源码位置：

- `spring-beans-5.2.15.RELEASE-sources.jar`
- 包路径：`org/springframework/beans/factory/config/BeanPostProcessor.java`

这个接口讲什么？

它是 Spring 留给“Bean 实例化后再加工”的扩展点。

重点方法只有两个：

```java
postProcessBeforeInitialization(Object bean, String beanName)
postProcessAfterInitialization(Object bean, String beanName)
```

教学里可以这样理解：

- BeanDefinition 解决的是“这个 Bean 应该怎么造”
- BeanPostProcessor 解决的是“这个 Bean 造出来之后，要不要再加工一下”

典型用途包括：

- 给 Bean 做代理
- 处理 `@Autowired`
- 处理 AOP
- 处理各种框架注解带来的增强逻辑

### D. refresh()：容器启动主干流程在哪里

核心源码入口：

- `org.springframework.context.support.AbstractApplicationContext`

源码位置：

- `spring-context-5.2.15.RELEASE-sources.jar`
- 包路径：`org/springframework/context/support/AbstractApplicationContext.java`

最关键的方法就是：

```java
public void refresh()
```

这段方法是讲容器启动时一定要盯住的主干代码。当前版本里，核心顺序大致是：

```java
prepareRefresh();
ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
prepareBeanFactory(beanFactory);
postProcessBeanFactory(beanFactory);
invokeBeanFactoryPostProcessors(beanFactory);
registerBeanPostProcessors(beanFactory);
initMessageSource();
initApplicationEventMulticaster();
onRefresh();
registerListeners();
finishBeanFactoryInitialization(beanFactory);
finishRefresh();
```

这段顺序非常适合拿来做课堂主线。

你可以这样讲：

1. `obtainFreshBeanFactory()`：先拿到一个干净的 BeanFactory
2. `invokeBeanFactoryPostProcessors(...)`：先改 BeanDefinition，再谈实例化
3. `registerBeanPostProcessors(...)`：把创建 Bean 时要用到的后处理器注册进去
4. `finishBeanFactoryInitialization(...)`：开始实例化剩余的非懒加载单例
5. `finishRefresh()`：最后发布容器刷新完成事件

### E. BeanPostProcessor 是在哪里注册进容器启动流程的

如果你想再往下一层讲，可以继续看：

- `org.springframework.context.support.PostProcessorRegistrationDelegate`

它负责处理：

- `invokeBeanFactoryPostProcessors(...)`
- `registerBeanPostProcessors(...)`

也就是说，`AbstractApplicationContext.refresh()` 只是列出流程，
真正的“后处理器按什么顺序找、按什么顺序注册”，是由这个委托类继续完成的。

### F. 这几个点之间的关系

这几个源码点最好不要分散讲，建议串成一条线：

1. 配置类、扫描结果、自动配置等，先转成 `BeanDefinition`
2. `BeanDefinition` 被放进 `DefaultListableBeanFactory.beanDefinitionMap`
3. `refresh()` 过程中先处理 BeanDefinition，再注册 BeanPostProcessor
4. 最后根据 BeanDefinition 去创建 Bean，并在创建前后执行 BeanPostProcessor

这条线一旦讲通，容器启动的骨架就很清楚了。

### 6.9 refresh 之后的处理

```java
afterRefresh(context, applicationArguments);
listeners.started(context);
callRunners(context, applicationArguments);
listeners.running(context);
```

这几步分别可以理解为：

1. `afterRefresh`：留给子类扩展
2. `started`：发布“已经启动”事件
3. `callRunners`：执行 `ApplicationRunner` / `CommandLineRunner`
4. `running`：发布“应用正在运行”事件

---

## 7. 你提到的三句话，建议这样修正

你原来的描述是：

1. 先通过构造器初始化一些监听器和初始化器
2. 再通过 run 方法准备好我们的 JAR 包
3. 最后开始拼接的过程

其中第 1 句基本对，第 2、3 句建议换成更准确的表达。

可以改成：

1. 先在 `SpringApplication` 构造器中收集监听器和初始化器
2. 再在 `run()` 中准备启动环境、创建应用上下文、装载配置源
3. 随后进入 `refresh()`，完成 Bean 容器初始化和应用启动

如果你想讲得更口语化一点，也可以说：

1. 构造器先把“启动需要的扩展角色”找齐
2. `run()` 再把环境、上下文、配置源这些启动材料组织起来
3. 最后通过 `refresh()` 完成容器真正启动

---

## 8. 这个项目到底在讲什么

如果结合现在的代码，我建议把项目定位成：

> 以 Spring Boot 启动流程为主线，同时穿插讲解 SPI 在启动阶段的作用。

也就是：

- 主线：Spring 是怎么启动起来的
- 切面：SPI 是怎么参与这个启动过程的

这样讲最自然，因为：

1. 你项目里既有 `ServiceLoader` 的纯 SPI 示例
2. 也有 `spring.factories` 这种 Spring Boot 启动期扩展点
3. 最终它们都能服务于“启动机制为什么具备扩展性”这个问题

## 9. 推荐教学顺序

如果你要拿这份项目讲课，推荐顺序如下：

### 第一讲：从 main 方法进入

- 看 `BlueOceanApplication`
- 看 `SpringApplication.run(...)`

### 第二讲：先补 SPI 基础

- 什么是 SPI
- 看 `MyService`、`MyServiceImpl1`、`MyServiceImpl2`
- 看 `META-INF/services/...`
- 跑 `SpiDemo`

### 第三讲：再看 Spring 风格 SPI

- 看 `spring.factories`
- 看 `MyInitializer`
- 看 `MyListener`
- 说明 Spring Boot 如何在构造器阶段收集它们

### 第四讲：串起 run 主流程

- 构造器收集扩展点
- `prepareEnvironment`
- `createApplicationContext`
- `prepareContext`
- `refreshContext`
- `callRunners`

### 第五讲：延伸到自动配置

- `refresh` 过程中如何解析配置类
- 自动配置为什么能被加载
- `DeferredImportSelector`、自动配置导入机制在整体中的位置

## 10. 一句话总结

这份项目最适合这样讲：

> 它的主线是 Spring Boot 的启动流程，SPI 是解释“这些扩展点和配置类是如何被发现并接入启动过程”的关键机制。
