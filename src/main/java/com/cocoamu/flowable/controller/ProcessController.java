package com.cocoamu.flowable.controller;

import com.cocoamu.flowable.dto.CalApprovePathDto;
import com.cocoamu.flowable.dto.StartProcessDto;
import com.cocoamu.flowable.service.MyProcessService;
import com.cocoamu.flowable.service.MyTaskService;
import com.cocoamu.flowable.vo.FlowElementVo;
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
@RequestMapping(value = "/process")
public class ProcessController {

    @Autowired
    private MyProcessService myProcessService;
    @Autowired
    private MyTaskService myTaskService;

    /**
     * 开始流程
     *
     * @param processKey 流程key
     * @return
     */
    @RequestMapping(value = "/start")
    public Map<String,Object> startProcessInstance(@RequestBody StartProcessDto startProcessDto) {
        Map<String,Object> result = new HashMap<>();
        ProcessInstance processInstance = myProcessService.startProcess(startProcessDto.getProcessKey());
        result.put("processInstanceId：",processInstance.getProcessInstanceId());
        result.put("processDefinitionId：",processInstance.getProcessDefinitionId());

        myTaskService.updateSignTask(processInstance.getProcessInstanceId(),startProcessDto.getExpressList());
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
     * 模型json格式转xml格式
     * @param bpmJson json格式定义
     * @throws Exception
     */
    @RequestMapping(value = "/getBpmnXmlByJson")
    public String getBpmnXml(@RequestBody String bpmJson) throws Exception {
        return myProcessService.getBpmnXmlByJson(bpmJson);
    }

    /**
     * 模型xml格式转json格式
     * @param bpmJson xml格式定义
     * @throws Exception
     */
    @RequestMapping(value = "/getBpmnJsonByXml")
    public String getBpmnJsonByXml(@RequestBody String bpmJson) throws Exception {
        return myProcessService.getBpmnJsonByXml(bpmJson);
    }

    @RequestMapping(value = "/calApprovePath")
    public List<FlowElementVo> calApprovePath(@RequestBody CalApprovePathDto calApprovePathDto){
        return myProcessService.calApprovePath(calApprovePathDto.getProcessId(),calApprovePathDto.getParams(),calApprovePathDto.getApproveIds());
    }
}
