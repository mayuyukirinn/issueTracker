package cn.edu.fudan.tagservice.domain;

import java.io.Serializable;

/**
 * @author WZY
 * @version 1.0
 **/
public class ResponseBean implements Serializable {

    private int code;

    private String msg;

    private Object data;

    public ResponseBean() {
    }

    public ResponseBean(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String toString() {
        return "code : " + code + "; msg : " + msg + "; data :" + data;
    }
}
