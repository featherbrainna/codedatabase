package com.wzy.codedatabase.redis.idgenerator;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * 基于Redis的分布式全局ID
 * @author 王忠义
 * @version 1.0
 * @date 2023/6/22 16:11
 */
@Component
public class RedisIdWorker {

    /**
     * 开始时间戳 2022/01/01 00:00:00
     */
    private static final long BEGIN_TIMESTAMP = 1640995200L;
    /**
     * 序列号位数
     */
    private static final int COUNT_BITS = 32;

    private StringRedisTemplate stringRedisTemplate;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 获取下一个Id
     * @param keyPrefix id的对应业务标识key
     * @return id
     */
    public long getNextId(String keyPrefix){
        //1.生成时间戳,高位32位。
        LocalDateTime now = LocalDateTime.now();
        long timeStamp = now.toEpochSecond(ZoneOffset.UTC) - BEGIN_TIMESTAMP;
        timeStamp = timeStamp << COUNT_BITS;//左移32位
        timeStamp = timeStamp & ((1L << 63) - 1L);//保证首位是0

        //2.生成序列号，低位32位，从redis的对应key的value获取
        //2.1.获取当前日期，精确到天。1.避免序列号超过32位，2.利于按天统计
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long serial = stringRedisTemplate.opsForValue().increment("incr:" + keyPrefix + ":" + date);

        //3.拼接并返回
        return timeStamp | serial;
    }

    //打印l的字节数组。从高位到低位，实现程序员计算器类似功能
    private static void printBit(long l) {
        byte[] bytes = new byte[64];
        for (int i = 0; i < 64; i++) {
            bytes[i] = (byte) (((l & (1L << 63-i)) != 0)?1:0);
        }
        System.out.println(Arrays.toString(bytes));
    }
}
