package cn.edu.fudan.eventservice.service;

import cn.edu.fudan.eventservice.domain.Event;

import java.util.List;

/**
 * @author WZY
 * @version 1.0
 **/
public interface EventService {

    void addEvents(List<Event> events);

    void deleteEvents(String repo_id,String category);

    Object getCurrentEvents(String userToken,String category);

    Object hasNewEvents(String userToken,String category);
}
