package com.cocoamu.flowable.delegate;

import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

@Slf4j
public class ServerTaskApproveDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("ServerTaskApproveDelegate start ");
        ServiceTask userTask = (ServiceTask) execution.getCurrentFlowElement();
        Object employee=execution.getVariable("employee");
        System.out.println("审批通过，祝贺员工： " + employee);
    }
}