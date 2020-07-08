package com.aaa.lee.redis.mapper;

import com.aaa.lee.redis.model.Book;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

public interface BookMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Book record);

    Book selectByPrimaryKey(Long id);

    List<Book> selectAll();

    int updateByPrimaryKey(Book record);
}