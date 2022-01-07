package com.cocoamu.flowable.service.impl;

import com.cocoamu.flowable.cmd.AfterSignUserTaskCmd;
import com.cocoamu.flowable.cmd.BeforeSignUserTaskCmd;
import com.cocoamu.flowable.dto.AddSignDto;
import com.cocoamu.flowable.dto.TaskDto;
import com.cocoamu.flowable.service.MyTaskService;
import com.cocoamu.flowable.vo.ReturnVo;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.impl.dynamic.DynamicUserTaskBuilder;
import org.flowable.task.api.Task;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MyTaskServiceImpl implements MyTaskService {
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private ProcessEngine processEngine;

    @Override
    public List<Map<String, Object>> getTaskByPid(String processId) {
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processId).list();
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Task task : taskList) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("taskId", task.getId());
            resultMap.put("taskName", task.getName());
            resultMap.put("assignee", task.getAssignee());
            mapList.add(resultMap);
        }
        return mapList;
    }

    @Override
    public List<Map<String, Object>> getTaskByAssignee(String assignee) {
        List<Task> taskList = taskService.createTaskQuery().taskAssignee(assignee).orderByTaskCreateTime().desc().list();
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Task task : taskList) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("taskId", task.getId());
            resultMap.put("taskName", task.getName());
            resultMap.put("assignee", task.getAssignee());
            mapList.add(resultMap);
        }
        return mapList;
    }

    @Override
    public void complete(String taskId, Integer approved, String comment) {
        TaskEntity taskEntity = (TaskEntity) taskService.createTaskQuery().taskId(taskId).singleResult();
        if (taskEntity == null) {
            throw new RuntimeException("任务不存在");
        }
        Map<String, Object> map = new HashMap();
        map.put("approved", approved);
        map.put("comment", comment);
        taskService.complete(taskId, map);
    }

    @Override
    public void mutileComplate(String taskId, Integer approved) {
        TaskEntity taskEntity = (TaskEntity) taskService.createTaskQuery().taskId(taskId).singleResult();
        if (taskEntity == null) {
            throw new RuntimeException("任务不存在");
        }
        //如果同意
        if (approved == 0) {
            //获取当前签署总数
            String currentSignCount = StringUtils
                    .defaultString(runtimeService.getVariable(taskEntity.getExecutionId(), "signCount").toString(), "0");
            //签署数+1
            runtimeService.setVariable(taskEntity.getExecutionId(), "signCount", Integer.parseInt(currentSignCount) + 1);
        }
        taskService.complete(taskId);
    }

    @Override
    public ReturnVo addSignTask(AddSignDto addSignVo) {
        //加签节点信息
        DynamicUserTaskBuilder taskBuilder = new DynamicUserTaskBuilder();
        taskBuilder.setName(addSignVo.getTaskName());
        String taskIdPrefix = addSignVo.getAddType() == 0 ? "AddBeforeSign" : "AddAfterSign";
        taskBuilder.setId(taskIdPrefix + UUID.randomUUID().toString().replaceAll("-", ""));
        taskBuilder.setAssignee(addSignVo.getAssignee());
        TaskEntity taskEntity = (TaskEntity) taskService.createTaskQuery().taskId(addSignVo.getTaskId()).singleResult();
        if (taskEntity != null) {
            TaskDto taskDto = new TaskDto();
            taskDto.setProcessInstanceId(taskEntity.getProcessInstanceId());
            taskDto.setTaskId(taskEntity.getId());
            taskDto.setAssignee(addSignVo.getUserId());
            if (addSignVo.getAddType() == 0) {
                //前加签
                processEngine.getManagementService().executeCommand(new BeforeSignUserTaskCmd(taskDto,taskBuilder));
            } else {
                //后加签
                processEngine.getManagementService().executeCommand(new AfterSignUserTaskCmd(taskDto,taskBuilder));
            }
            return ReturnVo.sucess("加签成功");
        } else {
            return ReturnVo.fail("任务不存在");
        }
    }
}
