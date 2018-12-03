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
public class FindBugScanTask {

    private Logger logger = LoggerFactory.getLogger(FindBugScanTask.class);

    //这边注入的是findBug的扫描的实现方式，如果是其它工具，可以换作其它实现
    @Resource(name = "findBug")
    private ScanOperation scanOperation;

    private RedisLuaLock redisLock;

    @Autowired
    public void setRedisLock(RedisLuaLock redisLock) {
        this.redisLock = redisLock;
    }

    private KafkaTemplate kafkaTemplate;

    @Autowired
    public void setKafkaTemplate(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @SuppressWarnings("unchecked")
    private void send(String repoId, String commitId,String status, String description) {
        ScanResult scanResult = new ScanResult(repoId, commitId, "bug",status, description);
        kafkaTemplate.send("ScanResult", JSONObject.toJSONString(scanResult));
    }

    private void scan(String repoId, String commitId,String category) {
//        if (scanOperation.isScanned(commitId)) {
//            //如果当前commit已经扫描过，直接结束
//            logger.info("this commit has been scanned");
//            send(repoId, commitId, "success", "scan success!");
//            logger.info("Scan Success!");
//            return;
//        }
//        logger.info("this commit has not been scanned");
        logger.info("start to checkout -> " + commitId);
        //checkout,如果失败发送错误消息，直接返回
        if (!scanOperation.checkOut(repoId, commitId)) {
            send(repoId, commitId, "failed", "check out failed");
            logger.error("Check Out Failed!");
            return;
        }
        logger.info("checkout complete -> start the scan operation......");

        ScanInitialInfo scanInitialInfo = scanOperation.initialScan(repoId, commitId,category);
        ScanResult scanResult = scanOperation.doScan(scanInitialInfo);
        if (scanResult.getStatus().equals("failed")) {
            send(repoId, commitId, "failed", scanResult.getDescription());
            logger.error(scanResult.getDescription());
            return;
        }
        logger.info("scan complete ->" + scanResult.getDescription());
        logger.info("start to mapping ......");
        if (!scanOperation.mapping(repoId, commitId,category)) {
            send(repoId, commitId, "failed", "Mapping failed");
            logger.error("Mapping Failed!");
            return;
        }
        logger.info("mapping complete");
        //映射结束，更新当前scan
        logger.info("start to update scan status");
        if (!scanOperation.updateScan(scanInitialInfo)) {
            send(repoId, commitId, "failed", "scan update failed");
            logger.error("Scan Update Failed!");
            return;
        }
        logger.info("scan update complete");
        send(repoId, commitId, "success", "all complete");
    }

    @Async
    public Future<String> run(String repoId, String commitId,String category) {
        //获取分布式锁，一个repo同一时间只能有一个线程在扫
        //15min恰好是一个整个Scan操作的超时时间，如果某个线程获得锁之后Scan过程卡死导致锁没有释放
        //如果那个锁成功设置了过期时间，那么key过期后，其他线程自然可以获取到锁
        //如果那个锁并没有成功地设置过期时间
        //那么等待获取同一个锁的线程会因为15min的超时而强行获取到锁，并设置自己的identifier和key的过期时间
        String identifier = redisLock.acquireLockWithTimeOut(repoId, 15, 15, TimeUnit.MINUTES);
        try {
            scan(repoId, commitId,category);
        } finally {
            if (!redisLock.releaseLock(repoId, identifier)) {
                logger.error("repo->" + repoId + " release lock failed!");
            }
        }
        return new AsyncResult<>("complete");
    }
}
