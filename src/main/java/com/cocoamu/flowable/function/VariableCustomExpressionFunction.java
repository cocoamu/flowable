package com.cocoamu.flowable.function;

import com.cocoamu.flowable.constants.MyFlowableConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.impl.el.function.AbstractFlowableVariableExpressionFunction;
import org.flowable.variable.api.delegate.VariableScope;

/**
 * @ClassName: VariableQueryExpressionFunction
 * @Author: ren
 * @Description:
 * @CreateTime： 2020/3/19 0019 下午 9:37
 * @Version：
 **/
@Slf4j
public class VariableCustomExpressionFunction extends AbstractFlowableVariableExpressionFunction {

    public VariableCustomExpressionFunction(String variableScopeName) {
        super(variableScopeName, "custom");
    }

    @Override
    protected boolean isMultiParameterFunction() {
        return false;
    }

    /**
     * 实现自定义函数,与上面构造方法中的函数名相同
     * @param variableScope
     * @param variableNames
     * @return
     */
    public static boolean custom(VariableScope variableScope, String variableNames) {

        log.info("处理特殊表达式:method02");

        String[] variables = StringUtils.split(variableNames, MyFlowableConstants.SEPARATOR);
        Object variableValue = getVariableValue(variableScope, variables[0]);

        if (variableValue instanceof Number) {
            return ((Number) variableValue).intValue() < 10;
        }

        return false;
    }

}