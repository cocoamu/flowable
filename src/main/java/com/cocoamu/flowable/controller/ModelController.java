package com.cocoamu.flowable.controller;

import com.cocoamu.flowable.service.MyModelService;
import com.cocoamu.flowable.vo.ReturnVo;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/model")
public class ModelController {
    @Autowired
    private MyModelService modelService;

    /**
     * 存储模型json (新增及修改接口)
     *
     * @param modelNode
     * @return
     */
    @RequestMapping("/saveModelJSON")
    public ReturnVo saveModelJSON(@RequestBody JsonNode modelNode) {
        return modelService.saveModelJSON(modelNode);
    }

    /**
     * 激活或者挂起模板
     * @param processDefinitionId 模板id
     * @param action 动作 0挂起 1激活
     * @return
     */
    @RequestMapping(value = "/activateProcessDefinitionById")
    public ReturnVo<String> changeModelStatus(String processDefinitionId,Integer action) {
        return modelService.changeModelStatus(processDefinitionId, action);
    }
}
