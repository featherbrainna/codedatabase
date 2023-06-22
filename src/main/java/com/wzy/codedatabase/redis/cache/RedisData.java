package com.wzy.codedatabase.redis.cache;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Redis存储的数据结构
 * 带逻辑过期时间的Redis缓存
 */
@Data
public class RedisData {
    private LocalDateTime expireTime;
    private Object data;
}
