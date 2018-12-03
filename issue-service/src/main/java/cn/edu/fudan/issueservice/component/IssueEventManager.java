package cn.edu.fudan.issueservice.component;

import cn.edu.fudan.issueservice.domain.EventType;
import cn.edu.fudan.issueservice.domain.Issue;
import cn.edu.fudan.issueservice.domain.RawIssue;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author WZY
 * @version 1.0
 **/
@Component
public class IssueEventManager {

    @Value("${event.service.path}")
    private String eventServicePath;

    private RestTemplate restTemplate;

    public IssueEventManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendIssueEvent(EventType eventType, List<Issue> issues,String committer, String repoId){
        JSONArray issueEvents=new JSONArray();
        String now= DateTimeUtil.format(LocalDateTime.now());
        for(Issue issue:issues){
            JSONObject event=new JSONObject();
            event.put("id", UUID.randomUUID());
            event.put("category",issue.getCategory());
            event.put("targetType",issue.getType());
            event.put("targetId",issue.getUuid());
            event.put("targetCommitter",committer);
            event.put("repoId",repoId);
            event.put("createTime",now);
            event.put("eventType",eventType.toString());
            issueEvents.add(event);
        }
        if(!issueEvents.isEmpty())
             restTemplate.postForObject(eventServicePath,issueEvents,JSONObject.class);
    }

    public void sendRawIssueEvent(EventType eventType,List<RawIssue> rawIssues,String committer,String repoId){
        JSONArray issueEvents=new JSONArray();
        String now= DateTimeUtil.format(LocalDateTime.now());
        for(RawIssue rawIssue:rawIssues){
            JSONObject event=new JSONObject();
            event.put("id", UUID.randomUUID());
            event.put("category",rawIssue.getCategory());
            event.put("targetType",rawIssue.getType());
            event.put("targetId",rawIssue.getUuid());
            event.put("targetCommitter",committer);
            event.put("repoId",repoId);
            event.put("createTime",now);
            event.put("eventType",eventType.toString());
            issueEvents.add(event);
        }
        if(!issueEvents.isEmpty())
            restTemplate.postForObject(eventServicePath,issueEvents,JSONObject.class);
    }
}
