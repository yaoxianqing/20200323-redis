package com.aaa.lee.redis.controller;

import com.aaa.lee.redis.service.BookService;
import com.aaa.lee.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Company AAA软件教育
 * @Author Seven Lee
 * @Date Create in 2020/3/25 15:59
 * @Description
 **/
@RestController
public class BookController {

    @Autowired
    private BookService bookService;
    @Autowired
    private RedisService redisService;

    @GetMapping("/allBooks")
    public Object selectAllBooks    () {
        return bookService.selectAllBooks(redisService);
    }

}
