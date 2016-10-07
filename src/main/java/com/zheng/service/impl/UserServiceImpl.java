package com.zheng.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zheng.config.DynamicDataSourceAnn;
import com.zheng.dao.UserDao;
import com.zheng.domain.User;
import com.zheng.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDao;
	
	@Override
	@DynamicDataSourceAnn("ds3")
	public List<User> findList() {
		return userDao.findList();
	}

}
