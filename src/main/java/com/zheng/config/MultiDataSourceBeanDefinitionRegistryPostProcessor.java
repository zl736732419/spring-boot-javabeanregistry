package com.zheng.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.core.env.Environment;

/**
 * 这里是静态切换数据源的方式，一般不这么做，需要使用动态数据源切换的方式
 * 创建多数据源注册到Spring中
 *
 * 接口：BeanDefinitionRegistryPostProcessor只要是注入bean, 在上一节介绍过使用方式；
 * 
 * 接口：接口 EnvironmentAware 重写方法 setEnvironment
 * 可以在工程启动时，获取到系统环境变量和application配置文件中的变量。 这个第24节介绍过.
 * 
 * 
 * 方法的执行顺序是：
 * 
 * setEnvironment()-->postProcessBeanDefinitionRegistry() -->
 * postProcessBeanFactory()
 * 
 * 
 */
@Configuration
public class MultiDataSourceBeanDefinitionRegistryPostProcessor
		implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {
	// 名称生成器
	private BeanNameGenerator generator = new AnnotationBeanNameGenerator();
	// 作用域对象
	private ScopeMetadataResolver resolver = new AnnotationScopeMetadataResolver();
	// 如配置文件中未指定数据源类型，使用该默认值
	private static final Object DATASOURCE_TYPE_DEFAULT = "org.apache.tomcat.jdbc.pool.DataSource";
	// 存放DataSource配置的集合;
	private Map<String, Map<String, Object>> dataSourceMap = new HashMap<String, Map<String, Object>>();

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
		// 设置为主数据源
		factory.getBeanDefinition("dataSource").setPrimary(true);

		if (!dataSourceMap.isEmpty()) {
			BeanDefinition bd = null;
			Map<String, Object> dsMap = null;
			MutablePropertyValues mpv = null;
			for (Entry<String, Map<String, Object>> entry : dataSourceMap.entrySet()) {
				bd = factory.getBeanDefinition(entry.getKey()); //获取下面注册进去的datasource,设置对应的属性
				mpv = bd.getPropertyValues();
				dsMap = entry.getValue();
				mpv.addPropertyValue("driverClassName", dsMap.get("driverClassName"));
				mpv.addPropertyValue("url", dsMap.get("url"));
				mpv.addPropertyValue("username", dsMap.get("username"));
				mpv.addPropertyValue("password", dsMap.get("password"));
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		try {
			if (!dataSourceMap.isEmpty()) {
				// 不为空的时候，进行注册bean.
				for (Entry<String, Map<String, Object>> entry : dataSourceMap.entrySet()) {
					Object type = entry.getValue().get("type");// 获取数据源类型，没有设置为默认的数据源.
					if (type == null) {
						type = DATASOURCE_TYPE_DEFAULT;
					}
					registerBean(registry, entry.getKey(),
							(Class<? extends DataSource>) Class.forName(type.toString()));
				}
			}
		} catch (ClassNotFoundException e) {
			// 异常捕捉.
			e.printStackTrace();
		}
	}

	private void registerBean(BeanDefinitionRegistry registry, String name, Class<? extends DataSource> beanClass) {
		AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);
		// 单例还是原型等等...作用域对象.
		ScopeMetadata scopeMetadata = this.resolver.resolveScopeMetadata(abd);
		abd.setScope(scopeMetadata.getScopeName());
		String beanName = (name != null ? name : this.generator.generateBeanName(abd, registry));
		AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
	}

	@Override
	public void setEnvironment(Environment environment) {
		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, "custom.datasource.");
		String dsPrefixs = propertyResolver.getProperty("names");
		String[] dsPrefixsArr = dsPrefixs.split(",");
		for (String dsPrefix : dsPrefixsArr) {
			/*
			 * 获取到子属性，对应一个map; 也就是这个map的key就是
			 * type、driver-class-name等;
			 */
			Map<String, Object> dsMap = propertyResolver.getSubProperties(dsPrefix + ".");
			// 存放到一个map集合中，之后在注入进行使用.
			dataSourceMap.put(dsPrefix, dsMap);
		}

	}

}
