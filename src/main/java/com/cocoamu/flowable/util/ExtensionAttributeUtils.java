package com.cocoamu.flowable.util;

import org.flowable.bpmn.model.ExtensionAttribute;

public class ExtensionAttributeUtils {
    public static ExtensionAttribute generate(String key, String val) {
        ExtensionAttribute ea = new ExtensionAttribute();
        ea.setNamespace("http://exexm.com.cn");
        ea.setName(key);
        ea.setNamespacePrefix("customize");
        ea.setValue(val);
        return ea;
    }
}
