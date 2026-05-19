package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.nv.commons.dto.AccountProvider;
import com.nv.commons.model.database.DBQueryRunner;
import com.nv.commons.utils.OracleUtils;

/**
 * @author Luke Chi
 */
public class AccountProviderDAO {

	public static List<AccountProvider> findAliveByWebsiteAndTime(Connection conn, int websiteType, int hours)
		throws SQLException {
		String sql = "SELECT * FROM accountProvider A "
			+ " WHERE (website_type,user_id) in "
			+ "(SELECT website_type,user_id FROM accountProvider B  WHERE B.provider_update_time > (SYSTIMESTAMP - ( ? / 24)) AND WEBSITE_TYPE= ? ) "
			+ "    AND WEBSITE_TYPE = ?";

		return DBQueryRunner.getBeanList(conn, AccountProvider.class, sql, hours, websiteType, websiteType);
	}

	public static List<AccountProvider> findByWebsitesAndUserIds(Connection conn, String[][] userKeys)
		throws SQLException {
		String sql = "SELECT * FROM accountprovider WHERE (user_id, website_type) IN (" + OracleUtils.getGroupCondition(
			userKeys.length) + " )";

		return DBQueryRunner.getBeanList(conn, AccountProvider.class, sql,
			OracleUtils.getOracleARRAY(conn, "Q_COMBINE_USER_ID_ARRAY", userKeys));
	}


	public static List<AccountProvider> findLatestUpdate(Connection conn, Timestamp latestUpdateTime)
		throws SQLException {
		String sql = " SELECT * FROM accountProvider WHERE provider_update_time > ? ";

		return DBQueryRunner.getBeanList(conn, AccountProvider.class, sql, latestUpdateTime);
	}


	public static void insert(Connection conn, AccountProvider accountProvider) throws SQLException {
		String sql =
			" INSERT INTO accountprovider(user_id, website_type, provider_id, provider_account, provider_password, "
				+ " provider_balance, provider_create_time, provider_update_time, currency_type_id, provider_extra_data, bonus_turnover_id) "
				+ " VALUES(?,?,?,?,?,?,SYSTIMESTAMP, SYSTIMESTAMP,?,?,?) ";

		Object[] params = {accountProvider.getUserId(),
			accountProvider.getWebsiteType(),
			accountProvider.getProviderId(),
			accountProvider.getProviderAccount(),
			accountProvider.getProviderPassword(),
			accountProvider.getProviderBalance(),
			accountProvider.getCurrencyTypeId(),
			accountProvider.getProviderExtraData(),
			1 // temp solution
		};

		DBQueryRunner.update(conn, sql, params);
	}



	public static AccountProvider getAccountProvider(Connection conn, String userId, int websiteType,
		int providerId) throws SQLException {
		String sql = " SELECT * FROM accountprovider WHERE user_id = ? AND website_type = ? AND provider_id = ? ";

		return DBQueryRunner
			.getBean(conn, AccountProvider.class, sql, userId, websiteType, providerId);
	}

	public static AccountProvider getAccountProviderFromProviderAccount(Connection conn, int websiteType,
		int providerId, String providerAccount) throws SQLException {
		String sql = " SELECT * FROM accountprovider WHERE website_type = ? AND provider_id = ? AND provider_account = ? ";

		return DBQueryRunner
			.getBean(conn, AccountProvider.class, sql, websiteType, providerId, providerAccount);
	}

	public static List<AccountProvider> getAccountProviderList(Connection conn, int websiteType, String userId)
		throws SQLException {
		String sql = " SELECT * FROM accountProvider WHERE user_id = ? AND website_type = ? ";

		return DBQueryRunner.getBeanList(conn, AccountProvider.class, sql, userId, websiteType);
	}

	public static boolean exists(Connection conn, int websiteType, int providerId, String userId)
		throws SQLException {
		String sql = "SELECT COUNT(*) count FROM accountprovider WHERE user_id=? AND website_type=? AND provider_id=? ";
		return DBQueryRunner.getNumber(conn, sql, userId, websiteType, providerId).intValue() > 0;
	}

	public static int updateBalance(Connection conn, AccountProvider accountProvider) throws SQLException {
		String sql =
			"UPDATE accountProvider set provider_balance = ?, exposure = ?, provider_update_time = SYSTIMESTAMP "
				+ " WHERE website_type = ? AND user_id = ? AND provider_id = ? "
				+ " AND (provider_balance != ? OR exposure != ?) ";

		Object[] values = {
			accountProvider.getProviderBalance(),
			accountProvider.getExposure(),
			accountProvider.getWebsiteType(),
			accountProvider.getUserId(),
			accountProvider.getProviderId(),
			accountProvider.getProviderBalance(),
			accountProvider.getExposure(),
		};

		return DBQueryRunner.update(conn, sql, values);
	}

}
