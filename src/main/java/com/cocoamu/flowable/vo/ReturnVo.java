package com.cocoamu.flowable.vo;

import com.cocoamu.flowable.enums.ReturnCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class ReturnVo<T> implements Serializable {

    private ReturnCode returnCode;

    private T data;

    public ReturnCode getReturnCode() {
        return returnCode;
    }

    public T getData() {
        return data;
    }

    public ReturnVo(ReturnCode returnCode, T data) {
        this.returnCode = returnCode;
        this.data = data;
    }
}
