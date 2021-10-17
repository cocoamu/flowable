package com.cocoamu.flowable.conver;

import org.flowable.editor.language.json.converter.BpmnJsonConverter;

public class MyBpmnJsonConverter extends BpmnJsonConverter {
    static {
        convertersToBpmnMap.put(STENCIL_TASK_USER, MyCustomizeUserTaskJsonConverter.class);
    }
}
