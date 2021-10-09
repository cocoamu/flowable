package com.cocoamu.flowable.util;

import org.springframework.context.ApplicationContext;

public class FlowableUitls {

    private static volatile ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext ac) {
        if (applicationContext == null) {
            applicationContext = ac;
        }
    }

    public static ApplicationContext getApplicationContext() {
        AssertUtils.assertNotNull(applicationContext, "applicationContext is null");
        return applicationContext;
    }
}
