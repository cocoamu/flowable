package com.cocoamu.flowable.listener.execution;

import com.cocoamu.flowable.constants.Constants;
import com.cocoamu.flowable.service.MyTaskService;
import com.cocoamu.flowable.util.FlowableUitls;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.ExtensionAttribute;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;

import java.util.List;


public class StartExecutionListener implements ExecutionListener {

    private static final long serialVersionUID = -8353030309981222979L;

    private static final MyTaskService myTaskService = FlowableUitls.getApplicationContext().getBean(MyTaskService.class);

    public void notify(DelegateExecution execution) {
        if (execution.getCurrentFlowElement() instanceof StartEvent) {
//            List<UpdateElementDto> list = (List<UpdateElementDto>) execution.getVariable("expressList");
//            if (CollectionUtils.isNotEmpty(list)) {
//                myTaskService.updateSignTask(execution.getProcessInstanceId(), list);
//            }
        }
        if (execution.getCurrentFlowElement() instanceof UserTask) {
            UserTask userTask = (UserTask) execution.getCurrentFlowElement();
            if (null != userTask) {
                String test = (String) execution.getVariable(execution.getCurrentFlowElement().getId()+"_"+Constants.CUSTOM_ATTRIBUTES_USER_SELECTOR);
                //获取任务自定义属性
                List<ExtensionAttribute> customProperty = FlowableUitls.getCustomProperty(userTask.getId(), execution.getProcessDefinitionId(), Constants.CUSTOM_ATTRIBUTES_USER_SELECTOR);
                System.out.println(execution.getCurrentFlowElement().getName());
            }
        }
        else if (execution.getCurrentFlowElement() instanceof EndEvent) {

        }

    }
}