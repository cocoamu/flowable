package com.cocoamu.flowable.delegate;

import com.cocoamu.flowable.util.FlowableUitls;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

@Slf4j
public class ServerTaskApproveDelegate implements JavaDelegate {

    // 定义静态内部类实现单例对象初始化
    private static class InnerClass {
        private static final RuntimeService RUNTIMESERVICE = FlowableUitls.getApplicationContext().getBean(RuntimeService.class);
        private static final HistoryService HISTORYSERVICE = FlowableUitls.getApplicationContext().getBean(HistoryService.class);
    }

    @Override
    public void execute(DelegateExecution execution) {
        log.info("ServerTaskApproveDelegate start ");
        ServiceTask userTask = (ServiceTask) execution.getCurrentFlowElement();
        Object employee=execution.getVariable("employee");
        System.out.println("审批通过，祝贺员工： " + employee);
    }
}