package com.cocoamu.flowable.service;

import com.cocoamu.flowable.dto.UpdateElementDto;
import com.cocoamu.flowable.vo.FlowElementVo;
import com.fasterxml.jackson.core.JsonProcessingException;
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
    ProcessInstance startProcess(String processKey,List<UpdateElementDto> expressList);


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

    /**
     * 根据表达式获取(预测)流程可能经过的节点
     * @param processId 流程定义id
     * @param variableMap 表达式(key:value)
     * @param approveIds 过滤范围 (可能经过的有5个人，但只要返回在这个范围内的即可,为空则返回所有预测的节点)
     * @return
     */
    List<FlowElementVo> calApprovePath(String processId, Map<String, Object> variableMap, List<String> approveIds);

}
