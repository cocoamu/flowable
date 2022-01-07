package com.cocoamu.flowable.cmd;

import com.cocoamu.flowable.constants.Constants;
import com.cocoamu.flowable.dto.TaskDto;
import com.cocoamu.flowable.util.ExtensionAttributeUtils;
import com.cocoamu.flowable.util.FlowableUitls;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.cmd.AbstractDynamicInjectionCmd;
import org.flowable.engine.impl.context.Context;
import org.flowable.engine.impl.dynamic.BaseDynamicSubProcessInjectUtil;
import org.flowable.engine.impl.dynamic.DynamicUserTaskBuilder;
import org.flowable.engine.impl.persistence.entity.DeploymentEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.impl.util.ProcessDefinitionUtil;
import org.flowable.task.service.TaskService;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;

import java.util.Collections;
import java.util.List;

/**
 * 前加签核心类
 */
public class BeforeSignUserTaskCmd extends AbstractDynamicInjectionCmd implements Command<Void> {
    //流程实例id
    private TaskDto currentTask;

    //后加签的节点信息
    private DynamicUserTaskBuilder signUserTaskBuilder;

    private ExecutionEntity currentExecutionEntity;

    private FlowElement currentFlowElemet;

    public BeforeSignUserTaskCmd(TaskDto taskDto, DynamicUserTaskBuilder signUserTaskBuilder) {
        this.currentTask = taskDto;
        this.signUserTaskBuilder = signUserTaskBuilder;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        createDerivedProcessDefinitionForProcessInstance(commandContext, currentTask.getProcessInstanceId());
        return null;
    }

    @Override
    protected void updateBpmnProcess(CommandContext commandContext, Process process,
                                     BpmnModel bpmnModel, ProcessDefinitionEntity originalProcessDefinitionEntity, DeploymentEntity newDeploymentEntity) {
        //判断当前任务是否存在
        TaskService taskService = CommandContextUtil.getTaskService(commandContext);
        TaskEntity taskEntity = taskService.getTask(currentTask.getTaskId());
        if(taskEntity==null){
            throw new FlowableObjectNotFoundException("task:"+currentTask.getTaskId()+" not found");
        }

        //构建加签节点
        UserTask addUserTask = new UserTask();
        String id=signUserTaskBuilder.getId();
        if (id == null) {
            id=signUserTaskBuilder.nextTaskId(process.getFlowElementMap());
        }
        signUserTaskBuilder.setDynamicTaskId(id);
        addUserTask.setId(id);
        addUserTask.setName(signUserTaskBuilder.getName());
        addUserTask.setAssignee(signUserTaskBuilder.getAssignee());

        //设置自定义属性 这个可以在监听器里面解析出来搞一些事
        ExtensionAttribute ea1 = ExtensionAttributeUtils.generate(Constants.CUSTOM_ATTRIBUTES_USER_SELECTOR + "_key", signUserTaskBuilder.getAssignee());
        addUserTask.addAttribute(ea1);
        //设置执行监听器
        addUserTask.setExecutionListeners(FlowableUitls.getExecuteListener());
        //设置任务监听器
        addUserTask.setTaskListeners(FlowableUitls.getTaskListener());

        //查找当前节点对应的执行执行实体（表为ACT_RU_EXECUTION）
        currentExecutionEntity = CommandContextUtil.getExecutionEntityManager(commandContext).findById(taskEntity.getExecutionId());
        if(currentExecutionEntity==null){
            throw new FlowableObjectNotFoundException("task:"+currentTask.getTaskId()+",execution:"+taskEntity.getExecutionId()+" not found");
        }
        //获取当前流程节点入口顺序流
        String activityId = currentExecutionEntity.getActivityId();
        currentFlowElemet = process.getFlowElement(activityId,true);
        if(!(currentFlowElemet instanceof Task)){
            throw new FlowableException("task type error");
        }
        Activity activity = (Activity) currentFlowElemet;
        SequenceFlow currentTaskInSequenceFlow = activity.getIncomingFlows().get(0);

        //定义新的顺序流 节点的source改为新增节点  taget改为当前节点
        String flowId = "sequenceFlow-"+CommandContextUtil.getProcessEngineConfiguration(commandContext).getIdGenerator().getNextId();
        SequenceFlow newSequenceFlow = FlowableUitls.createSequenceFlow(flowId,addUserTask.getId(),taskEntity.getTaskDefinitionKey());

        //修改原有sequence 将原来的sequence的taget改为新的节点
        currentTaskInSequenceFlow.setTargetFlowElement(addUserTask);
        currentTaskInSequenceFlow.setTargetRef(addUserTask.getId());

        //修改当前节点的进入流为新加的顺序流
        activity.setIncomingFlows(Collections.singletonList(newSequenceFlow));

        //把新加的节点跟连线加入到当前流程实例
        process.addFlowElement(addUserTask);
        process.addFlowElement(newSequenceFlow);

       //调用自动排版方法
        new BpmnAutoLayout(bpmnModel).execute();

        //当前流程的重新定义
        BaseDynamicSubProcessInjectUtil.processFlowElements(commandContext, process, bpmnModel, originalProcessDefinitionEntity, newDeploymentEntity);
    }

    @Override
    protected void updateExecutions(CommandContext commandContext, ProcessDefinitionEntity processDefinitionEntity, ExecutionEntity executionEntity, List<ExecutionEntity> list) {
        BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(processDefinitionEntity.getId());
       TaskService taskService = CommandContextUtil.getTaskService(commandContext);
        List<TaskEntity> taskEntities= taskService.findTasksByProcessInstanceId(currentTask.getProcessInstanceId());
        // 删除当前活动任务
        for (TaskEntity taskEntity:taskEntities) {
            taskEntity.getIdentityLinks().stream().forEach(identityLinkEntity -> {
                if(identityLinkEntity.isGroup()){
                    taskEntity.deleteGroupIdentityLink(identityLinkEntity.getGroupId(),"candidate");
                }else{
                    taskEntity.deleteUserIdentityLink(identityLinkEntity.getUserId(),"participant");
                }
            });
            if(taskEntity.getTaskDefinitionKey().equals(currentFlowElemet.getId())){
                taskService.deleteTask(taskEntity,false);
            }
        }
        //设置活动后的节点
        UserTask userTask = (UserTask) bpmnModel.getProcessById(processDefinitionEntity.getKey()).getFlowElement(signUserTaskBuilder.getId());
        currentExecutionEntity.setCurrentFlowElement(userTask);
        Context.getAgenda().planContinueProcessOperation(currentExecutionEntity);
    }
}
