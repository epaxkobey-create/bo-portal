package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nv.commons.dto.ProviderAgent;
import com.nv.commons.model.database.DBQueryRunner;
import com.nv.commons.model.database.DataBeanProcessor;

public class ProviderAgentDAO {

	public static List<ProviderAgent> getAll(Connection conn) throws SQLException{
		String sql = "SELECT * FROM providerAgent";

		return DBQueryRunner.getBeanList(conn, ProviderAgent.class, sql);
	}

	public static List<ProviderAgent> get(Connection conn, Timestamp updateTime) throws SQLException {
		String sql = " SELECT * FROM providerAgent WHERE update_time > ? ";

		return DBQueryRunner.getBeanList(conn, ProviderAgent.class, sql, updateTime);
	}

	public static Map<Integer, ProviderAgent> getMap(Connection conn) throws SQLException{
		String sql = "SELECT * FROM providerAgent";

		Map<Integer, ProviderAgent> resultMap = new HashMap<>();

		DataBeanProcessor<ProviderAgent> processor = (rs, bean) -> resultMap.put(bean.getId(), bean);

		DBQueryRunner.processBeanResult(conn, processor,ProviderAgent.class, sql);

		return resultMap;
	}

}
