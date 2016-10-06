package com.zheng.service;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class MultiDataSourceTest extends BaseServiceTest {

	@Autowired
	private DataSource dataSource;
	
	@Autowired
	@Qualifier("ds1")
	private DataSource dataSource1;
	@Autowired
	@Qualifier("ds2")
	private DataSource dataSource2;
	
	@Test
	public void testDataSource() {
		System.out.println(dataSource);
		System.out.println(dataSource1);
		System.out.println(dataSource2);
	}
	
}
