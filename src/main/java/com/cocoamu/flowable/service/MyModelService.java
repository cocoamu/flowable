package com.cocoamu.flowable.service;

import com.cocoamu.flowable.vo.ReturnVo;
import com.fasterxml.jackson.databind.JsonNode;

public interface MyModelService {

    /**
     * 保存模型json数据
     * @param modelNode json数据
     * @return
     */
    ReturnVo saveModelJSON(JsonNode modelNode);

    /**
     * 修改模型状态
     * @param processDefinitionId 模板id
     * @param action 动作：0挂起 1激活
     * @return
     */
    ReturnVo<String> changeModelStatus(String processDefinitionId,Integer action);
}
