package com.aaa.lee.redis.config;

import com.aaa.lee.redis.properties.RedisProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * @Company AAA软件教育
 * @Author Seven Lee
 * @Date Create in 2020/3/23 15:43
 * @Description
 *      redis的自动装配类
 **/
@Configuration
public class RedisAutoConfiguration {

    @Autowired
    private RedisProperties redisProperties;

    /**
     * @author Seven Lee
     * @description
     *      redis的自动装配，获取JedisCluster对象(该对象的作用就是来让java对redis做增删改查)
     *      @Bean注解就相当于之前<bean></bean>标签
     *      作用就是spring通过IOC来反向生成对象用的--->只要通过bean所生成出来的所有对象都是单例
     *
     *      JedisCluster---><bean class="redis.clients.jedis.JedisCluster"></bean>
     *      class属性的作用就是具体所生成出来的对象的类型是谁
     *
     *      HostAndPort:就是redis所给java提供的连接自己的一个类
     * @param []
     * @date 2020/3/23
     * @return redis.clients.jedis.JedisCluster
     * @throws
    **/
    @Bean
    public JedisCluster getJedisCluster() {
        Set<HostAndPort> hostAndPortSet = new HashSet<HostAndPort>();
        // 1.先连接上远程redis服务器(ip地址和端口号)
        String ipAddr = redisProperties.getIpAddr();// 192.168.23.166:6380....
        // 2.分割ipAddr，以","进行分割
        String[] ipsAndPorts = ipAddr.split(",");// ["192.168.23.166:6380","192.168.23.166:6381"...]
        // 3.循环获取每一台服务器的ip和端口号
        for(String ipPort : ipsAndPorts) {
            // 第一次循环(192.168.23.166:6380)
            // 第二次循环(192.168.23.166:6381)
            // 第三次循环(192.168.23.166:6382)
            // 4.再次分割，以":"为分割符
            String[] ipAndPort = ipPort.split(":");//["192.168.23.166", "6380"](这个数组的长度是固定的)
            HostAndPort hostAndPort = new HostAndPort(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
            hostAndPortSet.add(hostAndPort);
        }
        // hostAndPortSet集合中已经有了这6台服务器的ip地址和端口号
        // 5.创建jedisCluster
        return new JedisCluster(hostAndPortSet, redisProperties.getCommandTimeout(), redisProperties.getMaxAttempts());
    }

}
