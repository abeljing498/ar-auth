package com.lyentech.bdc.md.auth.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext context = null;

    /**
     * * 设置上下文
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.context = applicationContext;
    }

    /**
     * * 获取上下文
     * @return
     */
    public static ApplicationContext getApplicationContext(){
        return context;
    }

    /**
     * * 根据名称获取Bean
     * @param BeanName
     * @param <T>
     * @return
     */
    public static <T> T getBean(String BeanName){
        return (T)context.getBean(BeanName);
    }

    /**
     * * 根据类型获取Bean
     * @param requiredType
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> requiredType){
        return (T) context.getBean(requiredType);

    }
}
