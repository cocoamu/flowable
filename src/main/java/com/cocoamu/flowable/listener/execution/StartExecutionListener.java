package com.cocoamu.flowable.listener.execution;

import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;


public class StartExecutionListener implements ExecutionListener {

    private static final long serialVersionUID = -8353030309981222979L;

    public void notify(DelegateExecution execution) {
        UserTask userTask = (UserTask) execution.getCurrentFlowElement();
//        List<ExtensionAttribute> customProperty = FlowableUitls.getCustomProperty(userTask.getId(), execution.getProcessDefinitionId(), Constants.CUSTOM_ATTRIBUTES_USER_SELECTOR + "_key");
        System.out.println("task name:"+ userTask.getName());
    }

}