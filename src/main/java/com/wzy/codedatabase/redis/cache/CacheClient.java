package com.wzy.codedatabase.redis.cache;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存的读、写工具类
 * 解决了缓存击穿和缓存穿透问题，缓存雪崩无需这里解决。
 * 固定了缓存的流程逻辑，先读redis后读maysql，且通过锁（特殊的分布式锁）和缓存空值来降低了mysql的压力。
 * @author 王忠义
 * @version 1.0
 * @date 2023/6/22 10:29
 */
@Slf4j
@Component
public class CacheClient {

    /**
     * redis连接
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 写入redis任意对象数据，并设置过期时间
     * @param key 键
     * @param value 对象数据值
     * @param time 过期时间
     * @param unit 时间单位
     */
    public void set(String key, Object value, Long time, TimeUnit unit){
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value),time,unit);
    }

    /**
     * 写入redis任意对象数据，并设置逻辑过期时间
     * @param key 键
     * @param value 对象数据值（会被封装为RedisData）
     * @param time 逻辑过期时间
     * @param unit 时间单位
     */
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit){

        //1.设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));

        //2.写入redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 读数据：从缓存和数据库
     * 解决了缓存穿透，没有解决缓存击穿
     * @param keyPrefix 缓存key前缀
     * @param id
     * @param type 缓存数据值类型
     * @param dbFallBack 数据库查询逻辑
     * @param time 缓存过期时间
     * @param unit 时间单位
     * @param <R> 数据类型参数
     * @param <ID> id类型参数
     * @return 数据
     */
    public <R,ID> R queryWithPassThrough(
            String keyPrefix, ID id, Class<R> type, Function<ID,R> dbFallBack,
            Long time, TimeUnit unit){
        String key = keyPrefix + id;
        //1.从redis查询信息缓存（根据id）
        String json = stringRedisTemplate.opsForValue().get(key);

        //2.判断是否存在缓存
        if (StrUtil.isNotBlank(json)){
            //3.存在，返回
            return JSONUtil.toBean(json, type);
        }
        //判断返回的是否是空值
        if (json!=null){
            //返回错误信息
            return null;
        }

        //4.不存在，根据id查询数据库
        R r = dbFallBack.apply(id);

        //5.判断数据库中是否存在商铺信息
        if (r==null){
            //将空值写入redis
            set(key,"",time, unit);
            //6.不存在，返回错误信息
            return null;
        }

        //7.存在，写入redis
        set(key,JSONUtil.toJsonStr(r),time, unit);

        //8.返回商铺信息
        return r;
    }

    //线程池
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * 读数据：从缓存和数据库
     * 解决了缓存击穿，通过逻辑过期。没有缓存穿透问题，不存在的缓存直接在redis层返回null
     * ========================================================
     * 增数据/改数据：先操作数据库；再操作缓存使用setWithLogicalExpire()；再添加方法为事务确保同时成功与失败
     * 删数据：先操作数据库；再操作缓存直接删除redis中的键；再添加方法为事务确保同时成功与失败
     * @param keyPrefix 缓存key前缀
     * @param id
     * @param lockPrefix 缓存的key对应的锁的前缀
     * @param type 缓存数据值类型
     * @param dbFallBack 数据库查询逻辑
     * @param time 缓存逻辑过期时间
     * @param unit 时间单位
     * @param <R> 数据类型参数
     * @param <ID> id类型参数
     * @return 数据
     */
    public <R,ID> R queryWithLogicalExpire(
            String keyPrefix,ID id,String lockPrefix,Class<R> type,Function<ID,R> dbFallBack,
            Long time,TimeUnit unit) throws InterruptedException {
        String key = keyPrefix + id;
        //1.从redis查询信息缓存（根据id）
        String json = stringRedisTemplate.opsForValue().get(key);

        //2.判断是否存在缓存
        if (StrUtil.isBlank(json)){
            //3.不存在，返回
            return null;
        }

        //4.命中，需要把json反序列化为对象
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);

        //5.判断是否过期
        LocalDateTime expireTime = redisData.getExpireTime();
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        //5.1.未过期，直接返回信息
        if (LocalDateTime.now().isBefore(expireTime)){
            return r;
        }

        //5.2.已过期，需要缓存重建

        //6.缓存重建
        String lockkey = lockPrefix + id;
        //6.1.获取互斥锁
        boolean flag = tryLock(lockkey);
        //6.2.判断是否获取锁成功
        if (flag){
            //双端检锁
            //1.从redis查询信息缓存（根据id）
            json = stringRedisTemplate.opsForValue().get(key);

            //2.判断是否存在缓存
            if (StrUtil.isBlank(json)){
                //3.不存在，返回
                return null;
            }

            //4.命中，需要把json反序列化为对象
            redisData = JSONUtil.toBean(json, RedisData.class);

            //5.判断是否过期
            expireTime = redisData.getExpireTime();
            r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
            //5.1.未过期，直接返回信息
            if (LocalDateTime.now().isBefore(expireTime)){
                return r;
            }

            //6.3.成功，开启独立线程，实现缓存重建
            CompletableFuture.runAsync(()->{
                try {
                    //重建缓存
                    //查数据库
                    R newr = dbFallBack.apply(id);

                    //缓存逻辑过期
                    setWithLogicalExpire(key,newr,time,unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    //释放锁
                    unlock(lockkey);
                }
            },CACHE_REBUILD_EXECUTOR);
        }

        //6.4.返回过期的商铺信息
        return r;
    }

    /**
     * 读数据：从缓存和数据库，针对数组数据的缓存
     * @param keyPrefix 缓存key前缀
     * @param id
     * @param lockPrefix 缓存的key对应的锁的前缀
     * @param type 缓存数据单个元素值类型
     * @param dbFallBack 数据库查询逻辑
     * @param time 缓存逻辑过期时间
     * @param unit 时间单位
     * @param <R> 数据单个元素类型参数
     * @param <ID> id类型参数
     * @return 数据
     */
    public <R,ID> List<R> queryWithLogicalExpireForArr(
            String keyPrefix,ID id,String lockPrefix,Class<R> type,Function<ID,List<R>> dbFallBack,
            Long time,TimeUnit unit) throws InterruptedException {
        String key = keyPrefix + id;
        //1.从redis查询信息缓存（根据id）
        String json = stringRedisTemplate.opsForValue().get(key);

        //2.判断是否存在缓存
        if (StrUtil.isBlank(json)){
            //3.不存在，返回
            return null;
        }

        //4.命中，需要把json反序列化为对象
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);

        //5.判断是否过期
        LocalDateTime expireTime = redisData.getExpireTime();
        List<R> rList = JSONUtil.toList((JSONArray) redisData.getData(), type);
        //5.1.未过期，直接返回信息
        if (LocalDateTime.now().isBefore(expireTime)){
            return rList;
        }

        //5.2.已过期，需要缓存重建

        //6.缓存重建
        String lockkey = lockPrefix + id;
        //6.1.获取互斥锁
        boolean flag = tryLock(lockkey);
        //6.2.判断是否获取锁成功
        if (flag){
            //双端检锁
            //1.从redis查询信息缓存（根据id）
            json = stringRedisTemplate.opsForValue().get(key);

            //2.判断是否存在缓存
            if (StrUtil.isBlank(json)){
                //3.不存在，返回
                return null;
            }

            //4.命中，需要把json反序列化为对象
            redisData = JSONUtil.toBean(json, RedisData.class);

            //5.判断是否过期
            expireTime = redisData.getExpireTime();
            rList = JSONUtil.toList((JSONArray) redisData.getData(), type);
            //5.1.未过期，直接返回信息
            if (LocalDateTime.now().isBefore(expireTime)){
                return rList;
            }

            //6.3.成功，开启独立线程，实现缓存重建
            CompletableFuture.runAsync(()->{
                try {
                    //重建缓存
                    //查数据库
                    List<R> newr = dbFallBack.apply(id);

                    //缓存逻辑过期
                    setWithLogicalExpire(key,newr,time,unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    //释放锁
                    unlock(lockkey);
                }
            },CACHE_REBUILD_EXECUTOR);
        }

        //6.4.返回过期的商铺信息
        return rList;
    }

    /**
     * 读数据：从缓存和数据库
     * 解决了缓存穿透和缓存击穿，通过互斥锁和空值缓存
     * ========================================================
     * 增数据：先操作数据库；再操作缓存使用set()；再添加方法为事务确保同时成功与失败（或者无需操作缓存！！！）
     * 删数据/改数据：先操作数据库；再操作缓存直接删除redis中的键；再添加方法为事务确保同时成功与失败
     * @param keyPrefix 缓存key前缀
     * @param id
     * @param lockPrefix 缓存的key对应的锁的前缀
     * @param type 缓存数据值类型
     * @param dbFallBack 数据库查询逻辑
     * @param time 缓存过期时间
     * @param unit 时间单位
     * @param <R> 数据类型参数
     * @param <ID> id类型参数
     * @return 数据
     */
    public <R,ID> R queryWithMutex(
            String keyPrefix,ID id,String lockPrefix,Class<R> type,Function<ID,R> dbFallBack,
            Long time,TimeUnit unit) throws InterruptedException {
        String key = keyPrefix + id;
        //1.从redis查询商铺信息缓存（根据商铺id）
        String json = stringRedisTemplate.opsForValue().get(key);

        //2.判断是否存在缓存
        if (StrUtil.isNotBlank(json)){
            //3.存在，返回商铺信息
            return JSONUtil.toBean(json, type);
        }
        //判断返回的是否是空值
        if (json!=null){
            //返回错误信息
            return null;
        }

        //4.实现缓存重建
        String lockkey = lockPrefix + id;
        //4.1.获取互斥锁
        boolean flag = tryLock(lockkey);
        R r = null;
        try {
            //4.2.判断是否获取成功
            if (!flag){
                //4.3.失败，则休眠并重试
                //暂停几毫秒钟线程
                Thread.sleep(50);
                return queryWithMutex(keyPrefix,id,lockPrefix,type,dbFallBack,time,unit);
            }

            //双端检锁
            //1.从redis查询商铺信息缓存（根据商铺id）
            json = stringRedisTemplate.opsForValue().get(key);

            //2.判断是否存在缓存
            if (StrUtil.isNotBlank(json)){
                //3.存在，返回商铺信息
                return JSONUtil.toBean(json, type);
            }
            //判断返回的是否是空值
            if (json!=null){
                //返回错误信息
                return null;
            }

            //4.4.成功，根据id查询数据库
            r = dbFallBack.apply(id);

            //5.判断数据库中是否存在商铺信息
            if (r==null){
                //将空值写入redis
                set(key,"",time,unit);
                //6.不存在，返回错误信息
                return null;
            }

            //7.存在，写入redis
            set(key,r,time,unit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            //8.释放互斥锁
            unlock(lockkey);
        }

        //9.返回商铺信息
        return r;
    }

    /**
     * 读数据：从缓存和数据库，针对数组数据的缓存
     * @param keyPrefix 缓存key前缀
     * @param id
     * @param lockPrefix 缓存的key对应的锁的前缀
     * @param type 缓存数据的单个元素值类型
     * @param dbFallBack 数据库查询逻辑
     * @param time 缓存过期时间
     * @param unit 时间单位
     * @param <R> 数据单个元素类型参数
     * @param <ID> id类型参数
     * @return 数据
     */
    public <R,ID> List<R> queryWithMutexForArr(
            String keyPrefix,ID id,String lockPrefix,Class<R> type,Function<ID,List<R>> dbFallBack,
            Long time,TimeUnit unit) throws InterruptedException {
        String key = keyPrefix + id;
        //1.从redis查询商铺信息缓存（根据商铺id）
        String json = stringRedisTemplate.opsForValue().get(key);

        //2.判断是否存在缓存
        if (StrUtil.isNotBlank(json)){
            //3.存在，返回商铺信息
            return JSONUtil.toList(json, type);
        }
        //判断返回的是否是空值
        if (json!=null){
            //返回错误信息
            return null;
        }

        //4.实现缓存重建
        String lockkey = lockPrefix + id;
        //4.1.获取互斥锁
        boolean flag = tryLock(lockkey);
        List<R> rList = null;
        try {
            //4.2.判断是否获取成功
            if (!flag){
                //4.3.失败，则休眠并重试
                //暂停几毫秒钟线程
                Thread.sleep(50);
                return queryWithMutexForArr(keyPrefix,id,lockPrefix,type,dbFallBack,time,unit);
            }

            //双端检锁
            //1.从redis查询商铺信息缓存（根据商铺id）
            json = stringRedisTemplate.opsForValue().get(key);

            //2.判断是否存在缓存
            if (StrUtil.isNotBlank(json)){
                //3.存在，返回商铺信息
                return JSONUtil.toList(json, type);
            }
            //判断返回的是否是空值
            if (json!=null){
                //返回错误信息
                return null;
            }

            //4.4.成功，根据id查询数据库
            rList = dbFallBack.apply(id);

            //5.判断数据库中是否存在商铺信息
            if (rList==null){
                //将空值写入redis
                set(key,"",time,unit);
                //6.不存在，返回错误信息
                return null;
            }

            //7.存在，写入redis
            set(key,rList,time,unit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            //8.释放互斥锁
            unlock(lockkey);
        }

        //9.返回商铺信息
        return rList;
    }

    /**
     * 分布式互斥锁获取锁
     * @param key 锁的名字
     * @return
     */
    public boolean tryLock(String key) throws InterruptedException {
        RLock lock = redissonClient.getLock(key);
        boolean flag = lock.tryLock(-1L,10L,TimeUnit.SECONDS);
        //Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 分布式互斥锁释放锁
     * @param key 锁的名字
     */
    public void unlock(String key){
        RLock lock = redissonClient.getLock(key);
        lock.unlock();
        //stringRedisTemplate.delete(key);
    }

    /**
     * 通过查询数据库预热redis
     * @param keyPrefix key缓存前缀
     * @param id
     * @param expireSeconds 逻辑过期时间
     * @param dbFallBack 数据库查询逻辑
     * @param <R>
     * @param <ID>
     */
    public <R,ID> void saveShop2Redis(String keyPrefix,ID id,Long expireSeconds,Function<ID,R> dbFallBack){
        //1.从数据库获取信息
        R r = dbFallBack.apply(id);

        //2.设置封装逻辑过期时间,存入Redis
        setWithLogicalExpire(keyPrefix+id,r,expireSeconds,TimeUnit.SECONDS);
    }

}
