package cn.edu.fudan.scanservice.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
public class ExcuteShellUtil {

    @Value("${workHome}")
    private String workHome;

    @Value("${repoHome}")
    private String repoHome;

    public  boolean excuteAnalyse(String repoPath, String projectName) {
        try {
            Runtime rt = Runtime.getRuntime();
            //String findbugs = "findbugs -xml -output  /home/fdse/issueTracker/resultfile/" + projectName
            //		+ ".xml " + repoPath;
            //脚本实现 用来解耦 还需要与tool解耦合
            Process process = rt.exec(workHome+"excuteTools.sh " + projectName + " " + repoPath);
            //Process process = rt.exec(findbugs, null, null);
            int exitValue = process.waitFor();
            if (exitValue == 0)
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public  boolean excuteMvn(String repoPath) {
        try {
            Runtime rt = Runtime.getRuntime();
            Process process = rt.exec(workHome+"excuteMvn.sh " + repoPath);
            int exitValue = process.waitFor();
            if (exitValue == 0)
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //do not include/consider the situation that there are at least files holding the same name in project-home dir
    public  String getFileLocation(String repoPath, String fileName) {
        try {
            Runtime rt = Runtime.getRuntime();
            Process process = rt.exec("find "+repoHome + repoPath + " -name " + fileName);
            process.waitFor();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuffer sBuffer = new StringBuffer();
            String line ;
            while ((line = bReader.readLine())!= null) {
                line = line.replace(repoHome, "");
                sBuffer.append(line).append(":");
            }
            return  sBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
