package com.cocoamu.flowable.util;

import org.slf4j.helpers.MessageFormatter;

public final class AssertUtils {

    public static void assertNotNull(Object target, String message, Object... params) {
        if (target == null) {
            throwAssertionException(message, params);
        }
    }

    private static void throwAssertionException(String message, Object[] params) {
        String errorMessage = params == null ? message : MessageFormatter.arrayFormat(message, params).getMessage();
        throw new AssertUtils.AssertionException(errorMessage);
    }

    public static class AssertionException extends RuntimeException {
        private static final long serialVersionUID = -1023297548643274521L;

        public AssertionException(String message) {
            super(message);
        }
    }
}