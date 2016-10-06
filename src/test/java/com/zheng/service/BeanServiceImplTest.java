package com.zheng.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class BeanServiceImplTest extends BaseServiceTest {

	@Autowired
	@Qualifier("beanServiceImpl2")
	private BeanService service;
	
	@Test
	public void testBeanService() {
		service.display();
	}
	
}
