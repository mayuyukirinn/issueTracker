package cn.edu.fudan.eventservice.dao;

import cn.edu.fudan.eventservice.domain.Event;
import cn.edu.fudan.eventservice.domain.EventType;
import cn.edu.fudan.eventservice.mapper.EventMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author WZY
 * @version 1.0
 **/
@Slf4j
@Repository
public class EventDao {

    private EventMapper eventMapper;

    public EventDao(EventMapper eventMapper) {
        this.eventMapper = eventMapper;
    }

    public void addEvents(List<Event> events){
        int count=eventMapper.insertEvents(events);
        log.info("insert {} events",count);
    }

    public void deleteEventByRepoIdAndCategory(String category,String repo_id){
        eventMapper.deleteEventByRepoIdAndCategory(category, repo_id);
    }

    public List<Event> getRecentEventsByEventType(List<String> repoIds, EventType eventType){
        return eventMapper.getRecentEventsByEventType(repoIds,eventType);
    }
}
