package com.aaa.lee.redis.service;

import com.aaa.lee.redis.mapper.BookMapper;
import com.aaa.lee.redis.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aaa.lee.redis.staticstatus.StaticProperties.*;
import static com.aaa.lee.redis.statusenum.StatusEnum.*;

/**
 * @Company AAA软件教育
 * @Author Seven Lee
 * @Date Create in 2020/3/25 15:40
 * @Description
 **/
@Service
public class BookService {

    @Value(BOOK_KEY)
    private String bookKey;

    @Autowired
    private BookMapper bookMapper;

    /**
     * @author Seven Lee
     * @description
     *      查询所有的图书信息
     *      图书信息是需要存入缓存中的，也就是说图书信息需要从缓存中查询
     *      这就是今天非常非常非常非常重点的内容了(你们到公司之后写的就是业务)
     *          BookMapper{
     *              List<Book> selectAllBooks();
     *          }
     *
     *          BookService{
     *              public List<Book> selectAllBooks() {
     *                  return bookMapper.selectAllBooks();
     *              }
     *          }
     *
     *          BookController{
     *              @PostMapping("/all")
     *              public List<Book> selectAllBooks(){
     *                  return bookService.selectAllBooks();
     *              }
     *          }
     *
     *          Service层是什么层？
     *              业务层，业务层是来处理业务业务逻辑的
     *              controller中什么都不做，只做return
     *
     *          controller层叫做控制层--->领导(做控制的，只负责跳转)
     *              --->service干活
     *
     *          !!! 实际开发工作中必须遵循的原则:所有的业务都必须要写在service层，controller中以后不允许出现和业务相关的代码 !!!
     *
     *          事务穿线:
     *              事务在service层开启--->bookService开启业务了(1.read only 只读业务(负责查询) 2.read write 读写业务(负责增删改))
     *                  @Autowried
     *                  private UserService userService;
     *                  相当于在bookService中开启了user的事务和book的事务
     *                  当你去存数据的时候--->先存用户信息--->再存图书信息
     *                  --->抛出异常--->无法把user转换成book对象
     *
     * @param []
     * @date 2020/3/25
     * @return java.lang.Object
     * @throws
    **/
    /*public Object selectAllBooks(RedisService redisService) {
        // 1.从mysql数据库中把图书信息查询出来
        List<Book> bookList = bookMapper.selectAll();// 报错？？？连接超时，数据库有问题...
        if(bookList.size() > 0) {
            // 说明数据库中有数据
            // 2.把从mysql查询出来的图书信息存入到redis缓存中
            // 需要把数据存入到redis中
            // 在这也抛异常，找不到服务器？？？
            String setResult = redisService.set("book_key", bookList, "nx", "px", 100000);
            // 3.判断
            // toUpperCase()就是把所有的字母全部转换成大写
            if("OK".equals(setResult.toUpperCase())) {
                return "OK";
            }
        }
        return "NO";
    }*/

    /**
     * @author Seven Lee
     * @description
     *      我是一个严谨的男人！！！
     *      以后写的项目全部都是前后端分离的(编写接口文档)
     *          到公司之后你的接口文档写的好，非常受人待见
     *          前端掉后端的controller数据，必须要规定好格式以及参数需要传什么
     *      公司使用的全部都是ajax，绝对没有同步了，全是异步请求-->发送过来的，和返回回去的都是json数据
     *      json数据格式:
     *          {"key":"value"....}--->json数据转换到java代码中Map最好用(因为json和map一模一样)
     *      到公司之后你会经常看到，返回的都是Map，有些公司接收前端发送的数据也是Map
     *
     *      业务是这样的---->如果redis有则从redis取，如果redis没有则从mysql中查然后再存入到redis中
     *      TODO 现在有问题--->如果redis中有数据，并且redis中的数据和mysql中的数据不一致
     *          redis中是脏数据
     *
     *
     * @param [redisService]
     * @date 2020/3/25
     * @return java.lang.Object
     * @throws
    **/
    public Map<String, Object> selectAllBooks(RedisService redisService) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        // 1.为了防止每一次都查询数据库，无论缓存中是否有数据，我第一步永远查缓存
        // 以后不允许抛出Exception(这个异常太大了，是所有异常的父类，定位不准确(IOException...))
        // 不知道抛说明异常，自己百度(redis需要抛出什么异常)
        List bookRedisValue = null;
        try {
            bookRedisValue = redisService.getList(bookKey);// 这一步就有可能抛异常(redis无法连接)
        } catch (Exception e) {
            // catch中是需要写具体代码的，不要走到这打印堆栈信息就完事了
            // 有可能是网络波动，几秒之后会恢复
            // 再从redis中查一次
            bookRedisValue = redisService.getList(bookKey);
            if(null == bookRedisValue || bookRedisValue.size() <= 0) {
                // 真的说明redis抛异常了
                // 有一句话叫做服务器正在维护，请稍后重试！
                // 模拟http，定义状态码(200叫成功，404叫找不到，500抛异常)
                // 在http中，只要是2开头的都是成功(200)，4xx都是找不到，5开头都抛异常
                // 自定义的返回状态码一定不要和http默认的状态码重复(500)
                /**
                 * success: function(data){
                 *     if(data.code == 501) {
                 *          data.msg;
                 *          window.location = "sys-error.html";
                 *     }
                 * }
                 */
                resultMap.put(CODE, FAILED_SYSTEM_EXCEPTION.getCode());
                resultMap.put(MSG, FAILED_SYSTEM_EXCEPTION.getMsg());
            } else {
                // 说明确实是因为网络的一些原因，现在恢复了，redis又正常运转了
                resultMap.put(CODE, SUCCESS_INTNET_EXCEPTION.getCode());
                resultMap.put(MSG, SUCCESS_INTNET_EXCEPTION.getMsg());
                resultMap.put(DATA, bookRedisValue);
            }
            e.printStackTrace();
            return resultMap;
        }

        // 2.判断从redis中取出的数据是否为null/长度是否为0
        if(bookRedisValue.size() < 0 || null == bookRedisValue) {
            // 说明redis中根本就没有数据，需要从mysql中查询数据
            List<Book> bookList = null;
            try {
                // 3.从mysql中查询数据
                bookList = bookMapper.selectAll();// 这一行也有可能抛异常(数据库连接超时，数据库无法连接，找不到表，找不到库...)
            } catch (Exception e){
                // 数据库如果抛异常了，可以实现尝试重连(尝试重连三次)
                for (int i = 0; i < 3; i++) {
                    bookList = bookMapper.selectAll();
                    // 在尝试重新连接中，有可能数据库恢复了，我就查询到数据了
                    if(bookList.size() > 0) {
                        // 数据库又重新连上了
                        resultMap.put(CODE, SUCCESS_INTNET_EXCEPTION.getCode());
                        resultMap.put(MSG, SUCCESS_INTNET_EXCEPTION.getMsg());
                        resultMap.put(DATA, bookList);
                        // 别再去存redis了，因为catch就证明程序已经有异常，只是补救方案
                        return resultMap;
                    }
                }
                // 说明重新连接了三次，还是没有连接上数据库(数据库确实有问题)
                resultMap.put(CODE, FAILED_SYSTEM_EXCEPTION.getCode());
                resultMap.put(MSG, FAILED_SYSTEM_EXCEPTION.getMsg());
                e.printStackTrace();
                return resultMap;
            }
            // 4.判断mysql中是否有数据
            if(bookList.size() > 0) {
                // 说明mysql中有数据
                // 5.把mysql中缓存数据存入到redis中
                String setResult = "";
                try {
                    // TODO 还有待完善
                    setResult = redisService.set(bookKey, bookList, NX, PX, null);// 这一行也可能抛异常
                    // 6.判断redis是否存储成功
                    if(OK.equals(setResult.toUpperCase())) {
                        // 说明redis存储成功，可以返回数据了
                        resultMap.put(CODE, SUCCESS_REDIS_SET.getCode());
                        resultMap.put(MSG, SUCCESS_REDIS_SET.getMsg());
                        resultMap.put(DATA, bookList);
                    } else {
                        // 说明redis没有存储成功，按照你们之前的写法肯定会返回一个null，需要把从mysql查询的数据传递给前端
                        // 好死不如赖活着(宁愿给客户端看到一个错误的结果，也不能让客户端看到null/异常)
                        resultMap.put(CODE, FAILED_REDIS_SET_DATA_OK.getCode());
                        resultMap.put(MSG, FAILED_REDIS_SET_DATA_OK.getMsg());
                        resultMap.put(DATA, bookList);
                    }
                } catch (Exception e) {
                    // 返回系统正在维护，打印堆栈信息，不需要处理！！！
                    resultMap.put(CODE, FAILED_REDIS_SET_DATA_OK.getCode());
                    resultMap.put(MSG, FAILED_REDIS_SET_DATA_OK.getMsg());
                    resultMap.put(DATA, bookList);
                    e.printStackTrace();
                    return resultMap;
                }

            } else {
                // 说明连mysql中都没有数据(提示前端)
                resultMap.put(CODE, NOT_FOUND_DATA.getCode());
                resultMap.put(MSG, NOT_FOUND_DATA.getMsg());
            }
        } else {
            // 说明redis中有数据,直接返回就完事了
            resultMap.put(CODE, SUCCESS_QUERY.getCode());
            resultMap.put(MSG, SUCCESS_QUERY.getMsg());
            resultMap.put(DATA, bookRedisValue);
        }
        return resultMap;
    }

}
