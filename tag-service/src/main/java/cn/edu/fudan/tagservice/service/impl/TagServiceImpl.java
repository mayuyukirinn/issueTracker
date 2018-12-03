package cn.edu.fudan.tagservice.service.impl;

import cn.edu.fudan.tagservice.dao.TagDao;
import cn.edu.fudan.tagservice.domain.Priority;
import cn.edu.fudan.tagservice.domain.Tag;
import cn.edu.fudan.tagservice.domain.TaggedItem;
import cn.edu.fudan.tagservice.service.TagService;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * @author WZY
 * @version 1.0
 **/
@Service
public class TagServiceImpl implements TagService {

    private Logger logger = LoggerFactory.getLogger(TagServiceImpl.class);

    private TagDao tagDao;

    @Autowired
    public void setTagDao(TagDao tagDao) {
        this.tagDao = tagDao;
    }

    @Transactional
    @Override
    public void addTag(JSONObject requestBody) {
        String name = requestBody.getString("name");
        String scope = requestBody.getString("scope");
        String itemId = requestBody.getString("itemId");
        String tag_id;
        if (requestBody.getBoolean("isDefault")) {   // 默认的tag，不是自定义的
            tag_id = tagDao.getUuidByNameAndScope(name, scope);
            if (tag_id == null) {
                tag_id = UUID.randomUUID().toString();
                tagDao.addOneTag(new Tag(tag_id, name, scope, Priority.getByValue("name").getColor()));
            }
            logger.error(tagDao.hasBeenTagged(tag_id, itemId).toString());
            if (tagDao.hasBeenTagged(tag_id, itemId) > 0)
                throw new RuntimeException("duplicate tag!");
            tagDao.addOneTaggedItem(itemId, tag_id);
        } else {
            if (Priority.contains(name))
                throw new IllegalArgumentException("enter other tag" + name);
            tag_id = UUID.randomUUID().toString();
            tagDao.addOneTag(new Tag(tag_id, name, scope, "#ffffff"));
            tagDao.addOneTaggedItem(itemId, tag_id);
        }
    }

    @Override
    public void addMultiTaggedItem(List<TaggedItem> list) {
        tagDao.addMultiTaggedItem(list);
    }

    @Override
    public void modifyMultiTaggedItem(List<TaggedItem> list) {
        tagDao.modifyMultiTaggedItem(list);
    }

    @Override
    public void deleteTag(String tagId, String itemId) {
        //tagDao.deleteOneTag(tagId);
        tagDao.deleteOneTagged(tagId, itemId);
    }

    @Override
    public void deleteTaggeds(List<String> itemIds) {
        tagDao.deleteTaggeds(itemIds);
    }

    @Override
    public void modifyTag(JSONObject requestBody) {
        String name = requestBody.getString("name");
        String scope = requestBody.getString("scope");
        String itemId = requestBody.getString("itemId");
        String oldName = requestBody.getString("oldName");
        String oldTagId = tagDao.getUuidByNameAndScope(oldName, scope);
        String newTagId = tagDao.getUuidByNameAndScope(name, scope);
        if (requestBody.getBoolean("isDefault")) {
            if(name.equals("Not Tagged")){
                //取消某个issue的tag
               tagDao.deleteOneTagged(oldTagId,itemId);
            }else{
                tagDao.modifyOneTagged(oldTagId, newTagId, itemId);
            }
        } else {
            if (Priority.contains(name))
                throw new IllegalArgumentException("enter other tag" + name);
            tagDao.modifyOneTag(oldTagId, name);
        }

    }

    @Override
    public List<Tag> getTagsByItemId(String item_id) {
        return tagDao.getTagsByItemId(item_id);
    }

    @Override
    public List<String> getItemIdsByTagIds(List<String> tagIds) {
        if (tagIds == null || tagIds.size() == 0)
            return null;
        return tagDao.getItemIdsByTagIds(tagIds);
    }

    @Override
    public List<Tag> getAllDefaultTags() {
        List<Tag> tags= tagDao.getAllDefaultTags();
        tags.sort(Comparator.comparingInt((Tag tag)->Priority.getByValue(tag.getName()).getLevel()));
        return tags;
    }

    @Override
    public void deleteTagByProjectId(String projectId) {
        tagDao.deleteTagByProjectId(projectId);
    }
}
