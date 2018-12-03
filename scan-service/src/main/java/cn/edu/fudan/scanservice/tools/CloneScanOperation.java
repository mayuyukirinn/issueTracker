package cn.edu.fudan.scanservice.tools;

import cn.edu.fudan.scanservice.domain.ScanInitialInfo;
import cn.edu.fudan.scanservice.domain.ScanResult;
import cn.edu.fudan.scanservice.service.impl.ScanOperationAdapter;
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
@Component("GPUClone")
public class CloneScanOperation extends ScanOperationAdapter {

    private Logger logger = LoggerFactory.getLogger(CloneScanOperation.class);

    private KafkaTemplate kafkaTemplate;

    @Autowired
    public void setKafkaTemplate(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ScanResult doScan(ScanInitialInfo scanInitialInfo) {
        kafkaTemplate.send("Clone", JSONObject.toJSONString(scanInitialInfo));
        logger.info("send message to clone service for scanning......");
        return null;
    }


}
