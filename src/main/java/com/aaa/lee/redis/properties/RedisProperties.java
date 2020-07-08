package com.aaa.lee.redis.properties;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Company AAA软件教育
 * @Author Seven Lee
 * @Date Create in 2020/3/23 15:24
 * @Description
 *      读取application.properties文件中的属性
 *      需要把这个类标识为springboot的一个组件，也就是说当项目启动的时候会自动去加载这个类
 *      不要用@Configuration，因为并不是一个配置类(配置类中都有@Bean注解)
 *
 *      prefix:前缀，统一获取配置文件中的前缀
 *      ipAddr--->一定要和application.properties中的ip-addr一模一样(除了写法模式不一样
 *      (java使用的是驼峰，properties并不需要))
 *      prefix+属性--->spring.redis.ipAddr(java文件中)--->
 *      映射了application.properties文件中的spring.redis.ip-addr
 *
 *      所有的实体类中以后都不允许再使用基本类型，必须使用包装类型:
 *          作用一:防止空指针
 *          作用二:防止指令重排(数据的原子性)(细讲)
 **/
@Component
@ConfigurationProperties(prefix = "spring.redis")
@Data// 相当于getter和setter方法
@AllArgsConstructor// 全部属性的构造方法
@NoArgsConstructor// 默认的无参构造方法
@ToString// 重写toString()--->其实@Data已经包含了
@EqualsAndHashCode// 重写了HashCode和equals方法
public class RedisProperties implements Serializable {

    @NotNull
    private String ipAddr;
    @NotNull
    private Integer maxAttempts;
    @NotNull
    private Integer commandTimeout;

}
