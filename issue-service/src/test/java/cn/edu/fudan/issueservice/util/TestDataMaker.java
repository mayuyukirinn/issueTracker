package cn.edu.fudan.issueservice.util;

import cn.edu.fudan.issueservice.domain.Issue;
import cn.edu.fudan.issueservice.domain.Location;
import cn.edu.fudan.issueservice.domain.RawIssue;

import java.util.ArrayList;
import java.util.List;

public class TestDataMaker {

    private List<Location> locationsMaker1(){
        List<Location> locations = new ArrayList<>();
        Location location = locationMaker1();
        locations.add(location);
        return locations;
    }

    private List<Location> locationsMaker2(){
        List<Location> locations = new ArrayList<>();
        Location location = locationMaker2();
        locations.add(location);
        return locations;
    }


    public Issue issueMaker1(){
        Issue issue = new Issue();
        issue.setUuid("iss1");
        issue.setType("OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE");
        issue.setStart_commit("badfd40225cb3208106ec30c403928fd55f84886");
        issue.setEnd_commit("badfd40225cb3208106ec30c403928fd55f84886");
        issue.setRaw_issue_start("10cfe678-8606-41cb-abf4-fda25292cc2b");
        issue.setRaw_issue_end("10cfe678-8606-41cb-abf4-fda25292cc2b");
        issue.setRepo_id("repo_id");
        issue.setTarget_files("DatabaseTool.java");
        return issue;
    }

    public Issue issueMaker2(){
        Issue issue = new Issue();
        issue.setUuid("iss2");
        issue.setType("OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE");
        issue.setStart_commit("badfd40225cb3208106ec30c403928fd55f84886");
        issue.setEnd_commit("badfd40225cb3208106ec30c403928fd55f84886");
        issue.setRaw_issue_start("c5b30c01-86bd-4a66-a828-0d78688fb005");
        issue.setRaw_issue_end("c5b30c01-86bd-4a66-a828-0d78688fb005");
        issue.setRepo_id("repo_id");
        issue.setTarget_files("DatabaseTool.java");
        return issue;
    }

    public RawIssue rawIssueMaker1(){
        RawIssue rawIssue = new RawIssue();
        rawIssue.setUuid("raw1");
        rawIssue.setType("OBL_UNSATISFIED_OBLIGATION");
        rawIssue.setDetail("{\"type\":\"OBL_UNSATISFIED_OBLIGATION\",\"priority\":\"2\",\"rank\":\"20\",\"abbrev\":\"OBL\",\"category\":\"EXPERIMENTAL\"}");
        rawIssue.setFile_name("DatabaseTool.java");
        rawIssue.setScan_id("scan1");
        rawIssue.setIssue_id("iss1");
        rawIssue.setCommit_id("comm1");
        rawIssue.setCategory("category");
        rawIssue.setLocations(locationsMaker1());
        return rawIssue;
    }

    public RawIssue rawIssueMaker2(){
        RawIssue rawIssue = new RawIssue();
        rawIssue.setUuid("raw2");
        rawIssue.setType("OBL_UNSATISFIED_OBLIGATION");
        rawIssue.setDetail("{\"type\":\"OBL_UNSATISFIED_OBLIGATION\",\"priority\":\"2\",\"rank\":\"20\",\"abbrev\":\"OBL\",\"category\":\"EXPERIMENTAL\"}");
        rawIssue.setFile_name("DatabaseTool.java");
        rawIssue.setScan_id("scan2");
        rawIssue.setIssue_id("iss2");
        rawIssue.setCommit_id("comm2");
        rawIssue.setCategory("category");
        rawIssue.setLocations(locationsMaker2());
        return rawIssue;
    }

    public RawIssue rawIssueMaker3(){
        RawIssue rawIssue = new RawIssue();
        rawIssue.setUuid("raw3");
        rawIssue.setType("OBL_UNSATISFIED_OBLIGATION");
        rawIssue.setDetail("{\"type\":\"OBL_UNSATISFIED_OBLIGATION\",\"priority\":\"2\",\"rank\":\"20\",\"abbrev\":\"OBL\",\"category\":\"EXPERIMENTAL\"}");
        rawIssue.setFile_name("Tool.java");
        rawIssue.setScan_id("scan2");
        rawIssue.setIssue_id("iss2");
        rawIssue.setCommit_id("comm2");
        rawIssue.setCategory("category");
        rawIssue.setLocations(locationsMaker2());
        return rawIssue;
    }

    public Location locationMaker1(){
        Location location = new Location();
        location.setUuid("lo1");
        location.setStart_line(160);
        location.setEnd_line(169);
        location.setBug_lines("162,163,164");
        location.setFile_path("github/ccran/WebMagicForBlog/src/main/java/com/ccran/tools/DatabaseTool.java");
        location.setClass_name("com.ccran.tools.DatabaseTool");
        location.setMethod_name("existCnblogAuthorItem");
        location.setRawIssue_id("raw1");
        location.setCode("xxxxxxx");
        return  location;
    }

    public Location locationMaker2(){
        Location location = new Location();
        location.setUuid("lo2");
        location.setStart_line(160);
        location.setEnd_line(169);
        location.setBug_lines("162,163,164");
        location.setFile_path("github/ccran/WebMagicForBlog/src/main/java/com/ccran/tools/DatabaseTool.java");
        location.setClass_name("com.ccran.tools.DatabaseTool");
        location.setMethod_name("existCnblogAuthorItem");
        location.setRawIssue_id("raw2");
        location.setCode("xxxxxxx");
        return  location;
    }
}
