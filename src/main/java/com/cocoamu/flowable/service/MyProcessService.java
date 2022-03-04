package com.cocoamu.flowable.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.runtime.ProcessInstance;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

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
    String getBpmnXmlByJson(String bpmJson) throws Exception;

    /**
     * 根据xml定义获取json定义
     * @param bpmXml 流程xml定义
     * @return
     * @throws UnsupportedEncodingException
     * @throws XMLStreamException
     */
    String getBpmnJsonByXml(String bpmXml) throws UnsupportedEncodingException, XMLStreamException;

    List<FlowElement> calApprovePath(String processInstanceId, String modelId, Map<String, Object> variableMap);

}
