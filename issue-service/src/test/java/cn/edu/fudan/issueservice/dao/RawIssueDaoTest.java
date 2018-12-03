package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.IssueServiceApplicationTests;
import cn.edu.fudan.issueservice.domain.RawIssue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@Ignore
public class RawIssueDaoTest extends IssueServiceApplicationTests {

    @Autowired
    RawIssueDao rawIssueDao;

    RawIssue rawIssue1;
    RawIssue rawIssue2;
    List<RawIssue> list;

    @Before
    public void setup() throws Exception {
        rawIssue1 = new RawIssue();
        rawIssue1.setUuid("111");
        rawIssue1.setType("OBL_UNSATISFIED_OBLIGATION");
        rawIssue1.setDetail("{\"type\":\"OBL_UNSATISFIED_OBLIGATION\",\"priority\":\"2\",\"rank\":\"20\",\"abbrev\":\"OBL\",\"category\":\"EXPERIMENTAL\"}");
        rawIssue1.setFile_name("DatabaseTool.java");
        rawIssue1.setScan_id("f012de17-318e-4bef-9f8c-9a263bbece6b");
        rawIssue1.setIssue_id("fe491c9a-4fd1-48d8-a577-057ce3c93a34");
        rawIssue1.setCommit_id("94628087eaf6c81584223617d287c26af2116a96");

        rawIssue2 = new RawIssue();
        rawIssue2.setUuid("222");
        rawIssue2.setType("OBL_UNSATISFIED_OBLIGATION");
        rawIssue2.setDetail("{\"type\":\"OBL_UNSATISFIED_OBLIGATION\",\"priority\":\"2\",\"rank\":\"20\",\"abbrev\":\"OBL\",\"category\":\"EXPERIMENTAL\"}");
        rawIssue2.setFile_name("DatabaseTool.java");
        rawIssue2.setScan_id("f012de17-318e-4bef-9f8c-9a263bbece6b");
        rawIssue2.setIssue_id("fe491c9a-4fd1-48d8-a577-057ce3c93a34");
        rawIssue2.setCommit_id("94628087eaf6c81584223617d287c26af2116a96");

        list = new ArrayList<RawIssue>();
        list.add(rawIssue1);
        list.add(rawIssue2);
    }

    @Test
    @Transactional
    public void insertRawIssueList() {
        rawIssueDao.insertRawIssueList(list);
    }

    @Test
    @Transactional
    public void deleteRawIssueByProjectId() {
        rawIssueDao.deleteRawIssueByRepoIdAndCategory("a585c7d8-e8a9-47c9-878d-761f8bfaaf62","bug");
    }

    @Test
    @Transactional
    public void batchUpdateIssueId() {
        rawIssueDao.batchUpdateIssueId(list);
    }

    @Test
    public void getRawIssueByCommitID() {
        List<RawIssue> list2 = rawIssueDao.getRawIssueByCommitIDAndCategory("94628087eaf6c81584223617d287c26af2116a96","bug");
        for (RawIssue rawIssue : list2) {
            System.out.println(rawIssue.getUuid());
        }
    }

    @Test
    public void getRawIssueByIssueId() {
        List<RawIssue> list3 = rawIssueDao.getRawIssueByIssueId("fe491c9a-4fd1-48d8-a577-057ce3c93a34");
        for (RawIssue rawIssue : list3) {
            System.out.println(rawIssue.getUuid());
        }
    }
}