package com.zheng.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.Configuration;

import com.zheng.service.impl.BeanServiceImpl1;
import com.zheng.service.impl.BeanServiceImpl2;

@Configuration
public class MyBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
	
	private BeanNameGenerator generator = new AnnotationBeanNameGenerator();
	

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory registry) throws BeansException {
		//这里可以获取我们在下面方法中注册的bean，在这里可以进行属性的设置操作
		// TODO Auto-generated method stub
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		//在这里进行javabean注册
		registryBean(registry, "beanServiceImpl1", BeanServiceImpl1.class);
		registryBean(registry, "beanServiceImpl2", BeanServiceImpl2.class);
		
	}

	/**
	 * 注册bean
	 * @param registry
	 * @param name
	 * @param clazz
	 */
	private void registryBean(BeanDefinitionRegistry registry, String name, Class<?> clazz) {
		AnnotatedBeanDefinition definition = new AnnotatedGenericBeanDefinition(clazz);
		//可以自动生成name
		String beanName = name != null ? name : this.generator.generateBeanName(definition, registry);
		//bean注册的holer类.
		BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, beanName);
		//使用bean注册工具类进行注册
		BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
		
		
	}

}
