package cn.xyz.commons.utils;

import cn.xyz.mianshi.utils.SKBeanUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class ExecUtil {

    public static Object exec(String beanName, String methodName, Object... args) {
        if (StringUtils.isAnyBlank(beanName, methodName)) return "beanName或methodName不能为空";
        Object bean = null;
        try {
            bean = SKBeanUtils.getBean(beanName);
        } catch (NoSuchBeanDefinitionException e) {
            log.warn("NoSuchBeanDefinitionException, beanName:{}", beanName);
        }
        if (null == bean) {
            try {
                bean = Class.forName(beanName).newInstance();
            } catch (ClassNotFoundException e) {
                return String.format("%s 不存在", beanName);
            } catch (IllegalAccessException e) {
                return "IllegalAccessException";
            } catch (InstantiationException e) {
                log.error("InstantiationException, beanName:{}, methodName:{}, args:{}", beanName, methodName, JSON.toJSON(args), e);
                return "InstantiationException";
            }
        }
        Class<?>[] argClasses = null;
        if (null != args && args.length > 0) {
            argClasses = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                argClasses[i] = arg.getClass();
            }
        }
        try {
            Method method = getMethod(bean.getClass(), methodName, argClasses);
            if (null == method) return "NoSuchMethodException";
            method.setAccessible(true);
            return method.invoke(bean, args);
        } catch (IllegalAccessException e) {
            return "IllegalAccessException";
        } catch (InvocationTargetException e) {
            log.error("InvocationTargetException, beanName:{}, methodName:{}, args:{}", beanName, methodName, JSON.toJSON(args), e);
            return "InvocationTargetException";
        }
    }

    public static Method getMethod(Class clazz, String methodName,
                                   final Class[] classes) {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, classes);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getMethod(methodName, classes);
            } catch (NoSuchMethodException ex) {
                if (clazz.getSuperclass() == null) {
                    return method;
                } else {
                    method = getMethod(clazz.getSuperclass(), methodName,
                            classes);
                }
            }
        }
        return method;
    }
}
