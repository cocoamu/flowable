package com.cocoamu.flowable.service;

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
     */
    void completeTask(String taskId, Integer approved);

    /**
     * 多人会签审批
     * @param taskId 任务id
     * @param approved 审批结果 0通过 1拒绝
     */
    void mutileComplate(String taskId, Integer approved);

    /**
     * 前加签
     * @param taskId 当前任务id
     * @param taskName 加签节点名称
     * @param assignee 受理人
     */
    void beforeAddSignTask(String taskId,String taskName,String assignee);

    /**
     * 后加签
     * @param taskId 当前任务id
     * @param taskName 加签节点名称
     * @param assignee 受理人
     */
    void afterAddSignTask(String taskId,String taskName,String assignee);
}
