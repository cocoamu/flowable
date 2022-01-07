package com.cocoamu.flowable.dto;

import lombok.Data;

/**
 * 任务dto
 */
@Data
public class TaskDto {
    //流程实例id
    String processInstanceId;

    //任务id
    String taskId;

    //受理人
    String assignee;
}
