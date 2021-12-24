package com.cocoamu.flowable.util;

import com.cocoamu.flowable.listener.execution.StartExecutionListener;
import com.cocoamu.flowable.listener.task.CreateTaskListener;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.TaskListener;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FlowableUitls {

    private static volatile ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext ac) {
        if (applicationContext == null) {
            applicationContext = ac;
        }
    }

    public static ApplicationContext getApplicationContext() {
        AssertUtils.assertNotNull(applicationContext, "applicationContext is null");
        return applicationContext;
    }

    /**
     * 获取执行监听器
     * @return
     */
    public static List<FlowableListener> getExecuteListener() {
        ArrayList<FlowableListener> listener = new ArrayList<>();
        FlowableListener executionListener = new FlowableListener();
        executionListener.setEvent(ExecutionListener.EVENTNAME_START);
        executionListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
//        executionListener.setImplementation("${startExecutionListener}");
        executionListener.setImplementation(StartExecutionListener.class.getName());
        listener.add(executionListener);
        return listener;
    }

    /**
     * 获取任务监听器
     * @return
     */
    public static List<FlowableListener> getTaskListener() {
        ArrayList<FlowableListener> listener = new ArrayList<>();
        FlowableListener taskListener = new FlowableListener();
        taskListener.setEvent(TaskListener.EVENTNAME_CREATE);
        taskListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
//        taskListener.setImplementation("${createTaskListener}");
        taskListener.setImplementation(CreateTaskListener.class.getName());
        listener.add(taskListener);
        return listener;
    }


    /**
     * 根据两点创建顺序流
     * @param id 节点id
     * @param from 起始节点
     * @param to 目标节点
     * @return
     */
    public static SequenceFlow createSequenceFlow(String id, String from, String to) {
        SequenceFlow flow = new SequenceFlow();
        flow.setId(id);
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        return flow;
    }

    public static FlowElement getFlowElementByActivityIdAndProcessDefinitionId(String activityId, String processDefinitionId) {
        AssertUtils.assertNotNull(applicationContext, "applicationContext is null");

        RepositoryService repositoryService = applicationContext.getBean(RepositoryService.class);
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        List<org.flowable.bpmn.model.Process> processes = bpmnModel.getProcesses();
        if (CollectionUtils.isNotEmpty(processes)) {
            for (Process process : processes) {
                FlowElement flowElement = process.getFlowElement(activityId);
                if (flowElement != null) {
                    return flowElement;
                }
            }
        }
        return null;
    }

    public static List<ExtensionAttribute> getCustomProperty(String activityId, String processDefinitionId, String customPropertyName) {
        FlowElement flowElement = getFlowElementByActivityIdAndProcessDefinitionId(activityId, processDefinitionId);
        if (flowElement instanceof UserTask) {
            UserTask userTask = (UserTask) flowElement;
            Map<String, List<ExtensionAttribute>> attributes = userTask.getAttributes();
            if (MapUtils.isNotEmpty(attributes)) {
                return attributes.get(customPropertyName);
            }
        }
        return null;
    }
}
