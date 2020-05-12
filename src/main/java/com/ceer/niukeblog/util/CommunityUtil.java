package com.ceer.niukeblog.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

/**
 * @ClassName CommunityUtil
 * @Description  工具类
 * @Author ceer
 * @Date 2020/4/30 0:41
 * @Version 1.0
 */
public class CommunityUtil {

    /**
     * @Description: 生成随机字符串
     * @param:
     * @return:
     * @date: 2020/4/30 0:42
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * @Description: MD5加密
     * @param:
     * @return:
     * @date: 2020/4/30 0:42
     */
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /**
     * @Description:
     * @param:
     * @return:
     * @date: 2020/5/1 23:40
     */
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

    public static void main(String[] args) {
        System.out.println(CommunityUtil.md5( "123456df6cd"));
    }
}
