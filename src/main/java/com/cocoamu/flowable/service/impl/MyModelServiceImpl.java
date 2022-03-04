package com.cocoamu.flowable.service.impl;

import com.cocoamu.flowable.conver.CustomBpmnJsonConverter;
import com.cocoamu.flowable.entity.User;
import com.cocoamu.flowable.service.MyModelService;
import com.cocoamu.flowable.vo.ReturnVo;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.editor.language.json.converter.util.CollectionUtils;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.ui.modeler.domain.AbstractModel;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.repository.ModelRepository;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MyModelServiceImpl implements MyModelService {

    @Autowired
    private ModelRepository modelRepository ;
    @Autowired
    private ModelService modelService;
    @Autowired
    protected RepositoryService repositoryService;
    private final BpmnJsonConverter bpmnJsonConverter;

    public MyModelServiceImpl() {
        bpmnJsonConverter = new CustomBpmnJsonConverter();
    }

    @Override
    public ReturnVo saveModelJSON(JsonNode modelNode) {
        try {
            BpmnModel bpmnModel = bpmnJsonConverter.convertToBpmnModel(modelNode);
            org.flowable.bpmn.model.Process process = bpmnModel.getMainProcess();
            String name = process.getId();
            if (StringUtils.isNotEmpty(process.getName())) {
                name = process.getName();
            }
            String description = process.getDocumentation();
            User user = new User();
            user.setId("sys");
            user.setFirstName("sys");

            //查询是否已经存在流程模板
            Model newModel = new Model();
            List<Model> models = modelRepository.findByKeyAndType(process.getId(), AbstractModel.MODEL_TYPE_BPMN);
            if (CollectionUtils.isNotEmpty(models)) {
                Model updateModel = models.get(0);
                newModel.setId(updateModel.getId());
            }
            newModel.setName(name);
            newModel.setKey(process.getId());
            newModel.setModelType(AbstractModel.MODEL_TYPE_BPMN);
            newModel.setCreated(Calendar.getInstance().getTime());
            newModel.setCreatedBy(user.getId());
            newModel.setDescription(description);
            newModel.setModelEditorJson(modelNode.toString());
            newModel.setLastUpdated(Calendar.getInstance().getTime());
            newModel.setLastUpdatedBy(user.getId());
            modelService.createModel(newModel, user);
            //部署流程
            return this.deploy(newModel.getId());
        } catch (Exception e) {
            String msg = MessageFormat.format("saveModelerXml is error:{0}", e.getMessage());
            log.error(msg, e);
            return ReturnVo.fail(msg);
        }
    }

    @Override
    public ReturnVo<String> changeModelStatus(String processDefinitionId, Integer action) {
        if (action == 1) {
            repositoryService.activateProcessDefinitionById(processDefinitionId, true, null);
        } else {
            repositoryService.suspendProcessDefinitionById(processDefinitionId, true, null);
        }
        return ReturnVo.sucess("ok");
    }

    private ReturnVo deploy(String modelId) {
        try {
            Model model = modelService.getModel(modelId.trim());
            //到时候需要添加分类
            String categoryCode = "1000";
            BpmnModel bpmnModel = modelService.getBpmnModel(model);
            //必须指定文件后缀名否则部署不成功
            Deployment deploy = repositoryService.createDeployment()
                    .name(model.getName())
                    .key(model.getKey())
                    .category(categoryCode)
                    .tenantId(null)
                    .addBpmnModel(model.getKey() + ".bpmn", bpmnModel)
                    .deploy();
            Map<String,Object> resultMap = new HashMap<>();
            resultMap.put("deploy_id", deploy.getId());
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult();
            resultMap.put("process_id", processDefinition.getKey());
            resultMap.put("module_id", modelId);
            return ReturnVo.sucess(resultMap);
        } catch (Exception e) {
            String msg = MessageFormat.format("deploy is error  modelId:{0} messsage:{1}", modelId, e.getMessage());
            log.error(msg, e);
            return ReturnVo.fail(msg);
        }
    }
}
