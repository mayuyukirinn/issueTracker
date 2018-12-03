package cn.edu.fudan.eventservice.mapper;

import cn.edu.fudan.eventservice.domain.Event;
import cn.edu.fudan.eventservice.domain.EventType;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventMapper {

    int insertEvents(List<Event> list);

    void deleteEventByRepoIdAndCategory(@Param("category")String category,@Param("repo_id")String repo_id);

    List<Event> getRecentEventsByEventType(@Param("list") List<String> list, @Param("event_type")EventType eventType);
}
