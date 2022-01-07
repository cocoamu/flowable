package com.cocoamu.flowable.service;

import com.cocoamu.flowable.dto.AddSignDto;
import com.cocoamu.flowable.vo.ReturnVo;

import java.util.List;
import java.util.Map;

public interface MyTaskService {
    /**
     * 根据流程实例id获取任务列表
     * @param processId 流程实例id
     * @return
     */
    List<Map<String,Object>> getTaskByPid(String processId);

    /**
     * 获取某个用户的待办任务列表
     * @param assignee  办理人
     * @return
     */
    List<Map<String,Object>> getTaskByAssignee(String assignee);

    /**
     * 任务审批
     * @param taskId 任务id
     * @param approved 审批结果 0通过 1拒绝
     * @param comment 审批意见
     */
    void complete(String taskId, Integer approved,String comment);

    /**
     * 多人会签审批
     * @param taskId 任务id
     * @param approved 审批结果 0通过 1拒绝
     */
    void mutileComplate(String taskId, Integer approved);

    /**
     * 加签
     * @param addSignVo
     */
    ReturnVo addSignTask(AddSignDto addSignVo);
}
