package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.IssueServiceApplicationTests;
import cn.edu.fudan.issueservice.domain.Location;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@Ignore
public class LocationDaoTest extends IssueServiceApplicationTests {

    @Autowired
    LocationDao locationDao;

    Location location1;
    Location location2;
    List<Location> list;

    @Before
    public void setup() throws Exception {
        location1 = new Location();
        location1.setUuid("111");
        location1.setStart_line(160);
        location1.setEnd_line(169);
        location1.setBug_lines("162,163,164");
        location1.setFile_path("github/ccran/WebMagicForBlog/src/main/java/com/ccran/tools/DatabaseTool.java");
        location1.setClass_name("com.ccran.tools.DatabaseTool");
        location1.setMethod_name("existCnblogAuthorItem");
        location1.setRawIssue_id("ec76e7ff-33c7-46ea-899b-7f24f6b51d3c");
        location1.setCode("xxxxxxx");

        location2 = new Location();
        location2.setUuid("222");
        location2.setStart_line(373);
        location2.setEnd_line(389);
        location2.setBug_lines("376,377,388,389");
        location2.setFile_path("github/ccran/WebMagicForBlog/src/main/java/com/ccran/tools/DatabaseTool.java");
        location2.setClass_name("com.ccran.tools.DatabaseTool");
        location2.setMethod_name("getIPProxyItemList");
        location2.setRawIssue_id("14ddd901-ff11-4240-baf4-6495b6eb3f95");
        location2.setCode("xxxxxxx");

        list = new ArrayList<Location>();
        list.add(location1);
        list.add(location2);
    }

    @Test
    @Transactional
    public void insertLocationList() {
        locationDao.insertLocationList(list);
    }

    @Test
    @Transactional
    public void deleteLocationByProjectId() {
        locationDao.deleteLocationByRepoIdAndCategory("9151ecba-e749-4a14-b6e3-f3a1388139ec","bug");
    }

    @Test
    public void getLocations() {
        List<Location> listLocation = locationDao.getLocations("ec76e7ff-33c7-46ea-899b-7f24f6b51d3c");
        for (Location location : listLocation) {
            System.out.println(location.getUuid());
        }
    }
}