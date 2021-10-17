package com.cocoamu.flowable.controller;

import com.cocoamu.flowable.service.FlowService;
import com.cocoamu.flowable.service.MyModelService;
import com.fasterxml.jackson.databind.JsonNode;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/flow")
public class FlowController {
    @Autowired
    private FlowService flowService;

    @Autowired
    private MyModelService modelService;

    /**
     * 存储模型json
     *
     * @param modelNode
     * @return
     */
    @RequestMapping("/saveModelJSON")
    public String saveModelJSON(@RequestBody JsonNode modelNode) {
        return modelService.saveModelJSON(modelNode);
    }

    /**
     * 开始流程
     *
     * @param processKey 流程key
     * @return
     */
    @RequestMapping(value = "/start")
    public Map<String,Object> startProcessInstance(String processKey) {
        Map<String,Object> result = new HashMap<>();
        ProcessInstance processInstance = flowService.startProcess(processKey);
        result.put("result：","流程启动成功");
        result.put("实例id：",processInstance.getProcessInstanceId());
        result.put("流程定义id：",processInstance.getProcessDefinitionId());
        return result;
    }

    /**
     * 根据流程实例id查询任务列表
     *
     * @param processId 流程实例id
     * @return
     */
    @RequestMapping(value = "/getTaskByPid")
    public List<Map<String, Object>> getTaskByPid(String processId) {
        return flowService.getTaskByPid(processId);
    }

    /**
     * 查询某个用户的任务列表
     *
     * @param assignee 用户名称
     * @return
     */
    @RequestMapping(value = "/getTaskByAssignee")
    public List<Map<String, Object>> getTaskByAssignee(String assignee) {
        return flowService.getTaskByAssignee(assignee);
    }

    /**
     * 流程审批
     *
     * @param taskId   任务id
     * @param approved 审批结果 0通过 1拒绝
     * @return
     */
    @RequestMapping(value = "/completeTask")
    public String completeTask(String taskId, Integer approved) {
        flowService.completeTask(taskId, approved);
        return approved == 0 ? "审批通过" : "审批不通过";
    }

    /**
     * 根据流程实例id生成流程图
     *
     * @param httpServletResponse
     * @param processId
     * @throws Exception
     */
    @RequestMapping(value = "createProcessDiagramPic")
    public void createProcessDiagramPic(HttpServletResponse httpServletResponse, String processId) throws Exception {
        flowService.createProcessDiagramPic(httpServletResponse, processId);
    }

    /**
     * 根据流程定义id获取json格式定义
     * @param ProcessDefinitionId 流程定义id
     * @throws Exception
     */
    @RequestMapping(value = "/getBpmnJosn")
    public String getBpmnJosn(String ProcessDefinitionId) throws Exception {
        return flowService.getBpmnJson(ProcessDefinitionId);
    }

    /**
     * 根据流程定义id获取xml格式定义
     * @param ProcessDefinitionId 流程定义id
     * @throws Exception
     */
    @RequestMapping(value = "/getBpmnXmlByPid")
    public String getBpmnXmlByPid(String ProcessDefinitionId) throws Exception {
        return flowService.getBpmnXmlByPid(ProcessDefinitionId);
    }

    /**
     * 根据流程定义id获取xml格式定义
     * @param bpmJson json格式定义
     * @throws Exception
     */
    @RequestMapping(value = "/getBpmnXmlByJson")
    public String getBpmnXml(@RequestBody String bpmJson) throws Exception {
        return flowService.getBpmnXmlByJson(bpmJson);
    }

}