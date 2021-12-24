package com.cocoamu.flowable.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.flowable.engine.runtime.ProcessInstance;

import javax.servlet.http.HttpServletResponse;

public interface MyProcessService {

    /**
     * 启动流程
     * @param processKey 流程key
     * @return
     */
    ProcessInstance startProcess(String processKey);

    /**
     * 绘制流程图
     * @param httpServletResponse http响应对象
     * @param processId 流程实例id
     * @throws Exception
     */
    void createProcessDiagramPic(HttpServletResponse httpServletResponse, String processId) throws Exception;

    /**
     * 根据流程定义id获取json定义
     * @param ProcessDefinitionId 流程定义id
     * @return
     */
    String getBpmnJson(String ProcessDefinitionId);

    /**
     * 根据流程定义id获取xml定义
     * @param ProcessDefinitionId 流程定义id
     * @return
     */
    String getBpmnXmlByPid(String ProcessDefinitionId);

    /**
     * 根据json定义获取xml定义
     * @param bpmJson json定义
     * @return
     * @throws JsonProcessingException
     */
    String getBpmnXmlByJson(String bpmJson) throws JsonProcessingException;
}
