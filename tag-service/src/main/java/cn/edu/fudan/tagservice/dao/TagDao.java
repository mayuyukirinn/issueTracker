package cn.edu.fudan.tagservice.dao;

import cn.edu.fudan.tagservice.domain.Tag;
import cn.edu.fudan.tagservice.domain.TaggedItem;
import cn.edu.fudan.tagservice.mapper.TagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author WZY
 * @version 1.0
 **/
@Component
public class TagDao {

    private TagMapper tagMapper;

    @Autowired
    public void setTagMapper(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    public void addOneTag(Tag tag) {
        tagMapper.addOneTag(tag);
    }

    public void addOneTaggedItem(String item_id, String tag_id) {
        tagMapper.addOneTaggedItem(item_id, tag_id);
    }

    public void addMultiTaggedItem(List<TaggedItem> list) {
        tagMapper.addMultiTaggedItem(list);
    }

    public String getUuidByNameAndScope(String name, String scope) {
        return tagMapper.getUuidByNameAndScope(name, scope);
    }

    public void deleteOneTag(String tagId) {
        tagMapper.deleteOneTag(tagId);
    }

    public void deleteOneTagged(String tagId, String itemId) {
        tagMapper.deleteOneTagged(tagId, itemId);
    }

    public void deleteTaggeds(List<String> itemIds) {
        tagMapper.deleteTaggeds(itemIds);
    }

    public void modifyOneTag(String tagId, String name) {
        tagMapper.modifyOneTag(tagId, name);
    }

    public void modifyOneTagged(String oldTagId, String newTagId, String iteamId) {
        tagMapper.modifyOneTagged(oldTagId, newTagId, iteamId);
    }

    public void modifyMultiTaggedItem(List<TaggedItem> list){
        tagMapper.modifyMultiTaggedItem(list);
    }

    public List<Tag> getTagsByItemId(String item_id) {
        return tagMapper.getTagsByItemId(item_id);
    }

    public List<String> getItemIdsByTagIds(List<String> tagIds) {
        return tagMapper.getItemIdsByTagIds(tagIds);
    }

    public List<Tag> getAllDefaultTags() {
        return tagMapper.getAllDefaultTags();
    }

    public Integer hasBeenTagged(String tag_id, String item_id) {
        return tagMapper.hasBeenTagged(tag_id, item_id);
    }

    public void deleteTagByProjectId(String projectId) {
        tagMapper.deleteTagByProjectId(projectId);
    }
}
