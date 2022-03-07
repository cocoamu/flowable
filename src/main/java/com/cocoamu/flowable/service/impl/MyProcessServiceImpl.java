package com.cocoamu.flowable.service.impl;

import com.cocoamu.flowable.cmd.ExpressionCmd;
import com.cocoamu.flowable.service.MyProcessService;
import com.cocoamu.flowable.vo.FlowElementVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.*;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.engine.*;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.ui.modeler.domain.AbstractModel;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.repository.ModelRepository;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MyProcessServiceImpl implements MyProcessService {
    @Autowired
    private ManagementService managementService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private ModelService modelService;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    ProcessEngineConfigurationImpl processEngineConfiguration;
    @Autowired
    private ModelRepository modelRepository;

    Logger log = LoggerFactory.getLogger(MyProcessServiceImpl.class);

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

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey, map);
        runtimeService.setVariable(processInstance.getProcessInstanceId(), "processInstance", processInstance.getProcessInstanceId());
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
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, engconf.getActivityFontName(), engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), 1.0, false);
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

    @Override
    public List<FlowElementVo> calApprovePath(String processId, Map<String, Object> variableMap, List<String> approveIds) {
        List<FlowElement> passElements = new ArrayList<>();
        BpmnModel bpmnModel;
        if (StringUtils.isNotBlank(processId)) {
            List<Model> models = modelRepository.findByKeyAndType(processId, AbstractModel.MODEL_TYPE_BPMN);
            if (CollectionUtils.isNotEmpty(models)) {
                Model model = models.get(0);
                bpmnModel = modelService.getBpmnModel(model);
                Collection<FlowElement> flowElements = bpmnModel.getMainProcess().getFlowElements();
                this.dueStartElement(passElements, flowElements, variableMap);
            }
        }
        return passElements.stream().filter(flowElement -> {
            if (approveIds.contains(flowElement.getId())){
                return true;
            }
            return false;
        }).map(flowElement -> new FlowElementVo(flowElement.getId(),flowElement.getName())).collect(Collectors.toList());
    }

    /**
     * 2. 找到开始节点，通过它的目标节点，然后再不断往下找。
     */
    private void dueStartElement(List<FlowElement> passElements, Collection<FlowElement> flowElements, Map<String, Object> variableMap) {
        Optional<FlowElement> startElementOpt = flowElements.stream().filter(flowElement -> flowElement instanceof StartEvent).findFirst();
        startElementOpt.ifPresent(startElement -> {
            flowElements.remove(startElement);
            List<SequenceFlow> outgoingFlows = ((StartEvent) startElement).getOutgoingFlows();
            String targetRef = outgoingFlows.get(0).getTargetRef();
            // 根据ID找到FlowElement
            FlowElement targetElementOfStartElement = getFlowElement(flowElements, targetRef);
            if (targetElementOfStartElement instanceof UserTask) {
                this.getPassElementList(passElements, flowElements, targetElementOfStartElement, variableMap);
            }
        });
    }

    private FlowElement getFlowElement(Collection<FlowElement> flowElements, String targetRef) {
        return flowElements.stream().filter(flowElement -> targetRef.equals(flowElement.getId())).findFirst().orElse(null);
    }

    /**
     * 3. 我只用到了UserTask、ExclusiveGateway、ParallelGateway，所以代码里只列举了这三种，如果用到了其他的，可以再自己补充
     */
    private void getPassElementList(List<FlowElement> passElements, Collection<FlowElement> flowElements, FlowElement curFlowElement, Map<String, Object> variableMap) {
        // 任务节点
        if (curFlowElement instanceof UserTask) {
            this.dueUserTaskElement(passElements, flowElements, curFlowElement, variableMap);
            return;
        }
        // 排他网关
        if (curFlowElement instanceof ExclusiveGateway) {
            this.dueExclusiveGateway(passElements, flowElements, curFlowElement, variableMap);
            return;
        }
        // 并行网关
        if (curFlowElement instanceof ParallelGateway) {
            this.dueParallelGateway(passElements, flowElements, curFlowElement, variableMap);
        }
    }

    private void dueUserTaskElement(List<FlowElement> passElements, Collection<FlowElement> flowElements, FlowElement curFlowElement, Map<String, Object> variableMap) {
        passElements.add(curFlowElement);
        List<SequenceFlow> outgoingFlows = ((UserTask) curFlowElement).getOutgoingFlows();
        String targetRef = outgoingFlows.get(0).getTargetRef();
        if (outgoingFlows.size() > 1) {
            // 找到表达式成立的sequenceFlow
            SequenceFlow sequenceFlow = getSequenceFlow(variableMap, outgoingFlows);
            targetRef = sequenceFlow.getTargetRef();
        }
        // 根据ID找到FlowElement
        FlowElement targetElement = getFlowElement(flowElements, targetRef);
        this.getPassElementList(passElements, flowElements, targetElement, variableMap);
    }

    private void dueExclusiveGateway(List<FlowElement> passElements, Collection<FlowElement> flowElements, FlowElement curFlowElement, Map<String, Object> variableMap) {
        // 获取符合条件的sequenceFlow的目标FlowElement
        List<SequenceFlow> exclusiveGatewayOutgoingFlows = ((ExclusiveGateway) curFlowElement).getOutgoingFlows();
        flowElements.remove(curFlowElement);
        // 找到表达式成立的sequenceFlow
        SequenceFlow sequenceFlow = getSequenceFlow(variableMap, exclusiveGatewayOutgoingFlows);
        // 根据ID找到FlowElement
        FlowElement targetElement = getFlowElement(flowElements, sequenceFlow.getTargetRef());
        this.getPassElementList(passElements, flowElements, targetElement, variableMap);
    }

    private void dueParallelGateway(List<FlowElement> passElements, Collection<FlowElement> flowElements, FlowElement curFlowElement, Map<String, Object> variableMap) {
        FlowElement targetElement;
        List<SequenceFlow> parallelGatewayOutgoingFlows = ((ParallelGateway) curFlowElement).getOutgoingFlows();
        for (SequenceFlow sequenceFlow : parallelGatewayOutgoingFlows) {
            targetElement = getFlowElement(flowElements, sequenceFlow.getTargetRef());
            this.getPassElementList(passElements, flowElements, targetElement, variableMap);
        }
    }

    /**
     * 4. 根据传入的变量，计算出表达式成立的那一条SequenceFlow
     *
     * @param variableMap
     * @param outgoingFlows
     * @return
     */
    private SequenceFlow getSequenceFlow(Map<String, Object> variableMap, List<SequenceFlow> outgoingFlows) {
        Optional<SequenceFlow> sequenceFlowOpt = outgoingFlows.stream().filter(item -> {
            try {
                return this.getElValue(item.getConditionExpression(), variableMap);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return false;
            }
        }).findFirst();
        return sequenceFlowOpt.orElse(outgoingFlows.get(0));
    }

    private boolean getElValue(String exp, Map<String, Object> variableMap) {
        return managementService.executeCommand(new ExpressionCmd(runtimeService, processEngineConfiguration, null, exp, variableMap));
    }

}
