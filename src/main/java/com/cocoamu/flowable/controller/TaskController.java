package com.cocoamu.flowable.controller;

import com.cocoamu.flowable.dto.AddSignDto;
import com.cocoamu.flowable.service.MyTaskService;
import com.cocoamu.flowable.vo.ReturnVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
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
     * 加签
     * @param addSignVo 0前加签 1后加签
     * @return
     */
    @RequestMapping(value = "/addSignTask")
    public ReturnVo addSignTask(@RequestBody AddSignDto addSignVo) {
        return myTaskService.addSignTask(addSignVo);
    }
}
