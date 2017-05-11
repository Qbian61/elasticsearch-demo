package com.qbian.common.dto;

/**
 * Created by Qbian on 2017/5/11.
 */
public class ResponseDto<T> {

    private Integer code;

    private String msg;

    private T data;

    public ResponseDto() {
        this.code = 0;
        this.msg = "ok";
    }

    public ResponseDto(T data) {
        this.code = 0;
        this.msg = "ok";
        this.data = data;
    }

    public ResponseDto(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
