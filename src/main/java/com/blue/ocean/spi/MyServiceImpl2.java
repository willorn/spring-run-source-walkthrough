package com.blue.ocean.spi;

// SPI 实现类 2。
// 这个例子说明 SPI 天然支持“同一个接口有多个实现，再由框架/调用方按约定发现它们”。
public class MyServiceImpl2 implements MyService{
    @Override
    public void say() {
        System.out.println("MyServiceImpl2...say");
    }
}
