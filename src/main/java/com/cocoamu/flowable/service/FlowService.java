package com.cocoamu.flowable.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FlowService {

    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;



    /**
     * 启动工作流
     * @param employee
     * @return
     */
    public ProcessInstance startProcess(String employee) {
        Map<String, Object> map = new HashMap();
        map.put("employee", employee);
        map.put("cusExpress", 1);
        ProcessInstance processInstance =  runtimeService.startProcessInstanceByKey("holidayRequest", map);
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());
        //指定下一个审批人，也可以在xml或监听器里面指定
        //类似：<userTask id="holidayApprovedTask" name="Holiday approved" flowable:assignee="${employee}"/>
        Task task2 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.setAssignee(task2.getId(),"boss");
        return processInstance;
    }

    /**
     * 根据流程实例id查询任务列表
     * @param processId 流程实例id
     * @return
     */
    public List<Map<String,Object>> getTaskByPid(String processId) {
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processId).list();
        List<Map<String,Object>> mapList = new ArrayList<>();
        for (Task task : taskList) {
            Map<String,Object> resultMap = new HashMap<>();
            resultMap.put("taskId",task.getId());
            resultMap.put("taskName",task.getName());
            resultMap.put("employee",(String)taskService.getVariable(task.getId(),"employee"));
            mapList.add(resultMap);
        }
        return mapList;
    }

    /**
     * 查询某个用户的任务列表
     * @param assignee 用户名称
     * @return
     */
    public List<Map<String,Object>> getTaskByAssignee(String assignee) {
        List<Task> taskList = taskService.createTaskQuery().taskAssignee(assignee).orderByTaskCreateTime().desc().list();;
        List<Map<String,Object>> mapList = new ArrayList<>();
        for (Task task : taskList) {
            Map<String,Object> resultMap = new HashMap<>();
            resultMap.put("taskId",task.getId());
            resultMap.put("taskName",task.getName());
            resultMap.put("employee",(String)taskService.getVariable(task.getId(),"employee"));
            mapList.add(resultMap);
        }
        return mapList;
    }

    /**
     * 流程审批
     * @param taskId 任务id
     * @param approved 审批结果 0通过 1拒绝
     * @return
     */
    public void completeTask(String taskId, Integer approved) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("流程不存在");
        }
        taskService.setVariable(taskId, "approved", approved.toString());
        taskService.complete(taskId);
    }


    /**
     * 根据流程实例id生成流程图
     * @param httpServletResponse
     * @param processId
     * @throws Exception
     */
    public void createProcessDiagramPic(HttpServletResponse httpServletResponse, String processId) throws Exception {
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
        if (pi == null) {
            return;
        }
        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();

        String InstanceId = task.getProcessInstanceId();
        List<Execution> executions = runtimeService
                .createExecutionQuery()
                .processInstanceId(InstanceId)
                .list();
        List<String> activityIds = new ArrayList<>();
        List<String> flows = new ArrayList<>();
        for (Execution exe : executions) {
            List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
            activityIds.addAll(ids);
        }
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, engconf.getActivityFontName(), engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), 1.0,false);
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * 根据流程定义id获取jspn格式定义
     * @param ProcessDefinitionId 流程定义id
     * @return
     */
    public String getBpmnJson(String ProcessDefinitionId){
        BpmnModel bpmnModel = repositoryService.getBpmnModel(ProcessDefinitionId);
        BpmnJsonConverter converter = new BpmnJsonConverter();
        com.fasterxml.jackson.databind.node.ObjectNode editorJsonNode = converter.convertToJson(bpmnModel);

        return editorJsonNode.toString();
    }

    public String getBpmnXmlByPid(String ProcessDefinitionId) throws JsonProcessingException {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(ProcessDefinitionId);
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel);
        return new String(bpmnBytes);
    }


    public String getBpmnXmlByJson(String bpmJson) throws JsonProcessingException {
        ObjectNode modelNode = (ObjectNode) new ObjectMapper().readTree(bpmJson);
        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
        return new String(bpmnBytes);
    }
}