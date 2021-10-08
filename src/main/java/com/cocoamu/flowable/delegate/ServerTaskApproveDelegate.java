package com.cocoamu.flowable.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class ServerTaskApproveDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        Object employee=execution.getVariable("employee");
        System.out.println("审批通过，祝贺员工： " + employee);
    }
}