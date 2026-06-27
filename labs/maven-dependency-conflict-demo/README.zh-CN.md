# Maven 依赖冲突实验

这个实验专门演示两个 Maven 依赖选择规则：

1. 短路径优先
2. 声明顺序优先

实验位置：

- [labs/maven-dependency-conflict-demo/pom.xml](/Users/tianyi/Resource/Kuake/spring-demo-blue-ocean/labs/maven-dependency-conflict-demo/pom.xml)

## 先理解结论

如果项目里同时出现两个不同版本的同一个依赖，Maven 不会把两个版本都放进最终 classpath，而是只选一个。

它的基本规则是：

1. 谁离当前项目更近，选谁
2. 如果距离一样，谁先声明，选谁

这里的“近”指的是依赖路径更短。

例如：

```text
当前项目 -> slf4j-api:2.0.9
当前项目 -> lib-a -> slf4j-api:1.7.25
```

第一条路径只有 1 跳，第二条路径有 2 跳，所以 Maven 选 `2.0.9`。

再例如：

```text
当前项目 -> lib-a -> slf4j-api:1.7.25
当前项目 -> lib-b -> slf4j-api:1.7.36
```

这两条路径长度一样，都是 2 跳，所以 Maven 看当前 `pom.xml` 中 `lib-a` 和 `lib-b` 的声明顺序。

## 这个实验怎么设计的

### `lib-a`

传递引入：

```text
org.slf4j:slf4j-api:1.7.25
```

### `lib-b`

传递引入：

```text
org.slf4j:slf4j-api:1.7.36
```

### `app-short-path`

同时依赖：

```text
lib-a
slf4j-api:2.0.9
```

用途：验证“短路径优先”。

### `app-declaration-order-a-first`

依赖顺序：

```text
lib-a
lib-b
```

用途：验证“同层级时，先声明的胜出”。

### `app-declaration-order-b-first`

依赖顺序：

```text
lib-b
lib-a
```

用途：验证把顺序反过来后，结果也会反过来。

## 你要执行的命令

推荐直接用 `-f` 指定实验项目的父 `pom.xml`，这样无论你当前终端在哪个目录执行都可以。

```bash
mvn -f /Users/tianyi/Resource/Kuake/spring-demo-blue-ocean/labs/maven-dependency-conflict-demo/pom.xml \
    -pl app-short-path -am dependency:tree -Dverbose -Dincludes=org.slf4j:slf4j-api
```

如果你已经先 `cd` 到实验目录，那么也可以写短一些：

```bash
cd /Users/tianyi/Resource/Kuake/spring-demo-blue-ocean/labs/maven-dependency-conflict-demo
```

### 1. 看短路径优先

```bash
mvn -pl app-short-path -am dependency:tree -Dverbose -Dincludes=org.slf4j:slf4j-api
```

你会看到类似结果：

```text
+- demo.maven.conflict:lib-a:jar:1.0-SNAPSHOT:compile
|  \- (org.slf4j:slf4j-api:jar:1.7.25:compile - omitted for conflict with 2.0.9)
\- org.slf4j:slf4j-api:jar:2.0.9:compile
```

关键看这一句：

```text
omitted for conflict with 2.0.9
```

意思是：`1.7.25` 被丢弃了，因为 Maven 选中了更近的 `2.0.9`。

### 2. 看声明顺序优先：a 在前

```bash
mvn -pl app-declaration-order-a-first -am dependency:tree -Dverbose -Dincludes=org.slf4j:slf4j-api
```

你会看到类似结果：

```text
+- demo.maven.conflict:lib-a:jar:1.0-SNAPSHOT:compile
|  \- org.slf4j:slf4j-api:jar:1.7.25:compile
\- demo.maven.conflict:lib-b:jar:1.0-SNAPSHOT:compile
   \- (org.slf4j:slf4j-api:jar:1.7.36:compile - omitted for conflict with 1.7.25)
```

说明最终选中的是 `1.7.25`，因为 `lib-a` 先声明。

### 3. 看声明顺序优先：b 在前

```bash
mvn -pl app-declaration-order-b-first -am dependency:tree -Dverbose -Dincludes=org.slf4j:slf4j-api
```

## 你刚才那个报错为什么会出现

报错原文是：

```text
Could not find the selected project in the reactor: app-short-path
```

这句话的意思不是 `app-short-path` 不存在，而是：

1. 你当前执行 `mvn` 时，Maven 读取的不是实验目录下的那个聚合 `pom.xml`
2. 所以当前 reactor 里根本没有 `app-short-path` 这个 module
3. 这时你再写 `-pl app-short-path`，Maven 就会报错

这里的 reactor 可以简单理解成“本次 Maven 构建看到的那一组模块”。

你这个仓库根目录的 [pom.xml](/Users/tianyi/Resource/Kuake/spring-demo-blue-ocean/pom.xml) 不是一个多模块聚合工程，它没有：

```xml
<modules>
    <module>app-short-path</module>
</modules>
```

但是实验目录下的 [labs/maven-dependency-conflict-demo/pom.xml](/Users/tianyi/Resource/Kuake/spring-demo-blue-ocean/labs/maven-dependency-conflict-demo/pom.xml) 才有这些 module：

```xml
<module>lib-a</module>
<module>lib-b</module>
<module>app-short-path</module>
<module>app-declaration-order-a-first</module>
<module>app-declaration-order-b-first</module>
```

所以：

1. 如果你在仓库根目录直接执行 `mvn -pl app-short-path ...`
2. Maven 默认会读取根目录的 `pom.xml`
3. 根目录这个 reactor 里没有 `app-short-path`
4. 就报你看到的错误

## 那几行 `Downloading from alimaven` 是什么

那几行不是报错根因，只是 Maven 在做仓库校验。

例如：

```text
Artifact ... is present in the local repository, but cached from a remote repository ID that is unavailable in current build context
```

意思是：

1. 这个依赖你本地仓库里已经有了
2. 但它当初是从另一个远程仓库配置下载下来的
3. 当前这次构建使用的远程仓库配置不完全一样
4. Maven 就会重新确认一下现在这个仓库还能不能下载到它

所以它会继续打印：

```text
Downloading from alimaven ...
Downloaded from alimaven ...
```

这和 `Could not find the selected project in the reactor` 不是一回事。

前者是仓库校验日志，后者才是命令上下文错误。

你会看到类似结果：

```text
+- demo.maven.conflict:lib-b:jar:1.0-SNAPSHOT:compile
|  \- org.slf4j:slf4j-api:jar:1.7.36:compile
\- demo.maven.conflict:lib-a:jar:1.0-SNAPSHOT:compile
   \- (org.slf4j:slf4j-api:jar:1.7.25:compile - omitted for conflict with 1.7.36)
```

说明最终选中的是 `1.7.36`，因为 `lib-b` 先声明。

## 为什么线上容易报 `NoSuchMethodError` 或 `ClassNotFoundException`

因为编译时和运行时你“以为”用的是 A 版本，但 Maven 最终实际放进 classpath 的可能是 B 版本。

典型场景：

1. 你代码里调用了某个新版本才有的方法
2. 编译时能过，因为编译环境拿到了新版本
3. 运行时 classpath 实际被旧版本占了
4. JVM 在旧版本类里找不到这个方法
5. 就报 `NoSuchMethodError`

`ClassNotFoundException` 也类似，本质上也是最终运行时 classpath 和你的预期不一致。

## 真项目里怎么排查

### 先看完整依赖树

```bash
mvn dependency:tree
```

作用：先看项目到底从哪些链路引入了依赖。

### 再只盯一个冲突包

```bash
mvn dependency:tree -Dincludes=org.slf4j:slf4j-api
```

作用：把视线缩小到一个具体依赖，不然完整树太大。

### 再打开详细模式

```bash
mvn dependency:tree -Dverbose -Dincludes=org.slf4j:slf4j-api
```

作用：显示被省略的节点，以及冲突原因。最关键的是看：

```text
omitted for conflict with ...
```

### 如果树很大，输出到文件再搜

```bash
mvn dependency:tree -Dverbose > tree.txt
rg "slf4j-api|omitted for conflict|NoSuchMethodError|ClassNotFoundException" tree.txt
```

重点不是把整棵树从头看到尾，而是定位：

1. 这个包被谁带进来的
2. 一共有几个版本
3. 最后 Maven 选了哪个
4. 哪些版本被省略了

## 查出来以后怎么修

常见修法只有 3 种：

1. 在当前项目直接显式指定你要的版本
2. 用 `<dependencyManagement>` 在父项目统一版本
3. 对错误来源加 `<exclusions>`，排除不想要的传递依赖

如果你愿意，我下一步可以继续直接在你这个 Spring Boot 项目的主 `pom.xml` 里，专门再做一版“真实业务项目风格”的依赖冲突示例，比如用 `spring-boot-starter-web`、`logback`、`slf4j-api`、`commons-logging` 这些更接近日常开发的组合来演示。 
