package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.nv.commons.constants.WebSiteType;
import com.nv.commons.model.database.DBQueryRunner;
import com.nv.module.engagement.constant.EngagementType;

/**
 *
 */
public class EngageMessageDeliverLogDAO {

	/**
	 * insert
	 */
	private static final int columnLength = 500;

	public static int insert(final Connection conn,
		WebSiteType websiteType,
		EngagementType engageType,
		int providerId,
		String providerName,
		String accountName,
		int countryTypeId,
		String sender,
		String receiver,
		String deliverResult,
		String purposeText
	) throws SQLException {

		// deliver_result VARCHAR2(50)
		if (deliverResult.length() > columnLength) {
			deliverResult = deliverResult.substring(0, columnLength);
		}

		final String sql =
			"INSERT INTO EngageMessageDeliverLog "
				+ "(website_type, engage_type, provider_id, provider_name, account_name, country_type, sender, receiver, deliver_result, purpose) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		return DBQueryRunner.update(conn, sql,
			websiteType.unique(),
			engageType.unique(),
			providerId,
			providerName,
			accountName,
			countryTypeId,
			sender,
			receiver,
			deliverResult,
			purposeText);
	}

	public static int deleteLogOlderThanTime(Connection conn, Timestamp queryTimestamp) throws SQLException {

		final String sql = "DELETE FROM EngageMessageDeliverLog WHERE create_time < ?";

		return DBQueryRunner.update(conn, sql, queryTimestamp);
	}
}
