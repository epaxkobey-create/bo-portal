package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.nv.commons.cache.PlayerAccountContactInfoLocalCache;
import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.ContactType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountContactInfo;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.model.database.DBQueryRunner;
import com.nv.commons.model.database.DataBeanProcessor;
import com.nv.commons.model.database.ResultSetProcessor;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.OracleUtils;
import org.apache.commons.lang3.StringUtils;

public class AccountContactInfoDAO {

	public static List<AccountContactInfo> findAll(Connection conn)
		throws SQLException {
		String sql = "SELECT * FROM AccountContactInfo ";
		return DBQueryRunner.getBeanList(conn, AccountContactInfo.class, sql);
	}

	public static List<AccountContactInfo> findAllByUpdateTime(Connection conn, Timestamp updateTime)
		throws SQLException {
		String sql = "SELECT * FROM AccountContactInfo WHERE update_time > ? ";
		return DBQueryRunner.getBeanList(conn, AccountContactInfo.class, sql, updateTime);
	}

	private static int add(Connection conn, AccountContactInfo contactInfo) throws SQLException {

		String sql = "INSERT INTO accountContactInfo(user_id, website_type, contact_type, content, verified_type, "
					 + "content_no, creator, create_time, updater, update_time) SELECT ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, ?, SYSTIMESTAMP "
					 + "FROM dual WHERE NOT EXISTS(SELECT 1 FROM accountContactInfo "
					 + "WHERE website_type = ? AND contact_type = ? AND content = ? and IS_DELETED = ? ) ";

		return DBQueryRunner.update(conn, sql,
			contactInfo.getUserId(),
			contactInfo.getWebsiteType(),
			contactInfo.getContactType(),
			contactInfo.getContent(),
			contactInfo.getVerifiedType(),

			contactInfo.getContentNo(),
			contactInfo.getCreator(),
			contactInfo.getUpdater(),

			contactInfo.getWebsiteType(),
			contactInfo.getContactType(),
			contactInfo.getContent(),
			BinaryStatusType.INACTIVE.unique()
		);
	}

//	/**
//	 * Update existing verify contact info
//	 */
//	public static int updateVerifyContactInfo(Connection conn, int webSiteTypeId, String userId,
//		ContactType contactType, String contactInfo) throws SQLException {
//
//		String sql = "UPDATE accountContactInfo SET content = ?, verified_type = ?, "
//					 + "content_no = ?, IS_DELETED = ?, UPDATER = ?, update_time = SYSTIMESTAMP "
//					 + "WHERE WEBSITE_TYPE = ? AND USER_ID = ? AND CONTACT_TYPE = ?";
//
//		return DBQueryRunner.update(conn, sql,
//			contactInfo, BinaryStatusType.ACTIVE.unique(), 1, BinaryStatusType.INACTIVE.unique(), userId,
//			webSiteTypeId, userId, contactType.unique());
//	}

	public static void checkDuplicateThenAdd(Connection conn, AccountContactInfo contact, String errorStr)
		throws Exception {

		int websiteType = contact.getWebsiteType();
		int contactType = contact.getContactType();
		String content = contact.getContent();

		// false if content was already present in the set
		if (PlayerAccountContactInfoLocalCache.getInstance()
			.addContactInfo(websiteType, contactType, content)) {
			// add db success
			// false if contact info already exist
			if (add(conn, contact) == 1) {
				// if all success, just return
				return;
			}
		}

		LogUtils.SYS.error("duplicate contact info, website:{}, user:{}, contact_type:{}, content:{}",
			websiteType, contact.getUserId(), contactType, content);

		throw new Deviation(errorStr);
	}


	public static int updateAsVerifiedByPlayer(Connection conn, int webSiteType, String userId,
		ContactType contactType, String contactInfo) throws SQLException {

		String sql = "UPDATE ACCOUNTCONTACTINFO SET verified_type = ?, "
					 + "UPDATER = ?, UPDATE_TIME = SYSTIMESTAMP "
					 + "WHERE user_id = ? AND website_type = ? "
					 + "AND contact_type = ? AND content = ? AND verified_type = ? AND is_deleted = ?";

		List<Object> params = new ArrayList<>();
		params.add(BinaryStatusType.ACTIVE.unique());
		params.add(userId);
		params.add(userId);
		params.add(webSiteType);
		params.add(contactType.unique());
		params.add(contactInfo);
		params.add(BinaryStatusType.INACTIVE.unique());
		params.add(BinaryStatusType.INACTIVE.unique());

		return DBQueryRunner.update(conn, sql, params);
	}


	public static boolean updateAsVerifiedByBO(Connection conn, int webSiteType, String userId,
		ContactType contactType, int contentNo, String updater) throws SQLException {

		String sql = "UPDATE accountcontactinfo SET verified_type = ?, updater = ?, update_time = SYSTIMESTAMP "
					 + "WHERE user_id = ? AND website_type = ? "
					 + "AND contact_type = ? AND content_no = ? AND verified_type = ? AND is_deleted = ?";

		int rowsUpdated = DBQueryRunner.update(conn, sql,
			BinaryStatusType.ACTIVE.unique(),
			updater,
			userId,
			webSiteType,
			contactType.unique(),
			contentNo,
			BinaryStatusType.INACTIVE.unique(),
			BinaryStatusType.INACTIVE.unique());

		return rowsUpdated > 0;
	}

	//	@CacheServer
	public static List<AccountContactInfo> findAccountContactDataByUserId(Connection conn, String userId,
		WebSiteType webSiteType) throws SQLException {
		return DBQueryRunner.getBeanList(conn, AccountContactInfo.class,
			"SELECT website_type, user_id, contact_type, verified_type, content, content_no, updater, update_time, allow_login FROM accountContactInfo "
			+ "WHERE user_id = ? AND website_type = ? AND is_deleted = ? ORDER BY contact_type ",
			userId, webSiteType.unique(), BinaryStatusType.INACTIVE.unique());
	}

	// 搜索 address， 返回list 看着 accountContactInfo 没有PK
//	public static List<AccountContactInfo> findAccountContactAddressByUserId(Connection conn, String userId,
//		WebSiteType webSiteType) throws SQLException {
//		return DBQueryRunner.getBeanList(conn, AccountContactInfo.class,
//			"SELECT website_type, user_id, contact_type, verified_type, content, content_no, updater, update_time, allow_login FROM accountContactInfo "
//			+ "WHERE user_id = ? AND website_type = ? AND is_deleted = ? AND contact_type = ? ORDER BY contact_type ",
//			userId, webSiteType.unique(), BinaryStatusType.INACTIVE.unique(), ContactType.Address.unique());
//
//	}


//	public static int delete(Connection conn, AccountContactInfo contactInfo) throws SQLException {
//		String sql = "UPDATE accountContactInfo SET "
//					 + "is_deleted = ?, content_no = (SELECT NVL(MIN(content_no), 0) "
//					 + "FROM accountcontactinfo WHERE website_type = ? AND user_id = ? "
//					 + "AND contact_type = ? AND is_deleted = ?) - 1, UPDATER = ?, "
//					 + "update_time = SYSTIMESTAMP "
//					 + "WHERE user_id = ? AND website_type = ? AND contact_type = ? AND content_no = ? AND is_deleted = ? ";
//
//		return DBQueryRunner
//			.update(conn, sql, BinaryStatusType.ACTIVE.unique(),
//				contactInfo.getWebsiteType(), contactInfo.getUserId(), contactInfo.getContactType(),
//				BinaryStatusType.ACTIVE.unique(), contactInfo.getUpdater(),
//				contactInfo.getUserId(), contactInfo.getWebsiteType(), contactInfo.getContactType(),
//				contactInfo.getContentNo(), BinaryStatusType.INACTIVE.unique());
//	}

//	public static void update(Connection conn, String userId, int webSiteType,
//		Map<AccountContactInfoKey, String> updateMap, Map<AccountContactInfoKey, String> insertMap,
//		Map<AccountContactInfoKey, String> deleteMap, String updater) {
//
//		update(conn, userId, webSiteType, updateMap, updater);
//
//		delete(conn, userId, webSiteType, deleteMap, updater);
//
//		insert(conn, userId, webSiteType, insertMap, updater);
//	}



//	public static void insert(Connection conn, String userId, int webSiteType,
//		Map<AccountContactInfoKey, String> insertMap, String updater) {
//		insertMap.forEach((accountContactInfoKey, insertStr) -> {
//			AccountContactInfo newContactInfo = new AccountContactInfo();
//			newContactInfo.setUserId(userId);
//			newContactInfo.setWebsiteType(webSiteType);
//			newContactInfo.setContactType(accountContactInfoKey.getContactType());
//			newContactInfo.setContent(insertStr);
//			newContactInfo.setContentNo(accountContactInfoKey.getContentNo());
//			newContactInfo.setCreator(updater);
//			newContactInfo.setUpdater(updater);
//
//			try {
//				checkDuplicateThenAdd(conn, newContactInfo, "duplicate contact info");
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		});
//	}

//	public static void delete(Connection conn, String userId, int webSiteType,
//		Map<AccountContactInfoKey, String> deleteMap, String updater) {
//		deleteMap.forEach((accountContactInfoKey, deleteStr) -> {
//			AccountContactInfo contactInfo = new AccountContactInfo();
//			contactInfo.setUserId(userId);
//			contactInfo.setWebsiteType(webSiteType);
//			contactInfo.setContactType(accountContactInfoKey.getContactType());
//			contactInfo.setContentNo(accountContactInfoKey.getContentNo());
//			contactInfo.setContent(deleteStr);
//			contactInfo.setUpdater(updater);
//			try {
//				delete(conn, contactInfo);
//			} catch (SQLException e) {
//				throw new RuntimeException(e);
//			}
//		});
//	}

//	public static void update(Connection conn, String userId, int webSiteType,
//		Map<AccountContactInfoKey, String> updateMap, String updater) {
//		updateMap.forEach((accountContactInfoKey, updateStr) -> {
//			PreparedStatement ps = null;
//			ResultSet rs = null;
//			try {
//				ps = conn.prepareStatement(
//					"SELECT content, verified_type, allow_login, updater FROM accountcontactinfo WHERE user_id = ? AND website_type = ? AND contact_type = ? AND content_no = ? AND is_deleted = ? FOR UPDATE",
//					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE,
//					ResultSet.HOLD_CURSORS_OVER_COMMIT);
//
//				DBQueryRunner.fillStatement(ps, ps.getParameterMetaData(), userId, webSiteType,
//					accountContactInfoKey.getContactType(), accountContactInfoKey.getContentNo(),
//					BinaryStatusType.INACTIVE.unique());
//
//				rs = ps.executeQuery();
//				if (rs.next()) {
//					rs.updateString("content", updateStr);
//					rs.updateInt("verified_type", BinaryStatusType.INACTIVE.unique());
//					rs.updateInt("allow_login", BinaryStatusType.INACTIVE.unique());
//					rs.updateString("updater", updater);
//					rs.updateRow();
//				}
//
//			} catch (SQLException e) {
//				throw new RuntimeException(e);
//			} finally {
//				DbUtils.closeAll(ps, rs);
//			}
//
//		});
//	}


	public static int checkIsAlreadyRegistered(Connection conn, String phoneNumber, String email, int websiteType)
		throws Exception {

		StringBuilder sql = new StringBuilder("SELECT CASE contact_type "
//											  + "WHEN " + ContactType.Phone.unique() + " THEN 1 "
											  + "WHEN " + ContactType.Email.unique() + " THEN 2 "
											  + "ELSE 5 END FROM accountContactInfo WHERE website_type = ? AND is_deleted = ? AND ( ");

		List<Object> params = new ArrayList<>();

		params.add(websiteType);
		params.add(BinaryStatusType.INACTIVE.unique());

		StringBuilder condition = new StringBuilder();

//		if (phoneNumber != null) {
//			condition.append("((content = ('0' || ?) OR content = ?) AND contact_type = ")
//				.append(ContactType.Phone.unique()).append(") OR ");
//			params.add(phoneNumber);
//			params.add(phoneNumber);
//		}

		if (email != null) {
			condition.append("(content = ? AND contact_type = ").append(ContactType.Email.unique()).append(") OR ");
			params.add(email);
		}

		if (condition.lastIndexOf(" OR ") > 0) {
			condition.delete(condition.lastIndexOf(" OR "), condition.length());
		}

		sql.append(condition).append(") ");

		Number number = DBQueryRunner.getNumber(conn, sql.toString(), params);

		return number == null ? 0 : number.intValue();
	}

	public static Map<String, List<AccountContactInfo>> getAccountContactByUserKeys(Connection conn,
		String[][] userKeys, Function<String, Account> getAccountFunc) throws SQLException {

		Map<String, List<AccountContactInfo>> accountContactMap = new HashMap<>();

		String sql = "SELECT * FROM accountContactInfo WHERE (user_id, website_type) IN ("
					 + OracleUtils.getGroupCondition(userKeys.length) + ") ORDER BY website_type, user_id, contact_type";

		WebSiteType webSiteType;
		String userKey;

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			ps = conn.prepareStatement(sql);
			ps.setObject(1, OracleUtils.getOracleARRAY(conn, "Q_COMBINE_USER_ID_ARRAY", userKeys));

			rs = ps.executeQuery();

			while (rs.next()) {
				webSiteType = WebSiteType.getInstance(rs.getInt("website_type"));

				userKey = AccountUtils.getUserKey(webSiteType, rs.getString("user_id"));

				Account userInCache = getAccountFunc.apply(userKey);

				if (userInCache == null) {
					continue;
				}

				List<AccountContactInfo> accountContactList = accountContactMap.computeIfAbsent(userKey,
					k -> new ArrayList<>());

				if (rs.getBoolean("is_deleted")) {
					continue;
				}

				AccountContactInfo contactInfo = new AccountContactInfo();
				contactInfo.setUserId(rs.getString("user_id"));
				contactInfo.setWebsiteType(rs.getInt("website_type"));
				contactInfo.setContactType(rs.getInt("contact_type"));
				contactInfo.setContent(rs.getString("content"));
				contactInfo.setContentNo(rs.getInt("content_no"));
				contactInfo.setVerifiedType(rs.getInt("verified_type"));
				contactInfo.setCreator(rs.getString("creator"));
				contactInfo.setCreateTime(rs.getTimestamp("create_time"));
				contactInfo.setUpdater(rs.getString("updater"));
				contactInfo.setUpdateTime(rs.getTimestamp("update_time"));
				accountContactList.add(contactInfo);
			}

		} finally {
			DbUtils.close(rs);
			DbUtils.close(ps);
		}
		return accountContactMap;
	}

	public static Map<String, Map<ContactType, List<AccountContactInfo>>> getContactInfoMapByUserIds(Connection conn,
		WebSiteType webSiteType, Set<String> userIds, Set<Integer> contactTypes) throws SQLException {

		Map<String, Map<ContactType, List<AccountContactInfo>>> resultMap = new HashMap<>();

		if (userIds == null || userIds.isEmpty()) {
			return resultMap;
		}

		StringBuilder sqlBuilder = new StringBuilder("SELECT user_id, content, contact_type, content_no, verified_type "
													 + "FROM accountcontactinfo WHERE website_type = ? AND is_deleted = ? ");

		ArrayList<Object> params = new ArrayList<>();
		params.add(webSiteType.unique());
		params.add(BinaryStatusType.INACTIVE.unique());

		if ("junit".equalsIgnoreCase(
			System.getProperty(com.nv.commons.constants.SystemConstants.RUNTIME_ENV))) {
			sqlBuilder.append("AND user_id IN (")
				.append(StringUtils.repeat("?", ",", userIds.size()))
				.append(" ) ");
			params.addAll(userIds);
		} else {
			sqlBuilder.append("AND user_id IN (")
				.append(OracleUtils.getGroupCondition(userIds.size()))
				.append(" ) ");
			params.add(OracleUtils.getOracleARRAY(conn, "STRING_ARRAY", userIds.toArray()));
		}

		if (!contactTypes.isEmpty()) {
			sqlBuilder.append("AND contact_type IN ( ")
				.append(StringUtils.repeat("?", ",", contactTypes.size()))
				.append(" ) ");
			params.addAll(contactTypes);
		}

		DataBeanProcessor<AccountContactInfo> processor = (resultSet, bean) -> {

			Map<ContactType, List<AccountContactInfo>> contactMap = resultMap.computeIfAbsent(
				resultSet.getString("user_id"), map -> new LinkedHashMap<>());

			List<AccountContactInfo> accountContactInfoList = contactMap.computeIfAbsent(
				ContactType.getInstanceOf(resultSet.getInt("contact_type")), list -> new ArrayList<>());

			accountContactInfoList.add(bean);

		};

		DBQueryRunner.processBeanResult(conn, processor, AccountContactInfo.class, sqlBuilder.toString(), params);

		return resultMap;
	}

	public static int refreshUpdateTime(Connection conn, Object[][] accountContactInfos) throws SQLException {

		String sql = " UPDATE accountContactInfo SET update_time = SYSTIMESTAMP "
					 + " WHERE (website_type, contact_type, content_no, user_id) in ( "
					 + OracleUtils.getGroupCondition(accountContactInfos.length, "ACCOUNT_CONTACT_INFO_KEY_SET")
					 + " ) ";

		return DBQueryRunner.update(conn, sql,
			OracleUtils.getOracleARRAY(conn, "ACCOUNT_CONTACT_INFO_KEY_SET", accountContactInfos));
	}


	public static List<String> getUserIdsByWildCardSearchEmail(Connection conn, WebSiteType webSiteType,
		ContactType contactType,
		String content)
		throws SQLException {

		ArrayList<Object> params = new ArrayList<>();
		ArrayList<String> results = new ArrayList<>();

		String sql =
			"SELECT user_id FROM accountcontactinfo "
			+ "WHERE website_type = ? AND contact_type = ? AND is_deleted = ? AND content like ?";

		params.add(webSiteType.unique());
		params.add(contactType.unique());
		params.add(BinaryStatusType.INACTIVE.unique());
		params.add("%" + content + "%");

		ResultSetProcessor processor = (index, resultSet) -> results.add(resultSet.getString("user_id"));
		DBQueryRunner.processResultSet(conn, processor, sql, params);
		return results;
	}

}
