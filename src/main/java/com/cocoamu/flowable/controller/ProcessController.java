package com.cocoamu.flowable.controller;

import com.cocoamu.flowable.service.MyProcessService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/process")
public class ProcessController {

    @Autowired
    private MyProcessService myProcessService;

    /**
     * 开始流程
     *
     * @param processKey 流程key
     * @return
     */
    @RequestMapping(value = "/start")
    public Map<String,Object> startProcessInstance(String processKey) {
        Map<String,Object> result = new HashMap<>();
        ProcessInstance processInstance = myProcessService.startProcess(processKey);
        result.put("processInstanceId：",processInstance.getProcessInstanceId());
        return result;
    }

    /**
     * 根据流程实例id生成流程图
     *
     * @param httpServletResponse http响应对象
     * @param processId 流程实例id
     * @throws Exception
     */
    @RequestMapping(value = "/createProcessDiagramPic")
    public void createProcessDiagramPic(HttpServletResponse httpServletResponse, String processId) throws Exception {
        myProcessService.createProcessDiagramPic(httpServletResponse, processId);
    }

    /**
     * 根据流程定义id获取json格式定义
     * @param ProcessDefinitionId 流程定义id
     * @throws Exception
     */
    @RequestMapping(value = "/getBpmnJosn")
    public String getBpmnJosn(String ProcessDefinitionId) throws Exception {
        return myProcessService.getBpmnJson(ProcessDefinitionId);
    }

    /**
     * 根据流程定义id获取xml格式定义
     * @param ProcessDefinitionId 流程定义id
     * @throws Exception
     */
    @RequestMapping(value = "/getBpmnXmlByPid")
    public String getBpmnXmlByPid(String ProcessDefinitionId) throws Exception {
        return myProcessService.getBpmnXmlByPid(ProcessDefinitionId);
    }

    /**
     * 根据流程定义id获取xml格式定义
     * @param bpmJson json格式定义
     * @throws Exception
     */
    @RequestMapping(value = "/getBpmnXmlByJson")
    public String getBpmnXml(@RequestBody String bpmJson) throws Exception {
        return myProcessService.getBpmnXmlByJson(bpmJson);
    }
}
