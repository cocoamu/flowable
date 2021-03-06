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
            throw new RuntimeException("???????????????");
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
            throw new RuntimeException("???????????????");
        }
        //????????????
        if (approved == 0) {
            //????????????????????????
            String currentSignCount = StringUtils
                    .defaultString(runtimeService.getVariable(taskEntity.getExecutionId(), "signCount").toString(), "0");
            //?????????+1
            runtimeService.setVariable(taskEntity.getExecutionId(), "signCount", Integer.parseInt(currentSignCount) + 1);
        }
        taskService.complete(taskId);
    }

    @Override
    public ReturnVo addSignTask(AddSignDto addSignVo) {
        //??????????????????
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
                //?????????
                processEngine.getManagementService().executeCommand(new BeforeSignUserTaskCmd(taskDto,taskBuilder));
            } else {
                //?????????
                processEngine.getManagementService().executeCommand(new AfterSignUserTaskCmd(taskDto,taskBuilder));
            }
            return ReturnVo.sucess("????????????");
        } else {
            return ReturnVo.fail("???????????????");
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
            return ReturnVo.fail("???????????????");
        //??????????????????
        if (backType==0){

        }else{
            //????????????????????????
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
            SequenceFlow sequenceFlow = flowNode.getIncomingFlows().get(0);
            //???????????????????????????
            if (sequenceFlow.getSourceFlowElement() instanceof StartEvent) {

            }else{
                //?????????????????????????????????????????????????????????????????????
                FlowElement preFlowNode = (FlowNode) bpmnModel.getFlowElement(getFrontUserTaskIds(task).get(0));
                //????????????????????????????????????????????????????????????????????????????????????????????????
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
     * ?????????????????????????????????
     * ??????GateWay???????????????targtaetRef????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param task
     * @return
     */
    private List<String> getFrontUserTaskIds(Task task) {
        //????????????
        List<Gateway> gateways = new ArrayList<>();
        //??????????????????
        List<UserTask> userTasks = new ArrayList<>();
        //????????????id
        List<String> gatewayNodelIdList = new ArrayList<>();
        //????????????id
        List<String> serviceTaskNodeIdList = new ArrayList<>();
        List<UserTask> serviceTasks = new ArrayList<>();
        //??????????????????id
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

    //??????????????????????????????
    private boolean isNormalUserTask(FlowElement flowElement, Task task, List<String> serviceTaskNodeIdList, List<UserTask> serviceTasks) {
        boolean result = false;
        if (flowElement instanceof UserTask) {
            //???????????????????????????????????????usertask???????????????????????????
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

    //?????????????????????????????????????????????
    private List<String> getFrontUserTaskIds(Task task, List<UserTask> userTasks, List<String> gatewayNodelIdList, List<Gateway> gateways, List<String> serviceTaskNodeIdList, List<UserTask> serviceTasks) {
        String nodeId = task.getTaskDefinitionKey();
        List<String> frontNodeIdlist = new ArrayList<>();
        for (UserTask userTask : userTasks) {
            List<SequenceFlow> incomingFlows = userTask.getIncomingFlows();
            for (SequenceFlow incomingFlow : incomingFlows) {
                String sourceRef = incomingFlow.getSourceRef();
                String targetRef = incomingFlow.getTargetRef();
                if (nodeId.equals(targetRef)) {
                    //????????????????????????????????????
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
