package cn.edu.fudan.tagservice.domain;

public class Tag {
    private String uuid;
    private String name;
    private String scope;
    private String color;
    private String icon;

    public Tag() {
    }

    public Tag(String uuid, String name, String scope, String color) {
        this.uuid = uuid;
        this.name = name;
        this.scope = scope;
        this.color = color;
        this.icon = null;
    }

    public Tag(String uuid, String name, String scope, String color, String icon) {
        this.uuid = uuid;
        this.name = name;
        this.scope = scope;
        this.color = color;
        this.icon = icon;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
