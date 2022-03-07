package com.cocoamu.flowable.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CalApprovePathDto {
    /**
     * 流程定义id
     */
    private String processId;
    /**
     * 环境变量及对应的值
     */
    private Map<String,Object> params;
    /**
     * 过滤范围 (可能经过的有5个人，但只要返回在这个范围内的即可,为空则返回所有预测的节点)
     */
    private List<String> approveIds;
}
