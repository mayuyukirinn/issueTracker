package cn.edu.fudan.cloneservice.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author WZY
 * @version 1.0
 **/
@Component
public class RedisLock {

    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public String acquireLock(String lockName, long acquireTimeout, long lockTimeout, TimeUnit timeUnit) {
        String identifier = UUID.randomUUID().toString();
        String lockKey = "lock:" + lockName;
        long end = System.currentTimeMillis() + timeUnit.toMillis(acquireTimeout);
        while (System.currentTimeMillis() < end) {
            //只有当前的key不存在才能设置成功
            Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, identifier);
            if (success != null && success) {
                stringRedisTemplate.expire(lockKey, lockTimeout, timeUnit);//只要这个键没了，就相当于释放了
                return identifier;
            }
            //到这里说明lockKey已经存在
            //有可能程序执行完setnx后挂了，没来得及设置过期时间
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        //获取锁超时，强制当前线程获得锁，并设置key的超时时间
        stringRedisTemplate.opsForValue().set(lockKey, identifier, lockTimeout, timeUnit);
        return identifier;
    }

    public boolean releaseLock(String lockName, String identifier) {
        String lockKey = "lock:" + lockName;
        while (true) {
            stringRedisTemplate.watch(lockKey);
            if (identifier.equals(stringRedisTemplate.opsForValue().get(lockKey))) {
                stringRedisTemplate.multi();
                stringRedisTemplate.delete(lockKey);
                List<Object> result = stringRedisTemplate.exec();
                if (result == null)
                    continue;
                return true;
            }
            stringRedisTemplate.unwatch();
            break;
        }
        return false;
    }
}
