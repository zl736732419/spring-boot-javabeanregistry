package com.zheng.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.bind.RelaxedDataBinder;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

public class DynamicDataSourceRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware {
	// 如配置文件中未指定数据源类型，使用该默认值
	private static final Object DATASOURCE_TYPE_DEFAULT = "org.apache.tomcat.jdbc.pool.DataSource";
	private ConversionService conversionService = new DefaultConversionService();
	private PropertyValues dataSourcePropertyValues;
	// 默认数据源
	private DataSource defaultDataSource;

	private Map<String, DataSource> customDataSources = new HashMap<String, DataSource>();

	@Override
	public void setEnvironment(Environment environment) {
		initDefaultDataSource(environment);
		initCustomDataSource(environment);
	}

	private void initCustomDataSource(Environment env) {
		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "custom.datasource.");
		String dsPrefixs = propertyResolver.getProperty("names");
		for (String dsPrefix : dsPrefixs.split(",")) {// 多个数据源
			Map<String, Object> dsMap = propertyResolver.getSubProperties(dsPrefix + ".");
			DataSource ds = buildDataSource(dsMap);
			customDataSources.put(dsPrefix, ds);
			dataBinder(ds, env);
		}
	}

	private void initDefaultDataSource(Environment env) {
		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "spring.datasource.");
		Map<String, Object> dsMap = new HashMap<>();
		dsMap.put("type", propertyResolver.getProperty("type"));
		dsMap.put("driverClassName", propertyResolver.getProperty("driverClassName"));
		dsMap.put("url", propertyResolver.getProperty("url"));
		dsMap.put("username", propertyResolver.getProperty("username"));
		dsMap.put("password", propertyResolver.getProperty("password"));
		defaultDataSource = buildDataSource(dsMap);
		dataBinder(defaultDataSource, env);
	}

	/**
	 * 为DataSource绑定更多数据
	 * 
	 * @param dataSource
	 * @param env
	 */
	private void dataBinder(DataSource dataSource, Environment env) {
		RelaxedDataBinder binder = new RelaxedDataBinder(dataSource);
		binder.setConversionService(conversionService);
		binder.setIgnoreInvalidFields(false);
		binder.setIgnoreNestedProperties(false);
		binder.setIgnoreUnknownFields(true);

		if (dataSourcePropertyValues == null) {
			Map<String, Object> rpr = new RelaxedPropertyResolver(env, "spring.datasource").getSubProperties(".");
			Map<String, Object> values = new HashMap<>(rpr);
			// 排除已经设置的属性
			values.remove("type");
			values.remove("driverClassName");
			values.remove("url");
			values.remove("username");
			values.remove("password");
			dataSourcePropertyValues = new MutablePropertyValues(values);
		}

		binder.bind(dataSourcePropertyValues);
	}

	@SuppressWarnings({ "unchecked" })
	private DataSource buildDataSource(Map<String, Object> dsMap) {
		Object type = dsMap.get("type");
		if (type == null) {
			type = DATASOURCE_TYPE_DEFAULT;
		}

		Class<? extends DataSource> dataSourceType = null;
		try {
			dataSourceType = (Class<? extends DataSource>) Class.forName((String) type);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		String driverClassName = dsMap.get("driverClassName").toString();
		String url = dsMap.get("url").toString();
		String username = dsMap.get("username").toString();
		String password = dsMap.get("password").toString();

		DataSourceBuilder factory = DataSourceBuilder.create().driverClassName(driverClassName).url(url)
				.username(username).password(password).type(dataSourceType);
		return factory.build();
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata meta, BeanDefinitionRegistry registry) {
		Map<String, Object> targetDataSources = new HashMap<>();
		String dataSourceId = "dataSource";
		targetDataSources.put(dataSourceId, defaultDataSource);
		DynamicDataSourceContextHolder.dataSourceIds.add(dataSourceId);

		targetDataSources.putAll(customDataSources);
		for (String key : customDataSources.keySet()) {
			DynamicDataSourceContextHolder.dataSourceIds.add(key);
		}

		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setBeanClass(DynamicDataSource.class);
		beanDefinition.setSynthetic(true);
		MutablePropertyValues mpv = beanDefinition.getPropertyValues();
		mpv.addPropertyValue("defaultTargetDataSource", defaultDataSource);
		mpv.addPropertyValue("targetDataSources", targetDataSources);
		registry.registerBeanDefinition("dataSource", beanDefinition);
	}
}
