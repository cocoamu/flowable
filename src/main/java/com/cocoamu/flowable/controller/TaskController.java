package com.cocoamu.flowable.controller;

import com.cocoamu.flowable.service.MyTaskService;
import com.cocoamu.flowable.vo.ReturnVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/task")
public class TaskController {
    @Autowired
    private MyTaskService myTaskService;

    /**
     * 根据流程实例id查询任务列表
     *
     * @param processId 流程实例id
     * @return
     */
    @RequestMapping(value = "/getTaskByPid")
    public List<Map<String, Object>> getTaskByPid(String processId) {
        return myTaskService.getTaskByPid(processId);
    }

    /**
     * 查询某个用户的任务列表
     *
     * @param assignee 用户名称
     * @return
     */
    @RequestMapping(value = "/getTaskByAssignee")
    public List<Map<String, Object>> getTaskByAssignee(String assignee) {
        return myTaskService.getTaskByAssignee(assignee);
    }


    /**
     * 任务审批
     *
     * @param taskId   任务id
     * @param approved 审批结果 0通过 1拒绝
     * @param comment 审批意见
     * @return
     */
    @RequestMapping(value = "/complete")
    public String completeTask(String taskId, Integer approved,String comment) {
        myTaskService.complete(taskId, approved,comment);
        return approved == 0 ? "审批通过" : "审批不通过";
    }

    /**
     * 会签审批
     * @param taskId
     * @param approved
     * @return
     */
    @RequestMapping(value = "/mutileComplete")
    public String mutileComplete(String taskId, Integer approved) {
        myTaskService.mutileComplate(taskId, approved);
        return approved == 0 ? "审批通过" : "审批不通过";
    }

    /**
     * 前加签
     * @param taskId 当前加签的任务id
     * @param taskName 新加签的任务名称
     * @param assignee 新加签的任务受理人 这边可能是具体的一个人，也可能是一个表达式(调用接口去获取再设置到候选人)
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/beforeAddSignTask")
    public ReturnVo beforeAddSignTask(String taskId, String taskName, String assignee) {
        myTaskService.beforeAddSignTask(taskId,taskName,assignee);
        return  ReturnVo.sucess("前加签成功");
    }


    /**
     * 后加签
     * @param taskId 当前加签的任务id
     * @param taskName 新加签的任务名称
     * @param assignee 新加签的受理人
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/afterAddSignTask")
    public ReturnVo afterAddSignTask(String taskId,String taskName,String assignee) {
        myTaskService.afterAddSignTask(taskId,taskName,assignee);
        return ReturnVo.sucess("后加签成功");
    }
}
