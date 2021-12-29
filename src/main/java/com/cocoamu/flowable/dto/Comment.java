package com.cocoamu.flowable.dto;

import lombok.Data;

import java.sql.Date;


@Data
public class Comment {

    /**
     * 流程实例id
     */
    private String processInstanceId;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 评论类型
     */
    private int type;

    /**
     * 评论内容
     */
    private String message;

    /**
     * 添加人
     */
    protected String userId;

    /**
     * 评论时间
     */
    private Date time;
}
