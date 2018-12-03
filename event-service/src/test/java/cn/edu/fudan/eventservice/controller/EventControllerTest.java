package cn.edu.fudan.eventservice.controller;

import cn.edu.fudan.eventservice.EventServiceApplicationTests;
import cn.edu.fudan.eventservice.domain.Event;
import cn.edu.fudan.eventservice.domain.EventType;
import cn.edu.fudan.eventservice.domain.ResponseBean;
import cn.edu.fudan.eventservice.service.EventService;
import cn.edu.fudan.eventservice.service.impl.EventServiceImpl;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

/**
 * @author WZY
 * @version 1.0
 **/

@PrepareForTest({EventController.class, EventService.class, EventServiceImpl.class})
@WebAppConfiguration
public class EventControllerTest extends EventServiceApplicationTests {


    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private EventController eventController;

    private EventService eventService;

    private MockMvc mockMvc;

    private List<Event> testEvents;

    private List<Event> newBugEvents;

    private List<Event> eliminatedBugEvents;

    @Before
    public void setUp() throws IllegalAccessException {
        eventService= Mockito.mock(EventService.class);
        mockMvc= MockMvcBuilders.webAppContextSetup(context).build();
        MemberModifier.field(EventController.class,"eventService").set(eventController,eventService);

        //准备测试数据
        testEvents=new ArrayList<>();
        testEvents.add(new Event("1","bug", EventType.NEW_BUG,"BIG_METHOD","2222","Jack","1",new Date()));
        testEvents.add(new Event("2","bug", EventType.ELIMINATE_BUG,"11","2222","Smith","1",new Date()));

        newBugEvents=new ArrayList<>();
        newBugEvents.add(new Event("3","bug", EventType.NEW_BUG,"BIG_METHOD","2222","Jack","1",new Date()));
        newBugEvents.add(new Event("4","bug", EventType.NEW_BUG,"BIG_METHOD","2222","Jack","1",new Date()));

        eliminatedBugEvents=new ArrayList<>();
        eliminatedBugEvents.add(new Event("5","bug", EventType.ELIMINATE_BUG,"BIG_METHOD","2222","Jack","1",new Date()));
        eliminatedBugEvents.add(new Event("6","bug", EventType.ELIMINATE_BUG,"BIG_METHOD","2222","Jack","1",new Date()));

    }

    @Test
    public void addEventsSuccess() throws Exception {

        String requestJson=mapper.writer().withDefaultPrettyPrinter().writeValueAsString(testEvents);

        PowerMockito.doNothing().when(eventService,"addEvents", ArgumentMatchers.anyIterable());

        ResponseBean expected=new ResponseBean(200,"event add success!",null);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/event")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
        ).andReturn();
        ResponseBean testResult= JSONObject.parseObject(result.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(expected.getCode(),testResult.getCode());
    }

    @Test
    public void addEventsFailed() throws Exception {
        String requestJson=mapper.writer().withDefaultPrettyPrinter().writeValueAsString(testEvents);

        PowerMockito.when(eventService,"addEvents", ArgumentMatchers.anyIterable()).thenThrow(new RuntimeException());

        ResponseBean expected=new ResponseBean(401,"event add failed!",null);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/event")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
        ).andReturn();
        ResponseBean testResult= JSONObject.parseObject(result.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(expected.getCode(),testResult.getCode());
    }


    @Test
    @SuppressWarnings("unchecked")
    public void eventHasNew()throws Exception{
        String token="ec15d79e36e14dd258cfff3d48b73d35";
        String category="bug";
        Map<String,Object> response=new HashMap<>();
        response.put("hasNew",true);
        PowerMockito.when(eventService.hasNewEvents(token,category)).thenReturn(response);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/event/has-new")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
        ).andReturn();
        Map<String,Object> testResult=JSONObject.parseObject(result.getResponse().getContentAsString(),Map.class);
        Assert.assertEquals(response.get("hasNew"),testResult.get("hasNew"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void eventHasNewWithOutToken(){
        try{
             mockMvc.perform(MockMvcRequestBuilders.get("/event/has-new")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .header("token", null)
            ).andReturn();
        }catch (Exception e){
            Assert.assertTrue(e instanceof RuntimeException);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getCurrentsEventsSuccess() throws Exception {
        Map<String,Object> response=new HashMap<>();
        response.put("newBug",newBugEvents);
        response.put("eliminateBug",eliminatedBugEvents);
        String token="ec15d79e36e14dd258cfff3d48b73d35";
        String category="bug";
        PowerMockito.when(eventService.getCurrentEvents(token,category)).thenReturn(response);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/event/current-events")
                .param("category",category)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
        ).andReturn();
        JSONObject testResult=JSONObject.parseObject(result.getResponse().getContentAsString());
        for(Map.Entry<String,Object> entry:testResult.entrySet()){
            List<Event> expectedList=(List<Event>)response.get(entry.getKey());
            JSONArray exactList=(JSONArray)entry.getValue();
            Assert.assertEquals(expectedList.size(),exactList.size());
            for(int i=0;i<expectedList.size();i++){
                Assert.assertEquals(expectedList.get(i).getId(),exactList.getJSONObject(i).getString("id"));
            }

        }
    }

    @Test
    public void getCurrentEventsWithOutToken(){
        try{
            mockMvc.perform(MockMvcRequestBuilders.get("/event/current-events")
                    .param("category","bug")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .header("token", null)
            ).andReturn();
        }catch (Exception e){
            Assert.assertTrue(e instanceof RuntimeException);
        }
    }

    @Test
    public void deleteEventsSuccess() throws Exception {
        String repoId="11111";
        String category ="bug";
        PowerMockito.doNothing().when(eventService,"deleteEvents", ArgumentMatchers.anyString(),ArgumentMatchers.anyString());

        ResponseBean expected=new ResponseBean(200,"event add success!",null);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/inner/event/"+category+"/"+repoId)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andReturn();
        ResponseBean testResult= JSONObject.parseObject(result.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(expected.getCode(),testResult.getCode());
    }

    @Test
    public void deleteEventsFailed()throws Exception{
        String repoId="11111";
        String category ="bug";
        PowerMockito.when(eventService,"deleteEvents", ArgumentMatchers.anyString(),ArgumentMatchers.anyString()).thenThrow(new RuntimeException());

        ResponseBean expected=new ResponseBean(401,"event add success!",null);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/inner/event/"+category+"/"+repoId)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andReturn();
        ResponseBean testResult= JSONObject.parseObject(result.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(expected.getCode(),testResult.getCode());
    }

}
