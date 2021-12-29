package com.cocoamu.flowable.conver;

import com.cocoamu.flowable.constants.Constants;
import com.cocoamu.flowable.util.ExtensionAttributeUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.bpmn.model.BaseElement;
import org.flowable.bpmn.model.ExtensionAttribute;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.editor.language.json.converter.BaseBpmnJsonConverter;
import org.flowable.editor.language.json.converter.UserTaskJsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CustomizeUserTaskJsonConverter extends UserTaskJsonConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomizeUserTaskJsonConverter.class);

    public static void fillBpmnTypes(
            Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        convertersToJsonMap.put(UserTask.class, CustomizeUserTaskJsonConverter.class);
    }

    @Override
    protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode,
                                               Map<String, JsonNode> shapeMap) {
        FlowElement flowElement = super.convertJsonToElement(elementNode, modelNode, shapeMap);
        LOGGER.info("进入自定义属性解析");
        if (flowElement instanceof UserTask) {
            ObjectMapper objectMapper = new ObjectMapper();
            UserTask userTask = (UserTask) flowElement;
            try {
                LOGGER.info("节点:" + objectMapper.writeValueAsString(userTask));
            } catch (JsonProcessingException e) {
                LOGGER.error("节点序列化异常.");
            }

            Map<String, List<ExtensionAttribute>> atts = new HashMap<>();
            List<ExtensionAttribute> list = new ArrayList<>();

            String userSelector = getPropertyValueAsString(Constants.CUSTOM_ATTRIBUTES_USER_SELECTOR, elementNode);
            if (userSelector != null) {
                LOGGER.info("新增自定义属性["+Constants.CUSTOM_ATTRIBUTES_USER_SELECTOR+"]="+userSelector);
                ExtensionAttribute ea1 = ExtensionAttributeUtils.generate(Constants.CUSTOM_ATTRIBUTES_USER_SELECTOR, userSelector);
                list.add(ea1);
            }
            atts.put("CUSTOM-FLOWABLE-EXT",list);
            if (atts.size()>0){
                flowElement.setAttributes(atts);
            }
        }
        return flowElement;
    }
}