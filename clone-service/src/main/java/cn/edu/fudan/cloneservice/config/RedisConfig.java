package cn.edu.fudan.cloneservice.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;
/**
 * @author WZY
 * @version 1.0
 **/
@Configuration
public class RedisConfig extends CachingConfigurerSupport {

    @Bean("acquireScript")
    public DefaultRedisScript<Boolean> redisScript(){
        DefaultRedisScript<Boolean> redisScript=new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/cn/edu/fudan/cloneservice/lua/AcquireRedisLock.lua")));
        redisScript.setResultType(Boolean.class);
        return redisScript;
    }

    @Bean("releaseScript")
    public DefaultRedisScript<Boolean> releaseScript(){
        DefaultRedisScript<Boolean> redisScript=new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/cn/edu/fudan/cloneservice/lua/ReleaseRedisLock.lua")));
        redisScript.setResultType(Boolean.class);
        return redisScript;
    }


    @Bean("myRedisTemplate")
    public RedisTemplate<String,Object> myRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(getCustomizeKeySerializer());
        template.setValueSerializer(getCustomizeValueSerializer());
        template.setHashKeySerializer(getCustomizeKeySerializer());
        template.setHashValueSerializer(getCustomizeValueSerializer());
        template.afterPropertiesSet();
        return template;
    }

    private RedisSerializer<String> getCustomizeKeySerializer(){
        return new StringRedisSerializer();
    }


    private RedisSerializer<Object> getCustomizeValueSerializer(){
        //值序列化使用的是jackson,也可以改成自定义的fastjson
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        //下面如果不设置，反序列化为实体类时会出错
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        return jackson2JsonRedisSerializer;
    }
}
