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
import org.springframework.stereotype.Component;

/**
 * @author WZY
 * @version 1.0
 **/
@Component
public class BaseScanTask {

    private Logger logger = LoggerFactory.getLogger(BaseScanTask.class);

    RedisLuaLock redisLock;

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

    void scan(ScanOperation scanOperation, String repoId, String commitId, String category){
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
}
