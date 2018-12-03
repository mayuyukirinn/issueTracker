package cn.edu.fudan.scanservice.controller;

import cn.edu.fudan.scanservice.domain.ResponseBean;
import cn.edu.fudan.scanservice.service.KafkaService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author WZY
 * @version 1.0
 **/
@RestController
public class CloneController {

    private KafkaService kafkaService;

    @Autowired
    public void setKafkaService(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @PostMapping(value = {"/scan/clone"})
    @CrossOrigin
    public Object scan(@RequestBody JSONObject requestParam) {
        try {
            kafkaService.scanByRequest(requestParam);
            return new ResponseBean(200, "scan msg send success!", null);
        } catch (Exception e) {
            return new ResponseBean(401, e.getMessage(), null);
        }
    }
}
