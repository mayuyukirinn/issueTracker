package cn.edu.fudan.issueservice.domain;

public class Location {

    private String uuid;
    private int start_line;
    private int end_line;
    private String bug_lines;
    private int start_token;
    private int end_token;
    private String file_path;
    private String class_name;
    private String method_name;
    private String rawIssue_id;
    private String code;


    public Location() {
    }

    public Location(String uuid, int start_line, int end_line, String bug_lines, String file_path, String class_name, String method_name, String rawIssue_id, String code) {
        this.uuid = uuid;
        this.start_line = start_line;
        this.end_line = end_line;
        this.bug_lines = bug_lines;
        this.file_path = file_path;
        this.class_name = class_name;
        this.method_name = method_name;
        this.rawIssue_id = rawIssue_id;
        this.code = code;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public int getStart_line() {
        return start_line;
    }

    public void setStart_line(int start_line) {
        this.start_line = start_line;
    }

    public int getEnd_line() {
        return end_line;
    }

    public void setEnd_line(int end_line) {
        this.end_line = end_line;
    }

    public String getBug_lines() {
        return bug_lines;
    }

    public void setBug_lines(String bug_lines) {
        this.bug_lines = bug_lines;
    }

    public int getStart_token() {
        return start_token;
    }

    public void setStart_token(int start_token) {
        this.start_token = start_token;
    }

    public int getEnd_token() {
        return end_token;
    }

    public void setEnd_token(int end_token) {
        this.end_token = end_token;
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public String getMethod_name() {
        return method_name;
    }

    public void setMethod_name(String method_name) {
        this.method_name = method_name;
    }

    public String getRawIssue_id() {
        return rawIssue_id;
    }

    public void setRawIssue_id(String rawIssue_id) {
        this.rawIssue_id = rawIssue_id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Location)) return false;
        Location location = (Location) obj;
        if(this.class_name!=null&&location.class_name!=null&&this.method_name!=null&&location.method_name!=null)
            return location.class_name.equals(class_name) &&
                    location.method_name.equals(method_name) &&
                    location.file_path.equals(file_path);
        else
            return location.file_path.equals(file_path);
    }
}
