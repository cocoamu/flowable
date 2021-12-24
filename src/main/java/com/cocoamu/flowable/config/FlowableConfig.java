package com.cocoamu.flowable.config;

import com.cocoamu.flowable.function.CustomExpressionFunction;
import com.cocoamu.flowable.util.FlowableUitls;
import lombok.extern.slf4j.Slf4j;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@Slf4j
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {

    @Autowired
    ApplicationContext applicationContext;

    /**
     * 初始化spring上下文
     */
    @PostConstruct
    public void init() {
        FlowableUitls.setApplicationContext(applicationContext);
    }

    @Override
    public void configure(SpringProcessEngineConfiguration engineConfiguration) {
        //通过传入流程ID生成当前流程的流程图给前端,如果流程中使用到中文且生成的图片是乱码的，则需要进配置下字体：
        engineConfiguration.setActivityFontName("宋体");
        engineConfiguration.setLabelFontName("宋体");
        engineConfiguration.setAnnotationFontName("宋体");
        //配置扩展表达式解析方法
        initExpressFunction(engineConfiguration);
    }

    /**
     *
     *  配置扩展表达式解析方法
     * @param springProcessEngineConfiguration
     */
    private void initExpressFunction(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
        log.info("配置扩展表达式解析方法");
        String variableScopeName = "execution";
        springProcessEngineConfiguration.initShortHandExpressionFunctions();
        springProcessEngineConfiguration.getShortHandExpressionFunctions().add(new CustomExpressionFunction(variableScopeName));
    }
}