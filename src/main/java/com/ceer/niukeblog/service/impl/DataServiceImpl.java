package com.ceer.niukeblog.service.impl;

import com.ceer.niukeblog.service.DataService;
import com.ceer.niukeblog.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @ClassName DataServiceImpl
 * @Description TODO
 * @Author ceer
 * @Date 2020/5/9 17:20
 * @Version 1.0
 */
@Service
public class DataServiceImpl implements DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    /**
     * @Description: 将指定的IP计入UV
     * @param:
     * @return:
     * @date: 2020/5/9 17:25
     */
    @Override
    public void recordUV(String ip) {
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    /**
     * @Description: 统计指定日期范围内的UV
     * @param:
     * @return:
     * @date: 2020/5/9 17:25
     */
    @Override
    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 整理该日期范围内的key
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        //Calendar.after()方法返回此Calendar的时间是否在指定Object(时间)所表示的时间之后。
        //开始时间小于结束时间 就将开始时间存入集合中
        // 然后加开始时间加一天 继续循环 直到开始时间大于结束时间为止
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            //表示对日期进行加一天操作
            calendar.add(Calendar.DATE, 1);
        }

        // 合并这些数据
        String redisKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

        // 返回统计的结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    /**
     * @Description: 将指定用户计入DAU
     * @param:
     * @return:
     * @date: 2020/5/9 17:25
     */
    @Override
    public void recordDAU(Integer userId) {
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    /**
     * @Description: 统计指定日期范围内的DAU
     * @param:
     * @return:
     * @date: 2020/5/9 17:36
     */
    @Override
    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 整理该日期范围内的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        // 进行OR运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), keyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }
}
