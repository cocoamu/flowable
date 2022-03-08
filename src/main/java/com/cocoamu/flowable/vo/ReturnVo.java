package com.cocoamu.flowable.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReturnVo<T> implements Serializable {

    private Integer code;

    private String msg;

    private T data;

    public static <T> ReturnVo sucess(T data) {
        ReturnVo<T> response = new ReturnVo<>();
        response.code = 0;
        response.msg = "ok";
        response.data = data;
        return response;
    }

    public static <T> ReturnVo fail(T data) {
        ReturnVo<T> response = new ReturnVo<>();
        response.code = -1;
        response.msg = "fail";
        response.data = data;
        return response;
    }
}
