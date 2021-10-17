package com.cocoamu.flowable.function;

import com.cocoamu.flowable.constants.MyFlowableConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.impl.el.function.AbstractFlowableVariableExpressionFunction;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
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
        log.info("处理特殊表达式:custom");
        //获取所有参数名数组
        String[] variables = StringUtils.split(variableNames, MyFlowableConstants.SEPARATOR);
        //获取第一个参数名，如果只有一个参数直接使用variableNames就可以
        String variableName1 = variables[0];

        ExecutionEntityImpl execution =  (ExecutionEntityImpl) variableScope;
        //获取流程实例id
        String processInstanceId =  execution.getProcessInstanceId();
        //从环境变量中根据变量名获取变量值
        Object variableValue = getVariableValue(variableScope, variables[0]);
        //判断参数值是否是数组类型，如果进行类型转换后比较
        if (variableValue instanceof Number) {
            return ((Number) variableValue).intValue() < 10;
        }
        return false;
    }

}