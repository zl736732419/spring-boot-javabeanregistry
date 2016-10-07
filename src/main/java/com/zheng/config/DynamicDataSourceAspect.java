package com.zheng.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 切换数据源
 * @author zhenglian
 */
@Aspect
@Order(-10)
@Component
public class DynamicDataSourceAspect {

	/**
	 * @annotation(targetDataSource)：
	 * 会拦截注解targetDataSource的方法，否则不拦截;
	 * 这里设置的注解对象参数名必须与方法参数中的注解名称相同
	 * @param point
	 * @param dynamicDataSourceAnn
	 */
	@Before("@annotation(dynamicDataSourceAnn)")
	public void changeDataSource(JoinPoint point, DynamicDataSourceAnn dynamicDataSourceAnn) {
		//获取当前使用的数据源
		String dsId = dynamicDataSourceAnn.value();
		if(!DynamicDataSourceContextHolder.containsDataSource(dsId)) {
			System.err.println("数据源[{}]不存在，使用默认数据源  > {}"+dynamicDataSourceAnn.value() + point.getSignature());
		}else {
			 System.out.println("Use DataSource : {} > {}"+dynamicDataSourceAnn.value()+point.getSignature());
			 //扎到直接设置到动态数据源上下文中
			 DynamicDataSourceContextHolder.setDataSourceType(dynamicDataSourceAnn.value());
		}
	}
	
	@After("@annotation(dynamicDataSourceAnn)")
	public void restoreDataSource(JoinPoint point, DynamicDataSourceAnn dynamicDataSourceAnn) {
		System.out.println("Revert DataSource : {} > {}"+dynamicDataSourceAnn.value()+point.getSignature());
		DynamicDataSourceContextHolder.clearDataSourceType();
	}
}
