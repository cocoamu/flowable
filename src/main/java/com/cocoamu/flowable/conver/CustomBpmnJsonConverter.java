package com.cocoamu.flowable.conver;

import org.flowable.editor.language.json.converter.BpmnJsonConverter;

public class CustomBpmnJsonConverter extends BpmnJsonConverter {
    static {
        convertersToBpmnMap.put(STENCIL_TASK_USER, CustomizeUserTaskJsonConverter.class);
    }
}
