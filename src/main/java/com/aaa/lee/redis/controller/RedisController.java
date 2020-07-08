package com.aaa.lee.redis.controller;

import com.aaa.lee.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Company AAA软件教育
 * @Author Seven Lee
 * @Date Create in 2020/3/23 17:56
 * @Description
 **/
@RestController
public class RedisController {

    @Autowired
    private RedisService redisService;

    /**
     * @author Seven Lee
     * @description
     *      测试redis
     * @param []
     * @date 2020/3/23
     * @return java.lang.Object
     * @throws
    **/
    @GetMapping("/setData")
    public Object setRedis() {
        return redisService.set("book_key", "123456", "nx", "ex", 100000);
    }

}
