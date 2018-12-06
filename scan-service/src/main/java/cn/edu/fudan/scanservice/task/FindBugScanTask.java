package cn.edu.fudan.scanservice.task;

import cn.edu.fudan.scanservice.domain.ScanInitialInfo;
import cn.edu.fudan.scanservice.domain.ScanResult;
import cn.edu.fudan.scanservice.lock.RedisLuaLock;
import cn.edu.fudan.scanservice.service.ScanOperation;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 异步的scan任务
 *
 * @author WZY
 * @version 1.0
 **/
@Component
public class FindBugScanTask extends BaseScanTask{

    private Logger logger = LoggerFactory.getLogger(FindBugScanTask.class);

    //这边注入的是findBug的扫描的实现方式，如果是其它工具，可以换作其它实现
    @Resource(name = "findBug")
    private ScanOperation scanOperation;


    @Async("forRequest")
    public Future<String> run(String repoId, String commitId,String category) {
        //获取分布式锁，一个repo同一时间只能有一个线程在扫
        //15min恰好是一个整个Scan操作的超时时间，如果某个线程获得锁之后Scan过程卡死导致锁没有释放
        //如果那个锁成功设置了过期时间，那么key过期后，其他线程自然可以获取到锁
        //如果那个锁并没有成功地设置过期时间
        //那么等待获取同一个锁的线程会因为15min的超时而强行获取到锁，并设置自己的identifier和key的过期时间
        String identifier = redisLock.acquireLockWithTimeOut(repoId, 15, 15, TimeUnit.MINUTES);
        try {
            scan(scanOperation,repoId, commitId,category);
        } finally {
            if (!redisLock.releaseLock(repoId, identifier)) {
                logger.error("repo->" + repoId + " release lock failed!");
            }
        }
        return new AsyncResult<>("complete");
    }

    public void runSynchronously(String repoId,String commitId,String category){
        scan(scanOperation,repoId, commitId,category);
    }
}
