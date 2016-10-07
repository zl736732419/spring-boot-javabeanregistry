package com.zheng.config;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态数据源上下文
 * @author zhenglian
 */
public class DynamicDataSourceContextHolder {
	private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();
	/**
	 * 主要用于管理所有的数据源id 判断数据源是否存在
	 */
	public static List<String> dataSourceIds = new ArrayList<>();

	public static void setDataSourceType(String dataSourceType) {
		contextHolder.set(dataSourceType);
	}

	public static String getDataSourceType() {
		return contextHolder.get();
	}

	public static void clearDataSourceType() {
		contextHolder.remove();
	}

	/**
	 * 判断当前datasource是否存在
	 * @param dataSourceId
	 * @return
	 */
	public static boolean containsDataSource(String dataSourceId) {
		return dataSourceIds.contains(dataSourceId);
	}
}
