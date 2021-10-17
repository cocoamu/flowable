package com.cocoamu.flowable.service.impl;

import com.cocoamu.flowable.conver.MyBpmnJsonConverter;
import com.cocoamu.flowable.service.MyModelService;
import com.fasterxml.jackson.databind.JsonNode;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MyModelServiceImpl implements MyModelService {

    private  BpmnJsonConverter bpmnJsonConverter;

    @PostConstruct
    public void init() {

        bpmnJsonConverter = new MyBpmnJsonConverter();
    }

    @Override
    public String saveModelJSON(JsonNode modelNode) {
//        BpmnModel bpmnModel = bpmnJsonConverter.convertToBpmnModel(modelNode);
//        org.flowable.bpmn.model.Process process = bpmnModel.getMainProcess();
//
//        //查询是否已经存在流程模板
//        Model newModel = new Model();
//        List<Model> models = modelRepository.findByKeyAndType(process.getId(), AbstractModel.MODEL_TYPE_BPMN);
//        if (CollectionUtils.isNotEmpty(models)) {
//            Model updateModel = models.get(0);
//            newModel.setId(updateModel.getId());
//        }
//        String name = process.getId();
//        if (StringUtils.isNotEmpty(process.getName())) {
//            name = process.getName();
//        }
//        String description = process.getDocumentation();
//        User createdBy = UserConverter.userTokenConverterToUser();
//
//        newModel.setName(name);
//        newModel.setKey(process.getId());
//        newModel.setModelType(AbstractModel.MODEL_TYPE_BPMN);
//        newModel.setCreated(Calendar.getInstance().getTime());
//        newModel.setCreatedBy(createdBy.getId());
//        newModel.setDescription(description);
//        newModel.setModelEditorJson(modelNode.toString());
//        newModel.setLastUpdated(Calendar.getInstance().getTime());
//        newModel.setLastUpdatedBy(createdBy.getId());
//        modelService.createModel(newModel, createdBy);
        return "ok";
    }
}
