package com.cocoamu.flowable.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 任务dto
 */
@Data
public class UpdateElementDto implements Serializable {
    private String elementId;
    private String elementAttr;
}
