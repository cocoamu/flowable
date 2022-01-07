package com.cocoamu.flowable.listener.task;

import com.cocoamu.flowable.constants.Constants;
import com.cocoamu.flowable.util.FlowableUitls;
import org.flowable.bpmn.model.ExtensionAttribute;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

import java.util.List;

public class CreateTaskListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        //获取自定义属性
        List<ExtensionAttribute> customProperty = FlowableUitls.getCustomProperty(delegateTask.getTaskDefinitionKey(), delegateTask.getProcessDefinitionId(), Constants.CUSTOM_ATTRIBUTES_USER_SELECTOR);
        System.out.printf("task id:"+ delegateTask.getName());
    }
}
