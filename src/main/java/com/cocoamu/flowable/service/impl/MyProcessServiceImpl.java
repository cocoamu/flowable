package com.cocoamu.flowable.service.impl;

import com.cocoamu.flowable.service.MyProcessService;
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
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MyProcessServiceImpl implements MyProcessService {
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private RepositoryService repositoryService;

    @Override
    public ProcessInstance startProcess(String processKey) {
        //可以往这个map里面放一些参数后面流程中可以获取
        Map<String, Object> map = new HashMap();

        //这边是为了测试会签任务设置的变量
        ArrayList<String> list = new ArrayList<>();
        list.add("王明4");
        list.add("小李4");
        list.add("红红4");
        map.put("assigneeList", list);
        map.put("signCount", 0);

        ProcessInstance processInstance =  runtimeService.startProcessInstanceByKey(processKey, map);
        runtimeService.setVariable(processInstance.getProcessInstanceId(),"processInstance",processInstance.getProcessInstanceId());
        return processInstance;
    }

    @Override
    public void createProcessDiagramPic(HttpServletResponse httpServletResponse, String processId) throws Exception {
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
        if (pi == null) {
            return;
        }
        List<Execution> executions = runtimeService
                .createExecutionQuery()
                .processInstanceId(processId)
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

    @Override
    public String getBpmnJson(String ProcessDefinitionId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(ProcessDefinitionId);
        BpmnJsonConverter converter = new BpmnJsonConverter();
        com.fasterxml.jackson.databind.node.ObjectNode editorJsonNode = converter.convertToJson(bpmnModel);

        return editorJsonNode.toString();
    }

    @Override
    public String getBpmnXmlByPid(String ProcessDefinitionId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(ProcessDefinitionId);
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel);
        return new String(bpmnBytes);
    }

    @Override
    public String getBpmnXmlByJson(String bpmJson) throws JsonProcessingException {
        ObjectNode modelNode = (ObjectNode) new ObjectMapper().readTree(bpmJson);
        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
        return new String(bpmnBytes);
    }

    @Override
    public String getBpmnJsonByXml(String bpmXml) throws UnsupportedEncodingException, XMLStreamException {
        // xml转bpmnModel
        InputStream bpmnStream = new ByteArrayInputStream(bpmXml.getBytes());// 获取bpmn2.0规范的xml
        XMLInputFactory xif = XMLInputFactory.newInstance();
        InputStreamReader in = new InputStreamReader(bpmnStream, "UTF-8");
        XMLStreamReader xtr = xif.createXMLStreamReader(in);
        // 然后转为bpmnModel
        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
        // bpmnModel转json
        ObjectNode objectNode = new BpmnJsonConverter().convertToJson(bpmnModel);
        return objectNode.toString();
    }

}
