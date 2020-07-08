package com.aaa.lee.redis.statusenum;

/**
 * @Company AAA软件教育
 * @Author Seven Lee
 * @Date Create in 2020/3/25 17:24
 * @Description
 **/
public enum StatusEnum {

    SUCCESS_INTNET_EXCEPTION("201", "网络波动，数据正常"),
    SUCCESS_REDIS_SET("203", "redis存储成功"),
    SUCCESS_QUERY("204", "查询成功"),
    NOT_FOUND_DATA("401", "暂无法找到您所要的数据信息！！"),
    FAILED_SYSTEM_EXCEPTION("501", "系统正在维护，请稍后重试！！"),
    FAILED_REDIS_SET_DATA_OK("503", "redis存储失败，数据库中有数据");

    StatusEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private String code;
    private String msg;

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
