package cn.edu.fudan.projectmanager.service;

import cn.edu.fudan.projectmanager.domain.CompleteDownLoad;
import cn.edu.fudan.projectmanager.domain.Project;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerService {


    private ProjectService projectService;

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    private Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @KafkaListener(id = "projectUpdate", topics = {"RepoManager"}, groupId = "project")
    public void update(ConsumerRecord<String, String> consumerRecord) {
        String msg = consumerRecord.value();
        logger.info("receive message from topic -> " + consumerRecord.topic() + " : " + msg);
        CompleteDownLoad completeDownLoad = JSONObject.parseObject(msg, CompleteDownLoad.class);
        Project currentProject = projectService.getProjectByID(completeDownLoad.getProjectId());
        currentProject.setLanguage(completeDownLoad.getLanguage());
        currentProject.setVcs_type(completeDownLoad.getVcs_type());
        currentProject.setDownload_status(completeDownLoad.getStatus());
        currentProject.setDescription(completeDownLoad.getDescription());
        currentProject.setRepo_id(completeDownLoad.getRepo_id());
        projectService.updateProjectStatus(currentProject);
    }
}
