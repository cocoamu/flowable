package com.cocoamu.flowable.cmd;

import com.cocoamu.flowable.constants.Constants;
import com.cocoamu.flowable.dto.UpdateElementDto;
import com.cocoamu.flowable.util.ExtensionAttributeUtils;
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
 * 动态修改环节属性核心类
 */

public class UpdateElementAttrCmd extends AbstractDynamicInjectionCmd implements Command<Void> {

    //当前操作加签节点信息
    private final UpdateElementDto currentTask;
    private final String processId;

    public UpdateElementAttrCmd(String processId, UpdateElementDto updateTaskDto) {
        this.processId = processId;
        this.currentTask = updateTaskDto;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        createDerivedProcessDefinitionForProcessInstance(commandContext, processId);
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
        //获取当前节点对象
        Activity activity = (Activity) currentFlowElemet.get();

        ExtensionAttribute ea1 = ExtensionAttributeUtils.generate(Constants.CUSTOM_ATTRIBUTES_USER_SELECTOR, currentTask.getElementAttr());

        Map<String,List<ExtensionAttribute>> map = new HashMap<>();
        //这边设置这个data没什么特别的意义，后面代码里也不会根据这个key来取，而是根据userTask.getAttributes()直接取到
        map.put("data", Arrays.asList(ea1));

        activity.setAttributes(map);

        //当前流程的重新定义
        BaseDynamicSubProcessInjectUtil.processFlowElements(commandContext, process, bpmnModel, originalProcessDefinitionEntity, newDeploymentEntity);

    }

    @Override
    protected void updateExecutions(CommandContext commandContext, ProcessDefinitionEntity processDefinitionEntity, ExecutionEntity executionEntity, List<ExecutionEntity> list) {
    }

}
