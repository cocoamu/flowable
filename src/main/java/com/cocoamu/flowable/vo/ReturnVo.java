package com.cocoamu.flowable.vo;

import com.cocoamu.flowable.enums.ReturnCodeEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class ReturnVo<T> implements Serializable {

    private ReturnCodeEnum returnCode;

    private T data;

    public static <T> ReturnVo sucess(T data) {
        ReturnVo<T> response = new ReturnVo<>();
        response.returnCode = ReturnCodeEnum.SUCCESS;
        response.data = data;
        return response;
    }

    public static <T> ReturnVo fail(T data) {
        ReturnVo<T> response = new ReturnVo<>();
        response.returnCode = ReturnCodeEnum.FAIL;
        response.data = data;
        return response;
    }
}
