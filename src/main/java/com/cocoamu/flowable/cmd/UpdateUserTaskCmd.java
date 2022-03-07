package com.cocoamu.flowable.cmd;

import com.cocoamu.flowable.constants.Constants;
import com.cocoamu.flowable.dto.UpdateTaskDto;
import com.cocoamu.flowable.service.MyCommentService;
import com.cocoamu.flowable.util.ExtensionAttributeUtils;
import com.cocoamu.flowable.util.FlowableUitls;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.cmd.AbstractDynamicInjectionCmd;
import org.flowable.engine.impl.dynamic.BaseDynamicSubProcessInjectUtil;
import org.flowable.engine.impl.persistence.entity.DeploymentEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;

import java.util.*;

/**
 * 后加签核心类
 */

public class UpdateUserTaskCmd extends AbstractDynamicInjectionCmd implements Command<Void> {

    //当前操作加签节点信息
    private UpdateTaskDto currentTask;


    private ExecutionEntity currentExecutionEntity;
    private FlowElement currentFlowElemet;
    private MyCommentService myCommentService = FlowableUitls.getApplicationContext().getBean(MyCommentService.class);

    public UpdateUserTaskCmd(UpdateTaskDto updateTaskDto) {
        this.currentTask = updateTaskDto;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        createDerivedProcessDefinitionForProcessInstance(commandContext, currentTask.getProcessId());
        return null;
    }

    @Override
    protected void updateBpmnProcess(CommandContext commandContext, Process process,
                                     BpmnModel bpmnModel, ProcessDefinitionEntity originalProcessDefinitionEntity, DeploymentEntity newDeploymentEntity) {

        Collection<FlowElement> flowElements = bpmnModel.getMainProcess().getFlowElements();
        Optional<FlowElement> currentFlowElemet =  flowElements.stream().filter(element -> {
            return element.getId().equals(currentTask.getElementId());
        }).findFirst();
        if (!(currentFlowElemet.get() instanceof Task)) {
            throw new FlowableException("task type error");
        }
        Activity activity = (Activity) currentFlowElemet.get();

        ExtensionAttribute ea1 = ExtensionAttributeUtils.generate(Constants.CUSTOM_ATTRIBUTES_USER_SELECTOR + "_key", currentTask.getElementAttr());

        Map<String,List<ExtensionAttribute>> map = new HashMap<>();
        map.put("FLOWABLE", Arrays.asList(ea1));

        activity.setAttributes(map);

        //当前流程的重新定义
        BaseDynamicSubProcessInjectUtil.processFlowElements(commandContext, process, bpmnModel, originalProcessDefinitionEntity, newDeploymentEntity);

    }

    @Override
    protected void updateExecutions(CommandContext commandContext, ProcessDefinitionEntity processDefinitionEntity, ExecutionEntity executionEntity, List<ExecutionEntity> list) {
    }

}
