package cn.edu.fudan.tagservice.domain;

/**
 * @author WZY
 * @version 1.0
 **/
public class TaggedItem {

    private String item_id;
    private String tag_id;

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getTag_id() {
        return tag_id;
    }

    public void setTag_id(String tag_id) {
        this.tag_id = tag_id;
    }
}
