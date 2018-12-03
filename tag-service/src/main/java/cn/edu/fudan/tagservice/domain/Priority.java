package cn.edu.fudan.tagservice.domain;

import java.util.HashMap;
import java.util.Map;

public enum Priority {
    Immediate("#b52a28",7),
    Urgent("#e36e6c",6),
    High("#eba47c",5),
    Normal("#ebcb8d",4),
    Low("#a0b08f",3),
    Ignore("#628e8b",2),
    Solved("#d3d3d3",1),
    Misinformation("#5499bd",0);

    private String color;
    private int level;
    private static Map<String, Priority> valueMap = new HashMap<>();

    static {
        for (Priority priority : Priority.values()) {
            valueMap.put(priority.toString(), priority);
        }
    }

    Priority(String color,int level) {
        this.color = color;
        this.level=level;
    }

    public static boolean contains(String name) {
        return valueMap.keySet().contains(name);
    }

    public String getColor() {
        return color;
    }

    public int getLevel() {
        return level;
    }

    public static Priority getByValue(String value) {
        Priority result = valueMap.get(value);
        if (result == null) {
            throw new IllegalArgumentException("No element matches " + value);
        }
        return result;
    }

}
