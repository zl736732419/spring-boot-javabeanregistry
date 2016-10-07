package com.zheng.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.zheng.dao.UserDao;
import com.zheng.domain.User;

@Repository
public class UserDaoImpl implements UserDao {

	@Autowired
	private JdbcTemplate template;
	
	@Override
	public List<User> findList() {
		String sql = "select * from user ";
		List<User> list = template.query(sql, new RowMapper<User>(){
			@Override
			public User mapRow(ResultSet rs, int rowNum) throws SQLException {
				User user = new User();
				user.setId(rs.getLong("id"));
				user.setUsername(rs.getString("username"));
				user.setPassword(rs.getString("password"));
				user.setLocked(rs.getInt("locked"));
				return user;
			}
		});
		return list;
	}

	
}
