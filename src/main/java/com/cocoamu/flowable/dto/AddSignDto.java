package com.cocoamu.flowable.dto;

import lombok.Data;

/**
 * 加签前端参数
 */
@Data
public class AddSignDto {
    //当前操作加签的节点
    String taskId;

    //当前节点受理人
    String userId;

    //加签节点标题
    String taskName;

    //加签节点受理人(表达式)
    String assignee;

    //加签类型 0前加签 1后加签
    Integer addType;
}
