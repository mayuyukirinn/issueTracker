package cn.edu.fudan.scanservice.task;

import cn.edu.fudan.scanservice.domain.ScanInitialInfo;
import cn.edu.fudan.scanservice.service.ScanOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author WZY
 * @version 1.0
 **/
@Component
public class CloneScanTask {
    private Logger logger = LoggerFactory.getLogger(CloneScanTask.class);

    //这边注入的是GPUClone的扫描的实现方式，如果是其它工具，可以换作其它实现
    @Resource(name = "GPUClone")
    private ScanOperation scanOperation;

    @Async
    public void run(String repoId, String commitId,String category) {
        ScanInitialInfo scanInitialInfo = scanOperation.initialScan(repoId, commitId,category);
        scanOperation.doScan(scanInitialInfo);
    }

}
