package cn.edu.fudan.issueservice.scheduler;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class QuartzScheduler {


    private RestInterfaceManager restInterfaceManager;

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private void updateTrend(String duration, String account_id, String repo_id, String category,int newIssueCount, int remainingIssueCount, int eliminatedIssueCount) {
        String newKey = "";
        String remainingKey = "";
        String eliminatedKey = "";
        if (repo_id == null) {
            newKey = "trend:"+category+":"+ duration + ":new:" + account_id;
            remainingKey = "trend:"+category+":"+ duration + ":remaining:" + account_id;
            eliminatedKey = "trend:"+category+":"+ duration + ":eliminated:" + account_id;
        } else {
            newKey = "trend:"+category+":"+ duration + ":new:" + account_id + ":" + repo_id;
            remainingKey = "trend:"+category+":"+ duration + ":remaining:" + account_id + ":" + repo_id;
            eliminatedKey = "trend:"+category+":"+ duration + ":eliminated:" + account_id + ":" + repo_id;
        }
        String now= DateTimeUtil.y_m_d_format(LocalDateTime.now());
        stringRedisTemplate.setEnableTransactionSupport(true);
        stringRedisTemplate.multi();
        stringRedisTemplate.opsForList().rightPush(newKey, now+":"+String.valueOf(newIssueCount));
        stringRedisTemplate.opsForList().trim(newKey,0,29);
        stringRedisTemplate.opsForList().rightPush(remainingKey,now+":"+String.valueOf(remainingIssueCount));
        stringRedisTemplate.opsForList().trim(remainingKey,0,29);
        stringRedisTemplate.opsForList().rightPush(eliminatedKey, now+":"+String.valueOf(eliminatedIssueCount));
        stringRedisTemplate.opsForList().trim(eliminatedKey,0,29);
        stringRedisTemplate.exec();
    }

    @Scheduled(cron = "0 50 23 * * *")
    public void perDay() {
       durationUpdate("day");
    }

    @Scheduled(cron = "0 55 23 * * SUN")
    public void perWeek() {
        durationUpdate("week");
    }

    @Scheduled(cron = "0 10 0 1 * *")
    public void perMonth() {
        JSONArray projects = restInterfaceManager.getProjectList(null);//所有用户的所有project
        if (projects == null||projects.isEmpty()) return;
        for (int i=0;i<projects.size();i++) {
            JSONObject project=projects.getJSONObject(i);
            if(project!=null){
                String repo_id=project.getString("uuid");
                String category=project.getString("type");
                String dashboardKey = "dashboard:"+category+":month:"+ repo_id;
                stringRedisTemplate.delete(dashboardKey);
            }
        }
    }

    private void initialNewAndEliminated(String key){
        stringRedisTemplate.setEnableTransactionSupport(true);
        stringRedisTemplate.multi();
        stringRedisTemplate.opsForHash().put(key,"new","0");
        stringRedisTemplate.opsForHash().put(key,"eliminated","0");
        stringRedisTemplate.exec();
    }

    private void durationUpdate(String duration){
        List<String> accountIds=restInterfaceManager.getAccountIds();
        for (String account_id : accountIds) {
            JSONArray projects = restInterfaceManager.getProjectList(account_id);
            if (projects == null||projects.isEmpty()) continue;
            int newSummary = 0;
            int remainingSummary = 0;
            int eliminatedSummary = 0;
            Set<String> categories=new HashSet<>();
            for (int i=0;i<projects.size();i++) {
                JSONObject project=projects.getJSONObject(i);
                if(project!=null){
                    String repo_id=project.getString("repo_id");
                    String category=project.getString("type");
                    String dashboardKey = "dashboard:"+category+":"+duration+":"+ repo_id;
                    String newIssueInfoKey="dashboard:"+category+":"+duration+":new"+ repo_id;
                    stringRedisTemplate.delete(newIssueInfoKey);
                    Boolean hasKey=stringRedisTemplate.hasKey(dashboardKey);
                    if(hasKey!=null&&hasKey){
                        categories.add(category);
                        int newIssueCount = Integer.parseInt((String) stringRedisTemplate.opsForHash().get(dashboardKey, "new"));
                        int remainingIssueCount = Integer.parseInt((String) stringRedisTemplate.opsForHash().get(dashboardKey, "remaining"));
                        int eliminatedIssueCount = Integer.parseInt((String) stringRedisTemplate.opsForHash().get(dashboardKey, "eliminated"));
                        updateTrend(duration, account_id, repo_id,category, newIssueCount, remainingIssueCount, eliminatedIssueCount);
                        newSummary += newIssueCount;
                        remainingSummary += remainingIssueCount;
                        eliminatedSummary += eliminatedIssueCount;
                        //new和eliminated清零
                        initialNewAndEliminated(dashboardKey);
                    }
                }
            }
            //当前用户所有
            for(String category:categories){
                updateTrend(duration, account_id, null,category, newSummary, remainingSummary, eliminatedSummary);
            }
        }
    }

    public void updateIssueCount(String time) {
        if (time.equals("day")) {
            perDay();
        }else if (time.equals("week")){
            perWeek();
        }else {
            perMonth();
        }
    }

}
