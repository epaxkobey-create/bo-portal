package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.dto.RoleResource;
import com.nv.commons.model.database.DBQueryRunner;

public class RoleResourceDAO {

	public static List<RoleResource> queryAllFunction(Connection conn) throws SQLException {
		String sql = """
			SELECT * FROM roleResource
			WHERE status = ?
			ORDER BY parent_id ASC, display_order ASC
			""";
		return DBQueryRunner.getBeanList(conn, RoleResource.class, sql, BinaryStatusType.ACTIVE.unique());
	}
}
