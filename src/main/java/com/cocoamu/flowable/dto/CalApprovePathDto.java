package com.cocoamu.flowable.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CalApprovePathDto {
    private String processId;
    Map<String,Object> params;
    List<String> approveIds;
}
