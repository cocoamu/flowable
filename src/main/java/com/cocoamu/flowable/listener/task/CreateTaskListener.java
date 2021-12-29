package com.cocoamu.flowable.listener.task;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

public class CreateTaskListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.printf("task id:"+ delegateTask.getName());
    }
}
