package com.cocoamu.flowable.dto;

import lombok.Data;

/**
 * 任务dto
 */
@Data
public class UpdateTaskDto {
    private String processId;
    private String elementId;
    private String elementAttr;
}
