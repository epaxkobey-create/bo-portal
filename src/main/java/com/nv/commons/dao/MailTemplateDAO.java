package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.nv.commons.dto.MailTemplate;
import com.nv.commons.model.database.DBQueryRunner;

public class MailTemplateDAO {

	public static List<MailTemplate> getAll(Connection conn) throws SQLException {
		String sql = " SELECT * FROM mailTemplate ";

		return DBQueryRunner.getBeanList(conn, MailTemplate.class, sql);
	}


	public static List<MailTemplate> findLatestUpdate(Connection conn, Timestamp latestUpdateTime)
		throws SQLException {
		String sql = " SELECT * FROM mailTemplate where update_time > ?";

		return DBQueryRunner.getBeanList(conn, MailTemplate.class, sql, latestUpdateTime);
	}

}
