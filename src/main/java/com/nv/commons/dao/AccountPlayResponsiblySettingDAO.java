package com.nv.commons.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.nv.commons.constants.AccountPlayResponsiblyType;
import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.dto.AccountPlayResponsiblySetting;
import com.nv.commons.model.database.DBQueryRunner;

/**
 * TODO: re-name db table to AccountPlayResponsiblySetting
 */
public class AccountPlayResponsiblySettingDAO {


	public static List<AccountPlayResponsiblySetting> findAll(Connection conn)
		throws SQLException {

		return DBQueryRunner.getBeanList(conn, AccountPlayResponsiblySetting.class, "SELECT * FROM accountPlayResponsibly");
	}

	public static List<AccountPlayResponsiblySetting> getEntities(Connection conn, Timestamp updateTime) throws SQLException {
		String sql = " SELECT * FROM accountPlayResponsibly WHERE update_time > ?";
		return DBQueryRunner.getBeanList(conn, AccountPlayResponsiblySetting.class, sql, updateTime);
	}

	public static List<AccountPlayResponsiblySetting> findAll(Connection conn, String userId, int webSiteType, int type)
		throws SQLException {

		return DBQueryRunner.getBeanList(conn, AccountPlayResponsiblySetting.class,
			"""
				SELECT * FROM accountPlayResponsibly
				WHERE user_id = ? AND website_type = ? AND type = ?
				""",
			userId, webSiteType, type
		);
	}

	public static AccountPlayResponsiblySetting findOneByUserIdAndTypeAndPeriodType (Connection conn, String userId, int webSiteType, int type, int periodType)
		throws SQLException {
		return DBQueryRunner.getBean(conn, AccountPlayResponsiblySetting.class,
			"""
				SELECT * FROM accountPlayResponsibly
				WHERE user_id = ? AND website_type = ? AND type = ? AND period_type = ?
				""",
			userId, webSiteType, type, periodType
		);
	}

	public static int insert(Connection conn, AccountPlayResponsiblySetting playResponsibly)
		throws SQLException {

		return DBQueryRunner.update(conn,
				"""
				INSERT INTO accountPlayResponsibly (user_id, website_type, type,
				period_type, current_value, new_value, status, effective_time, effective_end_time,
				creator, create_time, updater, update_time)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, ?, SYSTIMESTAMP)
				""",
			playResponsibly.getUserId(), playResponsibly.getWebsiteType(), playResponsibly.getType(),
			playResponsibly.getPeriodType(), playResponsibly.getCurrentValue(), playResponsibly.getNewValue(),
			playResponsibly.getStatus(), playResponsibly.getEffectiveTime(), playResponsibly.getEffectiveEndTime(),
			playResponsibly.getCreator(), playResponsibly.getUpdater()
		);
	}

	public static int update(Connection conn, AccountPlayResponsiblySetting playResponsibly)
		throws SQLException {

		return DBQueryRunner.update(conn,
			"""
				UPDATE accountPlayResponsibly SET current_value = ?, new_value = ?,
				status = ?, effective_time = ?, effective_end_time = ?, updater = ?, update_time = SYSTIMESTAMP
				WHERE user_id = ? AND website_type = ? AND type = ? AND period_type = ?
				""",
			playResponsibly.getCurrentValue(), playResponsibly.getNewValue(),
			playResponsibly.getStatus(), playResponsibly.getEffectiveTime(), playResponsibly.getEffectiveEndTime(),
			playResponsibly.getUpdater(),
			playResponsibly.getUserId(), playResponsibly.getWebsiteType(),
			playResponsibly.getType(), playResponsibly.getPeriodType()
		);
	}

	public static boolean isExist(Connection conn, String userId, int webSiteType, int type)
		throws Exception {
		return DBQueryRunner.getNumberWithDefault(conn,
			"""
				SELECT COUNT(*) FROM accountPlayResponsibly
				WHERE user_id = ? AND website_type = ? AND type = ?
				""",
			BigDecimal.ZERO, userId, webSiteType, type
		).intValue() > 0;
	}

	public static int resetAnnualReminderToFalse(Connection conn)
		throws SQLException {

		return DBQueryRunner.update(conn,
				"""
				UPDATE accountPlayResponsibly SET current_value = ?, new_value = ?,
				updater = ?, update_time = SYSTIMESTAMP
				WHERE type = ? AND EXTRACT(YEAR FROM update_time) < EXTRACT(YEAR FROM SYSTIMESTAMP)
				""",
			BinaryStatusType.INACTIVE.unique(), BinaryStatusType.INACTIVE.unique(), "SYS",
			AccountPlayResponsiblyType.ANNUAL_REMINDER.unique()
		);
	}

	public static int extendAccountReviewReminderEffectiveTime(Connection conn, String userId, int webSiteType,
		String updater)
		throws SQLException {

		return DBQueryRunner.update(conn,
				"""
				UPDATE accountPlayResponsibly SET effective_time = ADD_MONTHS(SYSTIMESTAMP, TO_NUMBER(current_value)),
				updater = ?, update_time = SYSTIMESTAMP
				WHERE user_id = ? AND website_type = ? AND type = ?
				""",
			updater, userId, webSiteType, AccountPlayResponsiblyType.ACCOUNT_REVIEW_REMINDER.unique()
		);
	}
}
