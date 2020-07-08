package com.aaa.lee.redis.service;

import com.aaa.lee.redis.utils.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.JedisCluster;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.aaa.lee.redis.staticstatus.StaticProperties.*;

/**
 * @Company AAA软件教育
 * @Author Seven Lee
 * @Date Create in 2020/3/23 16:01
 * @Description
 *      redis的业务实现类(以后我不会写service接口)
 *
 *      set:存数据-->返回值如果成功就是"OK"
 *
 *      get:取数据
 *
 *      del:删除数据
 *
 *      expire:为数据设置失效时间
 *
 *      以后到工作中写的也是业务逻辑--->下面的所有代码必须要会！！
 *
 *      如果需要存入redis数据库，首先应该把需要缓存的数据从mysql中查询出来，然后通过java代码存入到redis中
 *      --->从mysql中所查询出来的可能就是对象类型，可能是List...
 *
 **/
@Service
public class RedisService<T> {

    @Autowired
    private JedisCluster jedisCluster;

    // spring提供的key的序列化器，作用把key进行序列化
    private RedisSerializer keySerializer = null;

    /**
     * @author Seven Lee
     * @description
     *      向redis中保存数据
     *      key:redis的key
     *      value:所要保存的数据
     *      redis中默认所实现的是覆盖
     *      张三和李四是两个开发人员
     *          张三在写book信息管理
     *          李四在写user信息管理
     *          张三-->redis--->redisKey
     *          李四不知道--->redis--->redisKey--->直接把张三的数据给覆盖了
     *          解决方案:
     *              在存入的时候先检测key是否存在，如果存在则不存数据了，如果不存在才会让你存数据
     *       张三自己操作redis
     *          张三写book信息管理
     *          张三第一次就从mysql中把数据查询出来然后存入到redis中--->以后只要查询图书信息都不会再从mysql中查，只会从redis中
     *          可能出现问题(脏数据问题):
     *              突然mysql中新增一条图书信息(100条--->101条)，但是redis中仍然是100条
     *          张三需要覆盖原有的数据，使用新的数据
     *
     *      nxxx:
     *          只有两个固定值(架构规定，必须要传这两个固定值)
     *          "nx":如果redis中没有这个keu，才会去存，有这个key不再存数据
     *          "xx":redis中有这个key才能存，没有key则不能存
     *
     *      在实际开发中会有这种可能:
     *          当向redis存入的数据就必须要设置失效时间(秒杀，活动商品(xx小时之后价格恢复))
     *      expx:
     *          只有两个固定值
     *          ex:失效时间的单位为秒
     *          px:失效时间的单位为毫秒
     *      time:具体的失效时间
     *
     *      硬编码是固定写死在程序员中的数据"",数字...
     *      最早学习数据源连接的时候--->class.forName("mysql...");
     *
     * @param [key, value, nxxx, expx, time]
     * @date 2020/3/23
     * @return java.lang.String
     * @throws
    **/
    public String set(String key, T value, String nxxx, String expx, Integer time) {
        // 判断是否需要设置失效时间
        if(null != time && 0 < time &&
                (NX.equals(nxxx) || XX.equals(nxxx)) &&
                (EX.equals(expx) || PX.equals(expx))) {
            // 需要设置失效时间
            return jedisCluster.set(key, JSONUtil.toJsonString(value), nxxx, expx, time);
        } else {
            // 说明不需要设置失效时间
            // 需要再次判断--->是否nx或者xx
            if(NX.equals(nxxx)) {
                // getIntegerReply-->状态码(成功200，找不到400，报错500)--->如果找不到也可能会使程序受影响
                // 成功状态码:2xx 找不到状态码:4xx 异常状态码:5xx
                // 404--->可以对应多个异常(一对多的关系)
                // 为了保证返回给程序员的是一个真实有效的结果，所以返回的是受影响的行数
                return String.valueOf(jedisCluster.setnx(key, JSONUtil.toJsonString(value)));
                // 下面不能直接使用else，因为防止客户端传递过来的数据不是xx
                // 如果需要使用到其他工具类，直接从网上搜JSONUtil,StringUtil...
            } else if (XX.equals(nxxx)){
                return jedisCluster.set(key, JSONUtil.toJsonString(value));
            }
        }
        return NO;
    }

    /**
     * @author Seven Lee
     * @description
     *      从redis中获取数据(单个对象)--->包含基本类型
     * @param [key]
     * @date 2020/3/23
     * @return T
     * @throws
    **/
    public T getObject(String key) {
        String redisValue = jedisCluster.get(key);
        return (T) JSONUtil.toObject(redisValue, Object.class);
    }

    /**
     * @author Seven Lee
     * @description
     *      从redis中获取数据(集合数据)
     * @param [key]
     * @date 2020/3/23
     * @return java.util.List<T>
     * @throws
    **/
    public List<T> getList(String key) {
        String redisValue = jedisCluster.get(key);
        return (List<T>) JSONUtil.toList(redisValue, Object.class);
    }

    /**
     * @author Seven Lee
     * @description
     *      架构的好不好，就直接意味着程序员开发是否方便
     *      也就是说程序员使用这套架构开发使用越方便，越简单，架构底层代码就绝对越复杂
     *      在实际开发中程序员有一个习惯:
     *          为了防止redis的key冲突导致数据覆盖，会把id作为redis的key去传
     *          id有数字类型--->int/bigint--->自增(java代码中的属性对应的是int/long)
     *          id有可能是uuid之后，其他的都必须要转换String
     * @param [key]
     * @date 2020/3/23
     * @return java.lang.Long
     * @throws
    **/
    public Long delOne(Object key) {
        // 1.把key转换成字节数据--->任何程序语言里面，字节是最通用的
        // 如果看不懂，你们可以用if判断，最好使用jdk8的新特性写(断言--->判断语言--->专门去做判断)
        // 断言的业务是需要自己去写的，我这借助的是spring提供的工具类，断言的用法就是把判断统一化
        // 断言需要咱们自己定义，实现所有的判断统一处理(把所有的判断封装到一个方法中用)

        /*byte[] k1;
                if(this.keySerializer == null && key instanceof byte[]) {
                    // 直接进行强转即可
                    k1 = (byte[]) key;
                } else {
                    // 使用序列化器进行序列化，相当于实现了序列化接口
                    k1 = this.keySerializer.serialize(key);
                }*/
        return jedisCluster.del(obejct2ByteAarry(key));
    }

    /**
     * @author Seven Lee
     * @description
     *      批量删除redis的数据
     * @param [keys]
     * @date 2020/3/23
     * @return java.lang.Long
     * @throws
    **/
    public Long delMany(Collection<T> keys) {
        // 1.严谨判断集合的长度，如果为0，就是逗比了
        if(CollectionUtils.isEmpty(keys)) {
            return 0L;
        } else {
            // 因为JedisCluster中提供了可变长度的参数，所以咱们就可以使用这种模式来进行批量删除
            // 为了实现通用，还得必须转换成字节--->因为可变的，所以最好的方案就是使用二维数组来进行批量删除
            byte[][] keyBytes = this.collection2ByteArray(keys);
            return jedisCluster.del(keyBytes);
        }
    }

    // TODO 留作业--->自己补充完整失效时间(必须要实现通用)

    /**
     * @author Seven Lee
     * @description
     *      把Object对象类型转换为字节数组
     * @param [key]
     * @date 2020/3/23
     * @return byte[]
     * @throws
    **/
    private byte[] obejct2ByteAarry(Object key) {
        Assert.notNull(key,"this key is required, you can't send null!");// spring工具类--->如果为null直接抛异常，如果不为null直接往下走
        // 因为要转换字节数组，需要进行把对象序列化(让实体类必须要实现序列化接口的原因)
        return this.keySerializer == null && key instanceof byte[] ? (byte[]) key : this.keySerializer.serialize(key);
    }

    /**
     * @author Seven Lee
     * @description
     *      把集合转换为二维字节数组
     * @param [keys]
     * @date 2020/3/23
     * @return byte[][]
     * @throws
    **/
    private byte[][] collection2ByteArray(Collection<T> keys) {
        byte[][] bytes = new byte[keys.size()][];// 定义一个长度为集合长度的二维数组
        int i = 0;// 二维数组的下标，来进行存储数据用
        Object key;// 因为keys是一个集合，泛型对象是Object--->所以需要循环这个集合，把集合中的所有元素都要序列化
        // 使用迭代器去循环keys的集合
        /**
         * Iterator var4 = keys.iterator()--->因为Collection不一定是List，有可能是Map
         * for(Iterator it : keys.iterator()) {
         *      if(it.hasNext()) {
         *          key = it.next();
         *          把这个key进行序列化--->把Object对象序列化成字节数组
         *          obejct2ByteAarry(key);
         *      }
         * }
         */
        for (Iterator var4 = keys.iterator(); var4.hasNext(); bytes[i++] = obejct2ByteAarry(key)) {
            key = var4.next();// 集合中的第一个元素
        }
        return bytes;
    }

}
