package com.cocoamu.flowable.listener.execution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;

public class StartExecutionListener implements ExecutionListener {
    private static final Log LOGGER = LogFactory.getLog(StartExecutionListener.class);

    private static final long serialVersionUID = -8353030309981222979L;

    public void notify(DelegateExecution execution) {
        if (execution.getCurrentFlowElement() instanceof StartEvent) {

        } else if (execution.getCurrentFlowElement() instanceof UserTask) {

        } else if (execution.getCurrentFlowElement() instanceof EndEvent) {

        }
    }

}