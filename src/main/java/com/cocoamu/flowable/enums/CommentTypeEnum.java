package com.cocoamu.flowable.enums;

import lombok.Getter;

/**
 * 审批意见类型枚举类
 * @@author key
 */
@Getter
public enum CommentTypeEnum {
    START_FLOW(0),
    START_TASK(1),
    PASS(2),
    END(99);

    private int code;
    CommentTypeEnum(int code) {
        this.code = code;
    }
}
