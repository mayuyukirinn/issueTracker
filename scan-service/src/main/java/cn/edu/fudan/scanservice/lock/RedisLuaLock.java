package cn.edu.fudan.scanservice.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author WZY
 * @version 1.0
 **/
@Component
public class RedisLuaLock {

    private final DefaultRedisScript<Boolean> acquireScript;

    private final DefaultRedisScript<Boolean> releaseScript;

    //最好用StringRedisTemplate来执行Lua脚本
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisLuaLock(DefaultRedisScript<Boolean> acquireScript,
                        DefaultRedisScript<Boolean> releaseScript,
                        StringRedisTemplate stringRedisTemplate) {
        this.acquireScript=acquireScript;
        this.releaseScript=releaseScript;
        this.stringRedisTemplate=stringRedisTemplate;
    }

    public String acquireLockWithTimeOut(String lockName, long acquireTimeOut, long lockTimeOut, TimeUnit timeUnit){
        String identifier= UUID.randomUUID().toString();
        lockName="lock:"+lockName;
        int lockExpire = (int)timeUnit.toMillis(lockTimeOut);
        long end=System.currentTimeMillis()+timeUnit.toMillis(acquireTimeOut);
        while(System.currentTimeMillis()<end){
            Boolean acquired= stringRedisTemplate.execute(acquireScript, Collections.singletonList(lockName),String.valueOf(lockExpire),identifier);
            if(acquired!=null&&acquired)
                return identifier;
            try {
                Thread.sleep(10);
            }catch(InterruptedException ie){
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    public boolean releaseLock(String lockName,String identifier){
        lockName="lock:"+lockName;
        Boolean success=stringRedisTemplate.execute(releaseScript,Collections.singletonList(lockName),identifier);
        return success!=null&&success;
    }


}
