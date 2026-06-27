# Spring 启动阅读主线

这份文档只做一件事：

> 带着你按第一步、第二步、第三步，把当前项目里的 Spring Boot 启动流程完整看一遍。

不追求一次把所有细节吃完，只先抓主线。

---

## 先记住版本

当前项目使用的是：

- Spring Boot: `2.3.12.RELEASE`
- Spring Framework: `5.2.15.RELEASE`

所以你后面看源码时，尽量按这个版本找。

源码包位置：

- Boot: `/Users/tianyi/.m2/repository/org/springframework/boot/spring-boot/2.3.12.RELEASE/spring-boot-2.3.12.RELEASE-sources.jar`
- Context: `/Users/tianyi/.m2/repository/org/springframework/spring-context/5.2.15.RELEASE/spring-context-5.2.15.RELEASE-sources.jar`
- Beans: `/Users/tianyi/.m2/repository/org/springframework/spring-beans/5.2.15.RELEASE/spring-beans-5.2.15.RELEASE-sources.jar`

---

## 第一遍只看 6 步

### 第 1 步：从项目启动类开始

先看：

- [BlueOceanApplication.java](/Users/tianyi/Resource/Kuake/spring-demo-blue-ocean/src/main/java/com/blue/ocean/BlueOceanApplication.java)

你只需要盯住这一句：

```java
SpringApplication.run(BlueOceanApplication.class, args);
```

这一句就是整个启动入口。

你先记一句话：

> Spring Boot 启动，本质上就是先创建 `SpringApplication`，再调用它的 `run()`。

也就是：

```java
new SpringApplication(BlueOceanApplication.class).run(args);
```

---

### 第 2 步：看 SpringApplication 构造器

去看：

- `org.springframework.boot.SpringApplication`

先看构造器，不要一上来就看完整个 `run()`。

你主要看这几行：

```java
this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
this.webApplicationType = WebApplicationType.deduceFromClasspath();
setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
this.mainApplicationClass = deduceMainApplicationClass();
```

这一段要得出的结论是：

1. 保存主启动类 `BlueOceanApplication`
2. 判断当前应用是什么类型
3. 提前收集初始化器
4. 提前收集监听器

这一步先不要深究每个方法，只要先建立感觉：

> Spring 还没真正启动容器，就已经在为启动做准备了。

---

### 第 3 步：看为什么它会被判断成 Servlet Web 应用

去看：

- `org.springframework.boot.WebApplicationType`

重点看：

```java
static WebApplicationType deduceFromClasspath()
```

你要记住这里的关键点：

> Spring Boot 不是看你代码里有没有 `Servlet` 关键字，
> 而是看 classpath 里有没有 Servlet Web 应用的标志类。

当前项目引入了：

- `spring-boot-starter-web`

它会带来：

- `spring-webmvc`
- `spring-boot-starter-tomcat`

所以 Boot 最后推断出这是一个 `SERVLET` 应用。

这一步看懂就够了，不用展开太多。

---

### 第 4 步：正式看 run() 主流程

再回到：

- `org.springframework.boot.SpringApplication`

重点看：

```java
public ConfigurableApplicationContext run(String... args)
```

第一遍只看这几个方法名，不用抠细节：

```java
prepareEnvironment(...)
createApplicationContext()
prepareContext(...)
refreshContext(...)
afterRefresh(...)
callRunners(...)
```

你把它理解成 6 句话：

1. 准备环境
2. 创建容器
3. 往容器里塞启动需要的东西
4. 执行 `refresh()`
5. 做 refresh 之后的收尾
6. 调用 Runner

如果你现在只能记住一句，那就是：

> `run()` 不是直接创建 Bean，而是先准备环境和容器，最后才进入 `refresh()`。

---

### 第 5 步：真正看容器启动核心 refresh()

去看：

- `org.springframework.context.support.AbstractApplicationContext`

重点看：

```java
public void refresh()
```

第一遍只看这条主线：

```java
prepareRefresh();
obtainFreshBeanFactory();
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

你先把它压缩成 4 句：

1. 准备容器
2. 处理 BeanDefinition
3. 注册 BeanPostProcessor 和监听器
4. 实例化单例 Bean，最后发布 refresh 完成事件

这一步非常关键。

因为你后面看到的大部分启动细节，最后都会落到这个 `refresh()` 里。

---

### 第 6 步：把本项目自己的代码挂回去

现在再回来看项目中的两个扩展点：

- [MyInitializer.java](/Users/tianyi/Resource/Kuake/spring-demo-blue-ocean/src/main/java/com/blue/ocean/MyInitializer.java)
- [MyListener.java](/Users/tianyi/Resource/Kuake/spring-demo-blue-ocean/src/main/java/com/blue/ocean/listener/MyListener.java)

以及它们的注册文件：

- [spring.factories](/Users/tianyi/Resource/Kuake/spring-demo-blue-ocean/src/main/resources/META-INF/spring.factories)

你只要把它们挂到两个位置上：

### `MyInitializer` 挂在哪里？

挂在：

```java
prepareContext(...)
```

更具体地说，是这里：

```java
applyInitializers(context);
```

意思是：

> 初始化器是在 `refresh()` 之前执行的。

### `MyListener` 挂在哪里？

它监听的是：

```java
ContextRefreshedEvent
```

所以它会在：

```java
finishRefresh();
```

这个阶段附近被触发。

意思是：

> 监听器不是自己随便执行的，而是在 Spring 发布对应事件时被调用。

---

## 第二遍重点看 3 个核心概念

如果第一遍主线你已经顺下来了，第二遍只补这 3 个概念。

### 1. BeanDefinition 是什么

去看：

- `org.springframework.beans.factory.config.BeanDefinition`

记一句话：

> `BeanDefinition` 不是 Bean 本身，而是 Bean 的说明书。

---

### 2. BeanDefinition 放在哪里

去看：

- `org.springframework.beans.factory.support.DefaultListableBeanFactory`

重点看字段：

```java
private final Map<String, BeanDefinition> beanDefinitionMap
private volatile List<String> beanDefinitionNames
```

记一句话：

> Spring 启动早期，先管理的是 BeanDefinition 集合，不是 Bean 实例集合。

---

### 3. BeanPostProcessor 是干什么的

去看：

- `org.springframework.beans.factory.config.BeanPostProcessor`

重点看两个方法：

```java
postProcessBeforeInitialization(...)
postProcessAfterInitialization(...)
```

记一句话：

> Bean 创建出来后，Spring 还可以再加工它，这个加工入口就是 BeanPostProcessor。

---

## 最后把整条主线压成一句话

你如果要自己复述 Spring Boot 启动流程，可以直接按下面这段说：

### 第一句

项目从 `SpringApplication.run(...)` 开始。

### 第二句

`SpringApplication` 构造器会先保存主启动类、判断应用类型、加载初始化器和监听器。

### 第三句

`run()` 会先准备环境、创建 ApplicationContext、执行初始化器，然后进入 `refresh()`。

### 第四句

`refresh()` 会准备 BeanFactory、处理 BeanDefinition、注册 BeanPostProcessor、实例化单例 Bean，并在最后发布容器刷新完成事件。

### 第五句

项目里的 `MyInitializer` 是在 `refresh()` 之前参与启动的，`MyListener` 是在 `ContextRefreshedEvent` 发布时触发的。

---

## 你接下来最推荐的阅读顺序

如果你现在准备重新读一遍，我建议就按这个顺序：

1. [BlueOceanApplication.java](/Users/tianyi/Resource/Kuake/spring-demo-blue-ocean/src/main/java/com/blue/ocean/BlueOceanApplication.java)
2. `SpringApplication` 构造器
3. `WebApplicationType.deduceFromClasspath()`
4. `SpringApplication.run()`
5. `AbstractApplicationContext.refresh()`
6. [spring.factories](/Users/tianyi/Resource/Kuake/spring-demo-blue-ocean/src/main/resources/META-INF/spring.factories)
7. [MyInitializer.java](/Users/tianyi/Resource/Kuake/spring-demo-blue-ocean/src/main/java/com/blue/ocean/MyInitializer.java)
8. [MyListener.java](/Users/tianyi/Resource/Kuake/spring-demo-blue-ocean/src/main/java/com/blue/ocean/listener/MyListener.java)
9. `BeanDefinition`
10. `DefaultListableBeanFactory`
11. `BeanPostProcessor`

---

## 一句话提醒

第一次看 Spring 启动，最容易迷路的原因是一下子想看懂所有细节。

更好的方式是：

> 先只盯住“入口 -> run -> refresh -> 扩展点挂载位置”这条主线，
> 第二遍再补 BeanDefinition、BeanPostProcessor、自动配置这些细节。
