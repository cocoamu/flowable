package com.cocoamu.flowable.cmd;

import com.cocoamu.flowable.constants.Constants;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;

import java.util.Map;

public class ExpressionCmd implements Command<Boolean> {
    protected RuntimeService runtimeService;

    protected ProcessEngineConfigurationImpl processEngineConfiguration;

    protected String processInstanceId;

    protected String exp;

    protected Map<String, Object> variableMap;

    public ExpressionCmd(RuntimeService runtimeService, ProcessEngineConfigurationImpl processEngineConfiguration, String processInstanceId, String exp, Map<String, Object> variableMap) {
        this.runtimeService = runtimeService;
        this.processEngineConfiguration = processEngineConfiguration;
        this.processInstanceId = processInstanceId;
        this.exp = exp;
        this.variableMap = variableMap;
    }

    @Override
    public Boolean execute(CommandContext commandContext) {
        //判断是空表达式会直接返回，这边不用关心默认表达式会先执行的问题，在外面已经排序过了
        if (StringUtils.isEmpty(this.exp)) {
            return true;
        }
        if(this.exp.contains(Constants.CUSTOM_FUNC)){
            //自定义函数处理，这边可以根据具体的业务逻辑判断，比如调用其他服务接口判断
            //这边后面需求又改了一下，自定义函数的节点要无条件返回，但是测试了下，这边返回true，那第二条路不会进来，两条都返回false，那还是会默认返回第一条，
            //所以这边直接返回true，在其他地方做处理,不处理的话会少一些节点
            return true;
        }
        //下面都是使用原生的el表达式，先设置环境变量，再调用内置的方法计算表达式的结果
        Expression expression = processEngineConfiguration.getExpressionManager().createExpression(this.exp);
        ExecutionEntity executionEntity;
        if(StringUtils.isNotBlank(this.processInstanceId)){
            executionEntity = (ExecutionEntity) runtimeService.createProcessInstanceQuery().processInstanceId(this.processInstanceId).includeProcessVariables().singleResult();
        }else {
            executionEntity = new ExecutionEntityImpl();
            executionEntity.setVariables(variableMap);
        }
        Object value = expression.getValue(executionEntity);
        return value != null && "true".equals(value.toString());
    }
}