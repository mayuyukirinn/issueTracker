package cn.edu.fudan.scanservice.util;

import cn.edu.fudan.scanservice.domain.Scan;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TestDataMaker {
    public Scan  scanMakerSc1(){
        Scan scan = new Scan();
        scan.setUuid("sc1");
        scan.setCategory("bug");
        scan.setName("name");
        Calendar myCalendar = new GregorianCalendar(2014, 2, 26,12,30);
        Date start_time = myCalendar.getTime();
        scan.setStart_time(start_time);
        myCalendar = new GregorianCalendar(2015, 1, 17,12,30);
        Date end_time = myCalendar.getTime();
        scan.setEnd_time(end_time);
        scan.setStatus("Scanned");
        scan.setResult_summary("summary");
        scan.setRepo_id("repo1");
        scan.setCommit_id("comm1");
        myCalendar = new GregorianCalendar(2013, 11, 19,12,30);
        Date commit_time = myCalendar.getTime();
        scan.setCommit_time(commit_time);
        return scan;
    }

    public Scan  scanMakerSc2(){
        Scan scan = new Scan();
        scan.setUuid("sc2");
        scan.setCategory("bug");
        scan.setName("name");
        Calendar myCalendar = new GregorianCalendar(2014, 2, 26,12,30);
        Date start_time = myCalendar.getTime();
        scan.setStart_time(start_time);
        myCalendar = new GregorianCalendar(2015, 1, 17,12,30);
        Date end_time = myCalendar.getTime();
        scan.setEnd_time(end_time);
        scan.setStatus("Scanned");
        scan.setResult_summary("summary");
        scan.setRepo_id("repo1");
        scan.setCommit_id("comm2");
        myCalendar = new GregorianCalendar(2013, 11, 19,12,30);
        Date commit_time = myCalendar.getTime();
        scan.setCommit_time(commit_time);
        return scan;
    }

}
