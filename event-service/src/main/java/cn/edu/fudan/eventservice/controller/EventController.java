package cn.edu.fudan.eventservice.controller;

import cn.edu.fudan.eventservice.domain.Event;
import cn.edu.fudan.eventservice.domain.ResponseBean;
import cn.edu.fudan.eventservice.service.EventService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author WZY
 * @version 1.0
 **/
@RestController
public class EventController {

    private EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/event")
    public Object addEvents(@RequestBody List<Event> events){
        try {
            eventService.addEvents(events);
            return new ResponseBean(200, "event add success!", null);
        } catch (Exception e) {
            return new ResponseBean(401, "event add failed!", null);
        }
    }

    @GetMapping("/event/has-new")
    @CrossOrigin
    public Object eventHasNew(HttpServletRequest request,@RequestParam(name="category",defaultValue = "bug")String category){
        String token=request.getHeader("token");
        if(token==null)
            throw new RuntimeException("need user token!");
        return eventService.hasNewEvents(token,category);
    }

    @GetMapping("/event/current-events")
    @CrossOrigin
    public Object getCurrentEvents(HttpServletRequest request,@RequestParam(name="category",defaultValue = "bug")String category){
        String token=request.getHeader("token");
        if(token==null)
            throw new RuntimeException("need user token!");
        return eventService.getCurrentEvents(token,category);
    }

    @DeleteMapping("/inner/event/{category}/{repoId}")
    public Object deleteEvents(@PathVariable("category")String category,@PathVariable("repoId")String repoId){
        try {
            eventService.deleteEvents(repoId,category);
            return new ResponseBean(200, "event delete success!", null);
        } catch (Exception e) {
            return new ResponseBean(401, "event delete failed!", null);
        }
    }
}
