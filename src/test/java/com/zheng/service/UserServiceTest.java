package com.zheng.service;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.zheng.domain.User;

public class UserServiceTest extends BaseServiceTest {
	@Autowired
	private UserService userService;
	
	@Test
	public void testList() {
		List<User> list = userService.findList();
		System.out.println(list);
		System.out.println(list.size());
	}
}
