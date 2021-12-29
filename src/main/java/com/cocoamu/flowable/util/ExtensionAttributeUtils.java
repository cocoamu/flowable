package com.cocoamu.flowable.util;

import org.flowable.bpmn.model.ExtensionAttribute;

public class ExtensionAttributeUtils {
    public static ExtensionAttribute generate(String key, String val) {
        ExtensionAttribute ea = new ExtensionAttribute();
        ea.setNamespace("http://flowable.org/bpmn");
        ea.setName(key);
        ea.setNamespacePrefix("custom");
        ea.setValue(val);
        return ea;
    }
}
