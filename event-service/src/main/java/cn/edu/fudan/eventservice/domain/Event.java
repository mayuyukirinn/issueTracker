package cn.edu.fudan.eventservice.domain;

import java.util.Date;

/**
 * @author WZY
 * @version 1.0
 **/
public class Event {

    private String id;
    private String category;
    private EventType eventType;
    private String targetType;
    private String targetId;
    private String targetCommitter;
    private String repoId;
    private Date createTime;

    public Event() {
    }

    public Event(String id, String category, EventType eventType, String targetType, String targetId, String targetCommitter, String repoId, Date createTime) {
        this.id = id;
        this.category = category;
        this.eventType = eventType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetCommitter = targetCommitter;
        this.repoId = repoId;
        this.createTime = createTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetCommitter() {
        return targetCommitter;
    }

    public void setTargetCommitter(String targetCommitter) {
        this.targetCommitter = targetCommitter;
    }

    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
