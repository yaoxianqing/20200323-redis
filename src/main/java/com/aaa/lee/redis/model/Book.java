package com.aaa.lee.redis.model;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Accessors(chain = true) // 链条调用
public class Book implements Serializable {
    private Long id;

    private String bookName;

    private Double bookPrice;

}