package com.cocoamu.flowable.service.impl;

import com.cocoamu.flowable.cmd.ExpressionCmd;
import com.cocoamu.flowable.constants.Constants;
import com.cocoamu.flowable.dto.UpdateElementDto;
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
import java.util.concurrent.atomic.AtomicBoolean;
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
    public ProcessInstance startProcess(String processKey,List<UpdateElementDto> expressList) {
        //???????????????map????????????????????????????????????????????????
        Map<String, Object> map = new HashMap();

        //????????????????????????????????????????????????
        ArrayList<String> list = new ArrayList<>();
        list.add("??????4");
        list.add("??????4");
        list.add("??????4");
        map.put("assigneeList", list);
        map.put("signCount", 0);
        //??????????????????????????????????????????????????????,???????????????????????????????????????????????????
        if (CollectionUtils.isNotEmpty(expressList)){
            map.put(expressList.get(0).getElementId()+"_"+Constants.CUSTOM_ATTRIBUTES_USER_SELECTOR,expressList.get(0).getElementAttr());
        }

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
        // xml???bpmnModel
        InputStream bpmnStream = new ByteArrayInputStream(bpmXml.getBytes());// ??????bpmn2.0?????????xml
        XMLInputFactory xif = XMLInputFactory.newInstance();
        InputStreamReader in = new InputStreamReader(bpmnStream, "UTF-8");
        XMLStreamReader xtr = xif.createXMLStreamReader(in);
        // ????????????bpmnModel
        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
        // bpmnModel???json
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
                bpmnModel = modelService.getBpmnModel(models.get(0));
                Collection<FlowElement> flowElements = bpmnModel.getMainProcess().getFlowElements();
                this.dueStartElement(passElements, flowElements, variableMap);
            }
            return passElements.stream().filter(flowElement -> {
                if (!org.springframework.util.CollectionUtils.isEmpty(approveIds)) {
                    return approveIds.contains(flowElement.getId());
                }
                return true;
            }).map(flowElement -> new FlowElementVo(flowElement.getId(),flowElement.getName())).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????
     */
    private void dueStartElement(List<FlowElement> passElements, Collection<FlowElement> flowElements, Map<String, Object> variableMap) {
        Optional<FlowElement> startElementOpt = flowElements.stream().filter(flowElement -> flowElement instanceof StartEvent).findFirst();
        startElementOpt.ifPresent(startElement -> {
            flowElements.remove(startElement);
            List<SequenceFlow> outgoingFlows = ((StartEvent) startElement).getOutgoingFlows();
            String targetRef = outgoingFlows.get(0).getTargetRef();
            // ??????ID??????FlowElement
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
     * ???????????????UserTask???ExclusiveGateway???ParallelGateway?????????????????????????????????????????????????????????
     */
    private void getPassElementList(List<FlowElement> passElements, Collection<FlowElement> flowElements, FlowElement curFlowElement, Map<String, Object> variableMap) {
        // ????????????
        if (curFlowElement instanceof UserTask) {
            this.dueUserTaskElement(passElements, flowElements, curFlowElement, variableMap);
            return;
        }
        // ????????????
        if (curFlowElement instanceof ExclusiveGateway) {
            this.dueExclusiveGateway(passElements, flowElements, curFlowElement, variableMap);
            return;
        }
        // ????????????
        if (curFlowElement instanceof ParallelGateway) {
            this.dueParallelGateway(passElements, flowElements, curFlowElement, variableMap);
        }
    }

    private void dueUserTaskElement(List<FlowElement> passElements, Collection<FlowElement> flowElements, FlowElement curFlowElement, Map<String, Object> variableMap) {
        passElements.add(curFlowElement);
        List<SequenceFlow> outgoingFlows = ((UserTask) curFlowElement).getOutgoingFlows();
        String targetRef = outgoingFlows.get(0).getTargetRef();
        if (outgoingFlows.size() > 1) {
            // ????????????????????????sequenceFlow
            SequenceFlow sequenceFlow = getSequenceFlow(variableMap, outgoingFlows);
            targetRef = sequenceFlow.getTargetRef();
        }
        // ??????ID??????FlowElement
        FlowElement targetElement = getFlowElement(flowElements, targetRef);
        this.getPassElementList(passElements, flowElements, targetElement, variableMap);
    }

    private void dueExclusiveGateway(List<FlowElement> passElements, Collection<FlowElement> flowElements, FlowElement curFlowElement, Map<String, Object> variableMap) {
        // ?????????????????????sequenceFlow?????????FlowElement
        List<SequenceFlow> exclusiveGatewayOutgoingFlows = ((ExclusiveGateway) curFlowElement).getOutgoingFlows();
        flowElements.remove(curFlowElement);
        AtomicBoolean hasCusExpress = new AtomicBoolean(false);
        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        exclusiveGatewayOutgoingFlows.stream().forEach(sequenceFlow -> {
            if (StringUtils.isNotEmpty(sequenceFlow.getConditionExpression())){
                if (sequenceFlow.getConditionExpression().contains(Constants.CUSTOM_FUNC)){
                    hasCusExpress.set(true);
                    // ??????ID??????FlowElement
                    FlowElement targetElement = getFlowElement(flowElements, sequenceFlow.getTargetRef());
                    this.getPassElementList(passElements, flowElements, targetElement, variableMap);
                }
            }
        });
       //???????????????????????????????????????????????????????????????
        if (!hasCusExpress.get()){
            // ????????????????????????sequenceFlow
            SequenceFlow sequenceFlow = getSequenceFlow(variableMap, exclusiveGatewayOutgoingFlows);
            // ??????ID??????FlowElement
            FlowElement targetElement = getFlowElement(flowElements, sequenceFlow.getTargetRef());
            this.getPassElementList(passElements, flowElements, targetElement, variableMap);
        }
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
     * ????????????????????????????????????????????????????????????SequenceFlow
     *
     * @param variableMap
     * @param outgoingFlows
     * @return
     */
    private SequenceFlow getSequenceFlow(Map<String, Object> variableMap, List<SequenceFlow> outgoingFlows) {
        //?????????????????????????????????????????????????????????????????????null????????????????????????Comparator.nullsLast?????????????????????
        outgoingFlows.stream().forEach(sequenceFlow -> {
            if (StringUtils.isEmpty(sequenceFlow.getConditionExpression())){
                sequenceFlow.setConditionExpression(null);
            }
        });
        //?????????????????????????????????????????????????????????????????????????????????????????????????????????cmd?????????????????????????????????true
        List<SequenceFlow> outgoingFlowsSort = outgoingFlows.stream().sorted(Comparator.comparing(SequenceFlow::getConditionExpression, Comparator.nullsLast(String::compareTo))).collect(Collectors.toList());

        Optional<SequenceFlow> sequenceFlowOpt = outgoingFlowsSort.stream().filter(item -> {
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
