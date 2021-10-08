package com.cocoamu.flowable.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class ServerTaskRejectDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        Object employee=execution.getVariable("employee");
        System.out.println("审批不通过，员工" + employee + " 好自为之");
    }
}