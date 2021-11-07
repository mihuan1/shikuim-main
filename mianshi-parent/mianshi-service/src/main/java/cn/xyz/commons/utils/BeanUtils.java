package cn.xyz.commons.utils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;

public abstract class BeanUtils extends org.springframework.beans.BeanUtils {

	public static void copyProperties(Object source, Object target)
			throws BeansException {
		Assert.notNull(source, "Source must not be null");
		Assert.notNull(target, "Target must not be null");
		Class<?> actualEditable = target.getClass();
		PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
		for (PropertyDescriptor targetPd : targetPds) {
			if (targetPd.getWriteMethod() != null) {
				PropertyDescriptor sourcePd = getPropertyDescriptor(
						source.getClass(), targetPd.getName());
				if (sourcePd != null && sourcePd.getReadMethod() != null) {
					try {
						Method readMethod = sourcePd.getReadMethod();
						if (!Modifier.isPublic(readMethod.getDeclaringClass()
								.getModifiers())) {
							readMethod.setAccessible(true);
						}
						Object value = readMethod.invoke(source);// 这里判断以下value是否为空
																	// 当然这里也能进行一些特殊要求的处理
																	// 例如绑定时格式转换等等
						if (value != null) {
							Method writeMethod = targetPd.getWriteMethod();
							if (!Modifier.isPublic(writeMethod
									.getDeclaringClass().getModifiers())) {
								writeMethod.setAccessible(true);
							}
							writeMethod.invoke(target, value);
						}
					} catch (Throwable ex) {
						throw new FatalBeanException(
								"Could not copy properties from source to target",
								ex);
					}
				}
			}
		}
	}
	
	public static void populate(Object obj, Map<String, ?> map) {
		try {
			org.apache.commons.beanutils.BeanUtils.populate(obj, map);;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("map 转换 obj失败");
		} 
	}
	
	public static Map<String, Object> transBean2Map(Object obj) {
		 
		         if(obj == null){
		             return null;
		         }        
		         Map<String, Object> map = new HashMap<String, Object>();
		         try {
		             BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
		             PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		             for (PropertyDescriptor property : propertyDescriptors) {
		                 String key = property.getName();
		 
		                 // 过滤class属性
		                 if (!key.equals("class")) {
		                     // 得到property对应的getter方法
		                     Method getter = property.getReadMethod();
		                     Object value = getter.invoke(obj);
		 
		                     map.put(key, value);
		                 }
		 
		             }
		         } catch (Exception e) {
		             System.out.println("transBean2Map Error " + e);
		         }
		 
		         return map;
		 
		     
		 }
}
