package com.blue.ocean.spi;

// SPI 实现类 1。
// 只要类名写进 META-INF/services/com.blue.ocean.spi.MyService，
// ServiceLoader.load(MyService.class) 就有机会把它加载出来。
public class MyServiceImpl1 implements MyService{
    @Override
    public void say() {
        System.out.println("MyServiceImpl1...say");
    }
}
