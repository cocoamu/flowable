package com.cocoamu.flowable.listener.execution;

import com.cocoamu.flowable.constants.Constants;
import com.cocoamu.flowable.util.FlowableUitls;
import org.flowable.bpmn.model.ExtensionAttribute;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;

import java.util.List;


public class StartExecutionListener implements ExecutionListener {

    private static final long serialVersionUID = -8353030309981222979L;

    public void notify(DelegateExecution execution) {
        UserTask userTask = (UserTask) execution.getCurrentFlowElement();
        //获取任务自定义属性
        List<ExtensionAttribute> customProperty = FlowableUitls.getCustomProperty(userTask.getId(), execution.getProcessDefinitionId(), Constants.CUSTOM_ATTRIBUTES_USER_SELECTOR);
        System.out.println(execution.getCurrentFlowElement().getName());
    }
}