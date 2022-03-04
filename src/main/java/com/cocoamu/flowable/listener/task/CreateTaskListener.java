package com.cocoamu.flowable.listener.task;

import com.cocoamu.flowable.constants.Constants;
import com.cocoamu.flowable.util.FlowableUitls;
import org.flowable.bpmn.model.ExtensionAttribute;
import org.flowable.engine.HistoryService;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.task.service.delegate.DelegateTask;

import java.util.Arrays;
import java.util.List;

public class CreateTaskListener implements TaskListener {
    HistoryService historyService = FlowableUitls.getApplicationContext().getBean(HistoryService.class);

    @Override
    public void notify(DelegateTask delegateTask) {
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery().processInstanceId(delegateTask.getProcessInstanceId()).list();

        if (list.size()>0){
            List<String> list1 = Arrays.asList(list.get(list.size() - 1).getTaskId());
            list.stream().filter(entity->entity.getActivityId().equals(list.get(list.size() - 1).getActivityId()));
        }
        //获取自定义属性
        List<ExtensionAttribute> customProperty = FlowableUitls.getCustomProperty(delegateTask.getTaskDefinitionKey(), delegateTask.getProcessDefinitionId(), Constants.CUSTOM_ATTRIBUTES_USER_SELECTOR);
        System.out.printf("task id:"+ delegateTask.getName());
    }
}
