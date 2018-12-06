package cn.edu.fudan.scanservice.task;

import cn.edu.fudan.scanservice.domain.ScanInitialInfo;
import cn.edu.fudan.scanservice.domain.ScanResult;
import cn.edu.fudan.scanservice.service.ScanOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author WZY
 * @version 1.0
 **/

@Component
public class CloneScanTask extends BaseScanTask{

    private static final Logger logger= LoggerFactory.getLogger(CloneScanTask.class);


    //这边注入的是GPUClone的扫描的实现方式，如果是其它工具，可以换作其它实现
    @Resource(name = "CPUClone")
    private ScanOperation scanOperation;


    @Async("forRequest")
    public Future<String> run(String repoId, String commitId, String category) {
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
