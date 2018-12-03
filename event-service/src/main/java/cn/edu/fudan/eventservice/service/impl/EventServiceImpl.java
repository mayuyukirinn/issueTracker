package cn.edu.fudan.eventservice.service.impl;

import cn.edu.fudan.eventservice.component.RestInterfaceManager;
import cn.edu.fudan.eventservice.dao.EventDao;
import cn.edu.fudan.eventservice.domain.Event;
import cn.edu.fudan.eventservice.domain.EventType;
import cn.edu.fudan.eventservice.service.EventService;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author WZY
 * @version 1.0
 **/
@Slf4j
@Service
public class EventServiceImpl implements EventService {

    private static final String EVENT_HAS_NEW_KEY_PREFIX="event:has_new:";

    private EventDao eventDao;
    private StringRedisTemplate stringRedisTemplate;
    private RestInterfaceManager restInterfaceManager;

    public EventServiceImpl(EventDao eventDao,
                            StringRedisTemplate stringRedisTemplate,
                            RestInterfaceManager restInterfaceManager) {
        this.eventDao = eventDao;
        this.stringRedisTemplate=stringRedisTemplate;
        this.restInterfaceManager=restInterfaceManager;
    }

    @Override
    public void addEvents(List<Event> events) {
        if(events!=null&&!events.isEmpty()){
            eventDao.addEvents(events);
            String repoId=events.get(0).getRepoId();
            String category=events.get(0).getCategory();
            stringRedisTemplate.opsForValue().set(EVENT_HAS_NEW_KEY_PREFIX+category+":"+repoId,"true");
        }
    }

    @Override
    public void deleteEvents(String repo_id, String category) {
        if(repo_id!=null&&category!=null)
            eventDao.deleteEventByRepoIdAndCategory(category,repo_id);
    }

    @Override
    public Object getCurrentEvents(String userToken,String category) {

        Map<String,Object> result=new HashMap<>();
        JSONArray repoIds=restInterfaceManager.getRepoIdsOfAccount(userToken, category);
        if(repoIds==null||repoIds.isEmpty()){
            if(category.equals("bug")){
                result.put("newBug",Collections.emptyList());
                result.put("eliminateBug",Collections.emptyList());
            }else{
                result.put("newCloneClass",Collections.emptyList());
                result.put("removeCloneClass",Collections.emptyList());
                result.put("newCloneInstance",Collections.emptyList());
                result.put("removeCloneInstance",Collections.emptyList());
            }
            return result;
        }
        List<String> repoIdList=repoIds.toJavaList(String.class);
        if(category.equals("bug")){
            List<Event> newBugList=eventDao.getRecentEventsByEventType(repoIdList, EventType.NEW_BUG);
            List<Event> eliminateBugList=eventDao.getRecentEventsByEventType(repoIdList,EventType.ELIMINATE_BUG);
            result.put("newBug",newBugList);
            result.put("eliminateBug",eliminateBugList);
        }else if(category.equals("clone")){
            List<Event> newCloneClassList=eventDao.getRecentEventsByEventType(repoIdList, EventType.NEW_CLONE_CLASS);
            List<Event> removeCloneClassList=eventDao.getRecentEventsByEventType(repoIdList,EventType.REMOVE_CLONE_CLASS);
            List<Event> newCloneInstanceList=eventDao.getRecentEventsByEventType(repoIdList, EventType.NEW_CLONE_INSTANCE);
            List<Event> removeCloneInstanceList=eventDao.getRecentEventsByEventType(repoIdList,EventType.REMOVE_CLONE_INSTANCE);
            result.put("newCloneClass",newCloneClassList);
            result.put("removeCloneClass",removeCloneClassList);
            result.put("newCloneInstance",newCloneInstanceList);
            result.put("removeCloneInstance",removeCloneInstanceList);
        }
        //查询完后，清除时间的更新状态
        for(String repoId:repoIdList){
            stringRedisTemplate.opsForValue().set(EVENT_HAS_NEW_KEY_PREFIX+category+":"+repoId,"false");
        }

        return result;
    }



    @Override
    public Object hasNewEvents(String userToken, String category) {
        Map<String,Object> result=new HashMap<>();
        JSONArray repoIDs=restInterfaceManager.getRepoIdsOfAccount(userToken, category);
        boolean hasNew=false;
        //当前用户只要有任意一个repo有更新，则认为有新的通知
        for(int i=0;i<repoIDs.size();i++){
            String repoId=repoIDs.getString(i);
            String repoHasNew=stringRedisTemplate.opsForValue().get(EVENT_HAS_NEW_KEY_PREFIX+category+":"+repoId);
            if(Boolean.valueOf(repoHasNew)){
                hasNew=true;
                break;
            }
        }
        result.put("hasNew",hasNew);
        return result;
    }
}
