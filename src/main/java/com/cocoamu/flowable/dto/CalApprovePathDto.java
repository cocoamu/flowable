package com.cocoamu.flowable.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CalApprovePathDto {
    private String processInstanceId;
    private String modelId;
    Map<String,Object> params;
}
