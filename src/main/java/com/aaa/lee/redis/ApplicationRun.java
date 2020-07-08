package com.aaa.lee.redis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Company AAA软件教育
 * @Author Seven Lee
 * @Date Create in 2020/3/23 14:33
 * @Description
 *      springboot三板斧
 *      1.导入jar包
 *      2.编写application.properties文件--->就会被类所用
 *          使用java类来获取properties文件内容(如何获取properties中的数据信息，通过key来获取value)
 *              name=zhangsan
 *              @Value("${name}")
 *              private String name;--->zhangsan
 *      3.编写配置类
 **/
@SpringBootApplication
@MapperScan("com.aaa.lee.redis.mapper") // 以扫描的形式扫描下面的所有mapper接口，代替@Mapper注解 和扫描controller,扫描service一样
public class ApplicationRun {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationRun.class, args);
    }

}
