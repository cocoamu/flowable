package com.cocoamu.flowable.service.impl;

import com.cocoamu.flowable.cmd.AfterSignUserTaskCmd;
import com.cocoamu.flowable.cmd.BeforeSignUserTaskCmd;
import com.cocoamu.flowable.cmd.UpdateElementAttrCmd;
import com.cocoamu.flowable.constants.Constants;
import com.cocoamu.flowable.dto.AddSignDto;
import com.cocoamu.flowable.dto.TaskDto;
import com.cocoamu.flowable.dto.UpdateElementDto;
import com.cocoamu.flowable.service.MyTaskService;
import com.cocoamu.flowable.util.FlowableUitls;
import com.cocoamu.flowable.vo.ReturnVo;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.impl.dynamic.DynamicUserTaskBuilder;
import org.flowable.task.api.Task;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
public class MyTaskServiceImpl implements MyTaskService {
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    protected RepositoryService repositoryService;

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
        map.put("a", 1);
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

    @Override
    public void updateSignTask(String processId, List<UpdateElementDto> list) {
        list.stream().forEach(updateTaskDto -> {
            processEngine.getManagementService().executeCommand(new UpdateElementAttrCmd(processId,updateTaskDto));
        });
    }

    @Override
    public ReturnVo goBack(String taskId, Integer backType) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null)
            return ReturnVo.fail("任务不存在");
        //退回到发起人
        if (backType==0){

        }else{
            //退回到上一个环节
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
            SequenceFlow sequenceFlow = flowNode.getIncomingFlows().get(0);
            //如果来源是开始节点
            if (sequenceFlow.getSourceFlowElement() instanceof StartEvent) {

            }else{
                //获取下当前节点的上一个节点，用于下面的流程回退
                FlowElement preFlowNode = (FlowNode) bpmnModel.getFlowElement(getFrontUserTaskIds(task).get(0));
                //这边的情况是上个节点是网关，继续判断网关的上个节点是不是开始环节
                if (preFlowNode instanceof StartEvent) {

                } else {
                    runtimeService.createChangeActivityStateBuilder()
                            .processInstanceId(task.getProcessInstanceId()).moveActivityIdTo(task.getTaskDefinitionKey(), preFlowNode.getId())
                            .changeState();
                }
            }
        }
        return null;
    }

    /**
     * 获取当前节点的上一节点
     * 当是GateWay节点时，将targtaetRef设为网关的，继续遍历上一节点，就是跳过网关节点，只要用户任务节点
     *
     * @param task
     * @return
     */
    private List<String> getFrontUserTaskIds(Task task) {
        //网关集合
        List<Gateway> gateways = new ArrayList<>();
        //用户任务集合
        List<UserTask> userTasks = new ArrayList<>();
        //网关节点id
        List<String> gatewayNodelIdList = new ArrayList<>();
        //抄送节点id
        List<String> serviceTaskNodeIdList = new ArrayList<>();
        List<UserTask> serviceTasks = new ArrayList<>();
        //用户任务节点id
        List<String> usertaskNodelIdList = new ArrayList<>();

        String processDefinitionId = task.getProcessDefinitionId();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        List<Process> processes = bpmnModel.getProcesses();
        Process process = processes.get(0);
        Collection<FlowElement> flowElements = process.getFlowElements();
        flowElements.forEach(flowElement -> {
            if (flowElement instanceof Gateway) {
                gatewayNodelIdList.add(flowElement.getId());
                gateways.add((Gateway) flowElement);
            }
            if (isNormalUserTask(flowElement, task, serviceTaskNodeIdList, serviceTasks)) {
                usertaskNodelIdList.add(flowElement.getId());
                userTasks.add((UserTask) flowElement);
            }
        });
        return getFrontUserTaskIds(task, userTasks, gatewayNodelIdList, gateways, serviceTaskNodeIdList, serviceTasks);
    }

    //是否为普通的审核环节
    private boolean isNormalUserTask(FlowElement flowElement, Task task, List<String> serviceTaskNodeIdList, List<UserTask> serviceTasks) {
        boolean result = false;
        if (flowElement instanceof UserTask) {
            //先判断业务类型是原来的普通usertask还是新增的抄送业务
            Integer businessType = 0;
            List<ExtensionAttribute> businessTypeProperty = FlowableUitls.getCustomProperty(flowElement.getId(), task.getProcessDefinitionId(), Constants.CUSTOM_ATTRIBUTES_USER_SELECTOR + "_key");
            if (!CollectionUtils.isEmpty(businessTypeProperty))
                businessType = Integer.valueOf(businessTypeProperty.get(0).getValue());
            if (1 == businessType) {
                serviceTaskNodeIdList.add(flowElement.getId());
                serviceTasks.add((UserTask) flowElement);
            }
            result = 0 == businessType;
        }
        return result;
    }

    //获取上一环节，要跳过抄送和网关
    private List<String> getFrontUserTaskIds(Task task, List<UserTask> userTasks, List<String> gatewayNodelIdList, List<Gateway> gateways, List<String> serviceTaskNodeIdList, List<UserTask> serviceTasks) {
        String nodeId = task.getTaskDefinitionKey();
        List<String> frontNodeIdlist = new ArrayList<>();
        for (UserTask userTask : userTasks) {
            List<SequenceFlow> incomingFlows = userTask.getIncomingFlows();
            for (SequenceFlow incomingFlow : incomingFlows) {
                String sourceRef = incomingFlow.getSourceRef();
                String targetRef = incomingFlow.getTargetRef();
                if (nodeId.equals(targetRef)) {
                    //当前任务的上一节点是网关
                    if (gatewayNodelIdList.contains(sourceRef)) {
                        gateways.stream().forEach((Gateway gateway) -> {
                            List<SequenceFlow> incomingFlowsGateWay = gateway.getIncomingFlows();
                            incomingFlowsGateWay.stream().forEach((SequenceFlow sequenceFlow) -> {
                                String sourceRefGateWay = sequenceFlow.getSourceRef();
                                String targetRefGateWay = sequenceFlow.getTargetRef();
                                if (sourceRef.equals(targetRefGateWay)) {
                                    frontNodeIdlist.add(sourceRefGateWay);
                                }
                            });
                        });
                    } else if (serviceTaskNodeIdList.contains(sourceRef)) {
                        serviceTasks.stream().forEach((UserTask serviceTask) -> {
                            List<SequenceFlow> incomingFlowsTemp = serviceTask.getIncomingFlows();
                            incomingFlowsTemp.stream().forEach((SequenceFlow sequenceFlow) -> {
                                String sourceRefTask = sequenceFlow.getSourceRef();
                                String targetRefTask = sequenceFlow.getTargetRef();
                                if (sourceRef.equals(targetRefTask)) {
                                    frontNodeIdlist.add(sourceRefTask);
                                }
                            });
                        });
                    } else {
                        frontNodeIdlist.add(sourceRef);
                    }
                }
            }
        }
        return frontNodeIdlist;
    }
}
