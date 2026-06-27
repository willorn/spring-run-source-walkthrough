package com.blue.ocean.spi;

import java.util.ServiceLoader;

public class SpiDemo {
    public static void main(String[] args) {
        // 这是标准 Java SPI 的使用入口。
        //
        // ServiceLoader 会去 classpath 下找：
        // META-INF/services/com.blue.ocean.spi.MyService
        //
        // 文件里每一行都是一个实现类的全限定名。
        // 找到后，JDK 会把这些实现类实例化，再交给我们遍历使用。
        ServiceLoader<MyService> services = ServiceLoader.load(MyService.class);

        // 这里没有手动 new MyServiceImpl1() / new MyServiceImpl2()，
        // 这正是 SPI 的核心价值：
        // 调用方只依赖接口，具体实现由配置文件和类路径决定。
        services.forEach(myService -> myService.say());


        // ServiceLoader<MyService> loader1 = ServiceLoader.load(MyService.class);
        // ServiceLoader<MyService> loader2 = ServiceLoader.load(MyService.class);
        //
        // System.out.println(loader1.equals(loader2));
        //
        // MyService a1 = loader1.iterator().next();
        // // loader1.reload();
        // MyService a2 = loader1.iterator().next();
        // MyService b1 = loader2.iterator().next();
        //
        // System.out.println(a1 == a2);
        // System.out.println(a1 == b1);
        // System.out.println(a1.getClass());
        // System.out.println(a2.getClass());
        // System.out.println(b1.getClass());

    //     ServiceLoader<MyService> services = ServiceLoader.load(MyService.class);
        // services.forEach(myService -> myService.say());
        //
        //  -->
        //
        // List<MyService> list = new ArrayList<>();
        //
        // Class<?> c1 = Class.forName("com.blue.ocean.spi.MyServiceImpl1");
        // list.add((MyService) c1.getDeclaredConstructor().newInstance());
        //
        // Class<?> c2 = Class.forName("com.blue.ocean.spi.MyServiceImpl2");
        // list.add((MyService) c2.getDeclaredConstructor().newInstance());
        //
        // for (MyService myService : list) {
        //     myService.say();
        // }
    }
}
