package com.blue.ocean.spi;

// 这是标准 Java SPI 里的“服务接口”。
// SPI 的基本角色分成三部分：
// 1. 调用方依赖的接口：MyService
// 2. 接口的多个实现：MyServiceImpl1 / MyServiceImpl2
// 3. 配置文件：META-INF/services/com.blue.ocean.spi.MyService
public interface MyService {
    void say();
}
