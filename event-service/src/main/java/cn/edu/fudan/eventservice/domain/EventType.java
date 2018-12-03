package cn.edu.fudan.eventservice.domain;

import java.util.ArrayList;
import java.util.List;

public enum EventType {

    NEW_BUG("add a bug"),
    ELIMINATE_BUG("eliminate a bug"),
    NEW_CLONE_CLASS("add a clone class"),
    REMOVE_CLONE_CLASS("remove a clone class"),
    NEW_CLONE_INSTANCE("add a clone instance"),
    REMOVE_CLONE_INSTANCE("remove a clone instance");

    private String description;

    EventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static List<EventType> bugEventTypeList=new ArrayList<>();
    public static List<EventType> cloneEventTypeList=new ArrayList<>();

    static{
        bugEventTypeList.add(EventType.NEW_BUG);
        bugEventTypeList.add(EventType.ELIMINATE_BUG);

        cloneEventTypeList.add(EventType.NEW_CLONE_CLASS);
        cloneEventTypeList.add(EventType.REMOVE_CLONE_CLASS);
        cloneEventTypeList.add(EventType.NEW_CLONE_INSTANCE);
        cloneEventTypeList.add(EventType.REMOVE_CLONE_INSTANCE);
    }
}
