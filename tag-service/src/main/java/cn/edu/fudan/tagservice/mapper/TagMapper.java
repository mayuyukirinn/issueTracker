package cn.edu.fudan.tagservice.mapper;

import cn.edu.fudan.tagservice.domain.Tag;
import cn.edu.fudan.tagservice.domain.TaggedItem;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagMapper {

    void addOneTag(Tag tag);

    void addOneTaggedItem(@Param("item_id") String item_id, @Param("tag_id") String tag_id);

    void addMultiTaggedItem(List<TaggedItem> list);

    void modifyMultiTaggedItem(List<TaggedItem> list);

    String getUuidByNameAndScope(@Param("name") String name, @Param("scope") String scope);

    void deleteOneTag(@Param("tag_id") String tagId);

    void deleteOneTagged(@Param("tag_id") String tagId, @Param("item_id") String itemId);

    void deleteTaggeds(List<String> list);

    void modifyOneTagged(@Param("old_tag_id") String oldTagId, @Param("new_tag_id") String newTagId, @Param("item_id") String itemId);

    void modifyOneTag(@Param("tag_id") String tagId, @Param("name") String name);

    Integer hasBeenTagged(@Param("tag_id") String tag_id, @Param("item_id") String item_id);

    List<Tag> getTagsByItemId(@Param("item_id") String item_id);

    List<Tag> getAllDefaultTags();

    List<String> getItemIdsByTagIds(List<String> list);

    void deleteTagByProjectId(@Param("project_id") String projectId);
}
