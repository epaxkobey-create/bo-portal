package com.nv.commons.dao;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.bo.AccountDocumentBO;
import com.nv.commons.constants.AccountSortType;
import com.nv.commons.constants.AccountStatusType;
import com.nv.commons.constants.ContactType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.DBOrderType;
import com.nv.commons.constants.DBQueryType;
import com.nv.commons.constants.DocumentStatusType;
import com.nv.commons.constants.KycDocumentStatusType;
import com.nv.commons.constants.MoneyTransactionStatusType;
import com.nv.commons.constants.MoneyTransactionType;
import com.nv.commons.constants.PlatformType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.UpdatedAttributeType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountAttach;
import com.nv.commons.dto.AccountRequest;
import com.nv.commons.dto.PageResult;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.message.LangMessage;
import com.nv.commons.model.PageInfo;
import com.nv.commons.model.database.DBQueryRunner;
import com.nv.commons.model.database.JsonValueProcessor;
import com.nv.commons.system.SystemInfo;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DateTimeBuilder;
import com.nv.commons.utils.DateUtils;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.FormatUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.NamedQuery;
import com.nv.commons.utils.OracleUtils;
import com.nv.module.backendapi.cache.PlayerLocalCache;
import com.nv.module.swserver.neutec.SeamlessWalletApiService;
import org.apache.commons.lang3.StringUtils;

public class AccountDAO {

	public static List<Account> findAll(Connection conn)
		throws SQLException {
		String sql = "SELECT * FROM Account ";
		return DBQueryRunner.getBeanList(conn, Account.class, sql);
	}

	public static List<Account> findAllByUpdateTime(Connection conn, Timestamp updateTime)
		throws SQLException {
		String sql = "SELECT * FROM Account WHERE update_time > ? ";
		return DBQueryRunner.getBeanList(conn, Account.class, sql, updateTime);
	}

	public static long getAccountID(Connection conn) throws SQLException {
		String sql = "SELECT ACCOUNT_ID_SEQ.nextval FROM DUAL ";
		return DBQueryRunner.getNumber(conn, sql).longValue();
	}

	/*
	 * create new account
	 */
	public static void save(final Connection conn, Account account, long accountId) throws SQLException {

		final String sql =
			"INSERT INTO Account(ID, WEBSITE_TYPE,USER_ID,USER_NAME,LEGAL_FIRST_NAME,LEGAL_LAST_NAME,PASSWORD,AFFILIATE,AFFILIATE_ID,USER_CHANNEL_TYPE,CURRENCY_TYPE_ID,BALANCE,COUNTRY_TYPE,BIRTHDAY,OCCUPATION,"
				+ "ALLOW_GAME_TYPE,VIP_LEVEL,AUTO_UPGRADE_VIP,STATUS,min_force_serve,min_deposit_amount,"
				+ "LOGIN_FAIL,SIGN_UP_IP,SIGN_UP_CITY,SIGN_UP_STATE,SIGN_UP_COUNTRY,SIGN_UP_TIME,AFFILIATE_LINK_SEQ,GENDER,MARITAL,VIEWED_PLAY_RESPONSIBLY) VALUES "
				+ "(:ID,:WEBSITE_TYPE,:USER_ID,:USER_NAME,:LEGAL_FIRST_NAME,:LEGAL_LAST_NAME,:PASSWORD,:AFFILIATE,:AFFILIATE_ID,:USER_CHANNEL_TYPE,:CURRENCY_TYPE_ID,:BALANCE,:COUNTRY_TYPE,:BIRTHDAY,:OCCUPATION,"
				+ ":ALLOW_GAME_TYPE,:VIP_LEVEL,:AUTO_UPGRADE_VIP,:STATUS,:min_force_serve,:min_deposit_amount,"
				+ ":LOGIN_FAIL,:SIGN_UP_IP,:SIGN_UP_CITY,:SIGN_UP_STATE,:SIGN_UP_COUNTRY,SYSTIMESTAMP,:AFFILIATE_LINK_SEQ,:GENDER,:MARITAL,"
				+ ":VIEWED_PLAY_RESPONSIBLY)";

		NamedQuery namedQuery = new NamedQuery();
		namedQuery.parseSql(sql);
		namedQuery.setObject("ID", accountId);
		namedQuery.setObject("WEBSITE_TYPE", account.getWebsiteType());
		namedQuery.setObject("USER_ID", account.getUserId());
		namedQuery.setObject("USER_NAME", account.getUserName());
		namedQuery.setObject("LEGAL_FIRST_NAME", account.getLegalFirstName());
		namedQuery.setObject("LEGAL_LAST_NAME", account.getLegalLastName());
		namedQuery.setObject("PASSWORD", account.getPassword());
		namedQuery.setObject("AFFILIATE", account.getAffiliate());
		namedQuery.setObject("AFFILIATE_ID", account.getAffiliateId());
		namedQuery.setObject("USER_CHANNEL_TYPE", account.getUserChannelType());
		//--
		namedQuery.setObject("CURRENCY_TYPE_ID", account.getCurrencyTypeId());
		namedQuery.setObject("BALANCE", 0);
		namedQuery.setObject("COUNTRY_TYPE", account.getCountryType());
		namedQuery.setObject("BIRTHDAY", account.getBirthday());
		namedQuery.setObject("OCCUPATION", account.getOccupation());
		//--
		namedQuery.setObject("ALLOW_GAME_TYPE", account.getAllowGameType());
		namedQuery.setObject("VIP_LEVEL", account.getVipLevel());
		// 會員等級是否讓系統自動更新, 0=Manual, 1=Automatic
		namedQuery.setObject("AUTO_UPGRADE_VIP", account.getAutoUpgradeVip());
		namedQuery.setObject("STATUS", account.getStatus());

		namedQuery.setObject("min_force_serve", account.getMinForceServe());
		namedQuery.setObject("min_deposit_amount", account.getMinDepositAmount());

		//--
		namedQuery.setObject("LOGIN_FAIL", account.getLoginFail());
		namedQuery.setObject("SIGN_UP_IP", account.getSignUpIp());

		namedQuery.setObject("SIGN_UP_CITY", account.getSignUpCity());
		namedQuery.setObject("SIGN_UP_STATE", account.getSignUpState());
		namedQuery.setObject("SIGN_UP_COUNTRY", account.getSignUpCountry());
		namedQuery.setObject("AFFILIATE_LINK_SEQ", account.getAffiliateLinkSeq());

		namedQuery.setObject("GENDER", account.getGender());
		namedQuery.setObject("MARITAL", account.getMarital());

		namedQuery.setObject("VIEWED_PLAY_RESPONSIBLY", account.getViewedPlayResponsibly());

		DBQueryRunner.update(conn, namedQuery.getNativeSQL(), namedQuery.getParameterArray());
	}

	/**
	 * 更新 account 登入失敗次數,以及 lock
	 */
	public static int updateLoginFailureAndLock(final Connection conn, String userID, int webSiteType)
		throws SQLException {

		String sql = "UPDATE ACCOUNT SET UPDATE_TIME = SYSTIMESTAMP, LOGIN_FAIL = LOGIN_FAIL + 1 , STATUS = ?  WHERE USER_ID = ? AND WEBSITE_TYPE = ? ";

		return DBQueryRunner.update(conn, sql, AccountStatusType.LOCKED.unique(), userID, webSiteType);
	}

	/**
	 * 更新 account 登入失敗次數
	 */
	public static int updateLoginFailure(final Connection conn, String userID, int webSiteType)
		throws SQLException {

		String sql = "UPDATE ACCOUNT SET UPDATE_TIME = SYSTIMESTAMP, LOGIN_FAIL = LOGIN_FAIL + 1 WHERE USER_ID = ? AND WEBSITE_TYPE = ? ";

		return DBQueryRunner.update(conn, sql, userID, webSiteType);
	}

	public static int updateStatus(Connection conn, String userID, int webSiteType, int status, boolean resetLoginFail)
		throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ACCOUNT SET UPDATE_TIME = SYSTIMESTAMP, STATUS = ? ");

		if (resetLoginFail) {
			sql.append(", LOGIN_FAIL = 0 ");
		}

		sql.append("where USER_ID = ? and WEBSITE_TYPE = ? ");

		return DBQueryRunner.update(conn, sql.toString(), status, userID, webSiteType);
	}

	/**
	 * 針對userID做Update append
	 *
	 */
	public static int updateUpdatedAttribute(Connection conn, String userID, WebSiteType webSiteType,
		UpdatedAttributeType... updatedAttributeType) throws SQLException {
		// SERVER_ID is not null 代表目前有登入
		String sql = "UPDATE Account SET UPDATE_TIME = SYSTIMESTAMP, UPDATED_ATTRIBUTE = addBit(UPDATED_ATTRIBUTE, ? )  WHERE SERVER_ID is not null AND USER_ID = ? and WEBSITE_TYPE = ? AND platform_type = ?";
		int attributeUpdateValue = 0;
		for (UpdatedAttributeType attributeType : updatedAttributeType) {
			attributeUpdateValue = attributeUpdateValue | attributeType.unique();
		}
		Object[] values = new Object[] {attributeUpdateValue, userID, webSiteType.unique(), PlatformType.WEB.unique()};
		// MEMO: for h2 not support addBit function as oracle
		if ("junit".equalsIgnoreCase(
			System.getProperty(SystemConstants.RUNTIME_ENV))) {
			sql = "UPDATE Account SET UPDATE_TIME = SYSTIMESTAMP, UPDATED_ATTRIBUTE = addBit(UPDATED_ATTRIBUTE, "
				+ attributeUpdateValue
				+ " )  WHERE SERVER_ID is not null AND USER_ID = ? and WEBSITE_TYPE = ? AND platform_type = ?";
			values = new Object[] {userID, webSiteType.unique(), PlatformType.WEB.unique()};
		}

		return DBQueryRunner.update(conn, sql, values);
	}

	/*
	 * 不要統一都 update 成 0
	 * @updatedAttributes 代表是上次從 DB 取出得到的 updatedAttributes
	 * 也可能 DB 已經跟上次取的不一樣
	 */
	public static int resetUpdateAttribute(Connection conn, String[] userIdWebSiteId, int singleUpdatedAttribute)
		throws SQLException {

		String sql =
			"UPDATE Account set UPDATED_ATTRIBUTE = UPDATED_ATTRIBUTE - ? where USER_ID = ? and WEBSITE_TYPE = ? "
				+ "and UPDATED_ATTRIBUTE > 0 and (UPDATED_ATTRIBUTE - ?) >= 0";

		// userIdWebSiteId : [ userID, websiteTypeID ]
		return DBQueryRunner
			.update(conn, sql, singleUpdatedAttribute, userIdWebSiteId[0], userIdWebSiteId[1], singleUpdatedAttribute);
	}

	public static String getMemberJsonByMultiCondition(Connection conn, AccountRequest accountRequest)
		throws Exception {

		ArrayList<Object> params = new ArrayList<>();

		StringBuilder sharedSql = new StringBuilder(
			"SELECT a.website_type, a.user_id, a.friend_referrer, a.affiliate, a.user_channel_type, a.create_time, a.user_name, "
				+
				"a.vip_level, a.summary_convertion_point, a.status, a.login_ip, a.balance, a.sign_up_ip, a.sign_up_city, "
				+
				"a.sign_up_state, a.sign_up_time, a.login_time as login_time, a.last_deposit_time, a.last_bet_time, a.currency_type_id, a.country_type, "
				+
				"a.last_withdrawal_time, " +

				" CASE"
				+ "        WHEN k.first_name IS NULL AND k.last_name IS NULL THEN ''"
				+ "        WHEN k.first_name IS NULL THEN k.last_name"
				+ "        WHEN k.last_name IS NULL THEN k.first_name"
				+ "        ELSE k.first_name || ' ' || k.last_name"
				+ "    END AS full_name, " +
				"k.first_name, k.last_name, k.dob ,  k.document_no,  k.city, k.country, k.postal_code, " +

				// MEMO: DocumentStatusType 5 = 0, to order same level with 0
				"CASE " +
				"WHEN NVL(ad.status, 0) = " + DocumentStatusType.CREATED.unique() + " " +
				"THEN 0 " +
				"ELSE NVL(ad.status, 0) " +
				"END as verification_status, "
		);

		String sortCondition = accountRequest.getSortCondition();
		// 僅有使用總餘額排序時在sql直接加總後進行排序
		//		if (AccountSortType.TOTAL_BALANCE.getSortCondition().equals(sortCondition)) {
		//			sharedSql.append("a.balance + "
		//				+ "NVL("
		//				+ "(SELECT SUM(accountprovider.provider_balance) FROM accountprovider "
		//				+ "WHERE a.website_type = accountprovider.website_type "
		//				+ "AND a.user_id = accountprovider.user_id AND bonus_turnover_id = -1 ),0)"
		//				+ " + "
		//				+ "NVL("
		//				+ "(SELECT SUM(balance) FROM accountbonusturnover "
		//				+ "WHERE a.website_type = accountbonusturnover.website_type "
		//				+ "AND a.user_id = accountbonusturnover.user_id AND status IN (1,-1,2) "
		//				+ "AND bonus_wallet = 1 ),0) as total_balance ");
		//		} else {
		sharedSql.append("a.balance as total_balance ");
		//		}

		sharedSql.append(
			"FROM account a " +
				"LEFT JOIN accountdocument ad ON a.user_id = ad.user_id AND ad.website_type = a.website_type " +
				"LEFT JOIN kycpersonalinfo k ON ad.id = k.account_document_id " +
				"WHERE a.website_type = ? "
		);
		WebSiteType webSiteType = accountRequest.getWebSiteType();
		params.add(webSiteType.unique());

		List<Integer> currencyList = accountRequest.getCurrencyList();
		if (-1 != accountRequest.getCurrencyType()) {
			sharedSql.append("AND a.currency_type_id = ? ");
			params.add(accountRequest.getCurrencyType());
		} else if (currencyList.size() == 1) {
			sharedSql.append("AND a.currency_type_id = ? ");
			params.add(currencyList.getFirst());
		} else {
			sharedSql.append("AND a.currency_type_id IN (").append(StringUtils.repeat("?", ",", currencyList.size()))
				.append(") ");
			params.addAll(currencyList);
		}

		String usedIds = accountRequest.getUserIds();

		if (null != usedIds) {
			String[] userIds = usedIds.split(",");
			if ("junit".equalsIgnoreCase(System.getProperty(SystemConstants.RUNTIME_ENV))) {
				sharedSql.append("AND a.user_id in (").append(StringUtils.repeat("?", ",", userIds.length))
					.append(") ");
				for (String id : userIds) {
					params.add(id);
				}
			} else {
				sharedSql.append("AND a.user_id in (").append(OracleUtils.getGroupCondition(userIds.length))
					.append(")");
				params.add(OracleUtils.getOracleARRAY(conn, "STRING_ARRAY", userIds));
			}
		}
		if (null != accountRequest.getUserName()) {
			sharedSql.append("AND LOWER(TRIM(NVL(k.first_name, '') || ' ' || NVL(k.last_name, ''))) LIKE ? ");
			params.add("%" + accountRequest.getUserName().toLowerCase() + "%");
		}
		if (null != accountRequest.getLastDepositTime()) {
			sharedSql.append("AND a.last_deposit_time  >= ? ");
			params.add(accountRequest.getLastDepositTime());
		}
		if (null != accountRequest.getLastBetTime()) {
			sharedSql.append("AND a.last_bet_time >= ? ");
			params.add(accountRequest.getLastBetTime());
		}
		if (null != accountRequest.getLoginTime()) {
			sharedSql.append("AND a.login_time <= ? ");
			params.add(accountRequest.getLoginTime());
		}
		if (null != accountRequest.getLastLoginSince()) {
			sharedSql.append("AND a.login_time >= ? ");
			params.add(accountRequest.getLastLoginSince());
		}
		if (null != accountRequest.getLoginIp()) {
			sharedSql.append("AND a.login_ip = ? ");
			params.add(accountRequest.getLoginIp());
		}
		if (-1 != accountRequest.getVipLevel()) {
			sharedSql.append("AND a.vip_level = ? ");
			params.add(accountRequest.getVipLevel());
		}
		if (null != accountRequest.getLastRegister()) {
			sharedSql.append("AND a.sign_up_time >= ? ");
			params.add(new Timestamp(accountRequest.getLastRegister().getTime()));
		}

		if (null != accountRequest.getLastWithdrawTime()) {
			sharedSql.append("AND a.last_withdrawal_time >= ? ");
			params.add(accountRequest.getLastWithdrawTime());
		}

		int userChannelTypeId = accountRequest.getUserChannelTypeId();

		if (userChannelTypeId != -1) {
			sharedSql.append("AND a.user_channel_type = ? ");
			params.add(userChannelTypeId);
		}

		int status = accountRequest.getStatus();
		if (status > -1) {
			sharedSql.append("AND a.status = ? ");
			params.add(status);
		}

		int verificationStatus = accountRequest.getVerificationStatus();
		if (verificationStatus >= KycDocumentStatusType.FAILED.unique()) {
			// MEMO: DocumentStatusType 5 = 0, to filter as Unverified
			if (verificationStatus == KycDocumentStatusType.UNVERIFIED.unique()) {
				sharedSql.append("AND (NVL(ad.status, 0) = ? OR NVL(ad.status, 0) = ?)");
				params.add(verificationStatus);
				params.add(DocumentStatusType.CREATED.unique());
			} else {
				sharedSql.append("AND NVL(ad.status, 0) = ?");
				params.add(verificationStatus);
			}
		}

		if ("birthday".equalsIgnoreCase(sortCondition)) {
			sortCondition = "k.dob";
		}

		if ("status".equals(sortCondition)) {
			sortCondition = " CASE a.status"
				+ " WHEN 1 THEN 'Active'"
				+ " WHEN 3 THEN 'Locked'"
				+ " WHEN 0 THEN 'Inactive'"
				+ " WHEN 2 THEN 'Suspend'"
				+ " END ";
		}

		PageInfo pageInfo = accountRequest.getPageInfo();
		String countSql = " SELECT COUNT(*) FROM ( " + sharedSql + ")";
		int totalCount = DBQueryRunner.getNumber(conn, countSql, params).intValue();
		pageInfo.setTotalCount(totalCount);

		// 因為電話與Email、VIP Point 不再此處由sql取得，無法針對欄位進行排序，給予預設值避免sql執行錯誤
		if (AccountSortType.PHONE_NUMBER.getSortCondition().equals(sortCondition)
			|| AccountSortType.EMAIL.getSortCondition().equals(sortCondition)
		) {
			sharedSql.append(" ORDER BY ").append(AccountSortType.CREATE_TIME.getSortCondition())
				.append(accountRequest.getOrderType().getSqlString());
		} else if (AccountSortType.FULL_NAME.getSortCondition().equals(sortCondition)) {
			/*
			 * 因為現在 bo 不能分別輸入 first name 跟 last name，
			 * 所以 last name 會是 null。但是在 sort 做這樣的 case when
			 * 在資料量很多的時候可能會影響效能
			 * */

			sortCondition =
				" UPPER(CASE"
					+ "        WHEN k.first_name IS NULL AND k.last_name IS NULL THEN ''"
					+ "        WHEN k.first_name IS NULL THEN k.last_name"
					+ "        WHEN k.last_name IS NULL THEN k.first_name"
					+ "        ELSE k.first_name || ' ' || k.last_name"
					+ "    END) " + accountRequest.getOrderType().getSqlString();

			sharedSql.append(" ORDER BY ").append(sortCondition).append(" NULLS LAST");

		} else if (AccountSortType.VERIFICATION_STATUS.getSortCondition().equals(sortCondition)) {
			sortCondition = " (CASE"
				+ " WHEN verification_status IN (0, -2, 5) THEN 'Unverified'"  // Unverified
				+ " WHEN verification_status IN (1, 3) THEN 'Verifying'"      // Verifying
				+ " WHEN verification_status = 2 THEN 'Verified'"             // Verified
				+ " WHEN verification_status = -1 THEN 'Failed'"            // Failed
				+ " ELSE 'Unverified'"                                          // Default to Unverified
				+ " END)"
				+ accountRequest.getOrderType().getSqlString();
			sharedSql.append(" ORDER BY ").append(sortCondition).append(" NULLS LAST");

		} else {
			sharedSql.append(" ORDER BY ").append(sortCondition).append(accountRequest.getOrderType().getSqlString())
				.append(" NULLS LAST");
		}

		params.add(pageInfo.getLastRowNumber());
		params.add(pageInfo.getFirstRowNumber());

		String pageSQL = OracleUtils.getCalculatedPageSQL(sharedSql.toString());

		JsonValueProcessor memberProcessor = generateMemberProcessor(pageInfo);

		return DBQueryRunner.processJsonArrayValue(conn, memberProcessor, pageSQL, params);
	}

	// TODO: it is bad to process json in dao
	private static JsonValueProcessor generateMemberProcessor(PageInfo pageInfo) {

		AtomicInteger pageCount = new AtomicInteger();

		JsonValueProcessor processor = (index, rs, jGenerator) -> {

			pageCount.getAndIncrement();
			String userId = rs.getString("user_id");
			jGenerator.writeStartObject();

			jGenerator.writeStringField("channel", "direct");
			jGenerator.writeNullField("channelName");
			jGenerator.writeNumberField("createTime", rs.getTimestamp("create_time").getTime());
			jGenerator.writeNumberField("webSiteType", rs.getInt("website_type"));
			jGenerator.writeStringField("userId", userId);

			String lastName = StringUtils.trimToEmpty(rs.getString("last_name"));
			String firstName = StringUtils.trimToEmpty(rs.getString("first_name"));

			if (StringUtils.isEmpty(lastName) && StringUtils.isEmpty(firstName)) {
				jGenerator.writeNullField("fullName");
			} else {
				String fullName = StringUtils.trim(firstName + " " + lastName);
				jGenerator.writeStringField("fullName", fullName);
			}

			jGenerator.writeNumberField("vipPoint", rs.getDouble("summary_convertion_point"));
			jGenerator.writeStringField("affiliateUrl", rs.getString("affiliate"));

			jGenerator.writeNumberField("status", rs.getInt("status"));

			// totalBalance should get from single wallet
			//			jGenerator.writeNumberField("totalBalance", rs.getDouble("total_balance"));

			jGenerator.writeStringField("loginIp", rs.getString("login_ip"));
			jGenerator.writeStringField("signUpIp", rs.getString("sign_up_ip"));
			jGenerator.writeStringField("signUpCity", rs.getString("sign_up_city"));
			jGenerator.writeStringField("signUpState", rs.getString("sign_up_state"));
			Timestamp sign_up_time_tmp = rs.getTimestamp("sign_up_time");
			if (null != sign_up_time_tmp) {
				jGenerator.writeStringField("sign_up_time",
					DateUtils.toString(DateTimeBuilder.localDateTime(sign_up_time_tmp).toDate(),
						FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss));
			} else {
				jGenerator.writeNullField("sign_up_time");
			}
			Timestamp tmp = rs.getTimestamp("login_time");
			if (null != tmp) {
				jGenerator.writeStringField("loginTime", DateUtils.toString(
					DateTimeBuilder.localDateTime(tmp).toDate(), FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss));
			} else {
				jGenerator.writeNullField("loginTime");
			}
			tmp = rs.getTimestamp("last_deposit_time");
			if (null != tmp) {
				jGenerator.writeStringField("lastDepositTime", DateUtils.toString(
					DateTimeBuilder.localDateTime(tmp).toDate(), FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss));
			} else {
				jGenerator.writeNullField("lastDepositTime");
			}
			tmp = rs.getTimestamp("last_bet_time");
			if (null != tmp) {
				jGenerator.writeStringField("lastBetTime", DateUtils.toString(
					DateTimeBuilder.localDateTime(tmp).toDate(), FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy_HHmmss));
			} else {
				jGenerator.writeNullField("lastBetTime");
			}
			jGenerator
				.writeStringField("currencyName", CurrencyType.getInstance(rs.getInt("currency_type_id")).name());
			jGenerator.writeNumberField("currency", rs.getInt("currency_type_id"));
			jGenerator.writeNumberField("countryType", rs.getInt("country_type"));

			// kyc account document status
			//			jGenerator.writeNumberField("verificationStatus", rs.getInt("verification"));

			jGenerator.writeNumberField("kycDocumentStatus",
				AccountDocumentBO.getKycDocumentStatus(rs.getInt("verification_status")).unique());
			tmp = rs.getTimestamp("dob");
			if (null != tmp) {
				jGenerator.writeStringField("birthday",
					DateUtils.toString(tmp, FormatUtils.DATE_PATTERN_SLASH_ddMMyyyy));
			} else {
				jGenerator.writeNullField("birthday");
			}

			jGenerator.writeEndObject();
		};
		if (pageInfo.getPageSize() <= 0) {
			pageInfo.setTotalCount(pageCount.get());
		}
		return processor;
	}

	private static String prepareSelectSqlForRegisterUser() {
		return
			"SELECT a.user_id, a.website_type, a.user_name, a.sign_up_country, sign_up_state, sign_up_city, sign_up_time, affiliate, currency_type_id, "
				+ "user_channel_type, friend_referrer, "
				+ "(SELECT user_id FROM affiliate WHERE a.affiliate_id = affiliate.id) affiliate_name, "
				+ "(SELECT NVL(SUM(deposit_amount), 0) AS deposit_amount FROM accountStats s WHERE a.website_type = s.website_type AND a.user_id = s.user_id ) deposit_amount ";
	}

	private static StringBuilder prepareShareSqlForRegisterUser() {
		return new StringBuilder("FROM account a WHERE a.website_type = ? AND a.currency_type_id = ? ");
	}

	public static AccountAttach getProfileOverview(Connection conn, String userId, WebSiteType webSiteType,
		int accountCurrency) throws SQLException {

		String sql =
			"SELECT website_type, user_id, legal_first_name, legal_last_name, user_name, birthday, vip_level, summary_convertion_point, affiliate, status, "
				+ "create_time, sign_up_time, sign_up_ip, "
				+ "first_deposit_time, last_deposit_time, "
				+ "first_withdrawal_time, last_withdrawal_time, "
				+ "first_adjustment_time, last_adjustment_time, "
				+ "first_bonus_time, last_bonus_time, "
				+ "first_bet_time, "
				+ "last_bet_time, "
				+ "login_time, login_ip, balance, affiliate_id, currency_type_id, country_type, contact_verified, "
				+ "user_channel_type, friend_refer_code, friend_referrer,"
				+ "gender, marital, occupation "
				+ "FROM account WHERE user_id = ? AND website_type = ? AND currency_type_id = ? ";

		return DBQueryRunner
			.getBean(conn, AccountAttach.class, sql, userId, webSiteType.unique(), accountCurrency);
	}

	/*
	 *
	 */
	public static int changePassword(Connection conn, String password, String userID, WebSiteType websiteType,
		int isForgotPassword, boolean unlockAccount)
		throws SQLException {
		StringBuilder sb = new StringBuilder(
			"UPDATE Account SET update_time = SYSTIMESTAMP, password = ?, reset_password = ?, bio_token = null ");

		if (unlockAccount) {
			sb.append(" ,status = ? ,login_fail = 0");
		}

		sb.append(" WHERE user_id = ? AND website_type = ?");

		List<Object> params = new ArrayList<>();
		params.add(password);
		params.add(isForgotPassword);

		if (unlockAccount) {
			params.add(AccountStatusType.ACTIVE.unique());
		}

		params.add(userID);
		params.add(websiteType.unique());

		return DBQueryRunner.update(conn, sb.toString(), params);
	}

	/*
	 * MEMO: deviceType 最後一次登入時所使用的設備
	 */
	public static int updateLastLogin(final Connection conn, String userID, int webSiteType, String loginIP,
		String sessionId, String serverId, int deviceType, int platformType) throws SQLException {

		String sql =
			"UPDATE ACCOUNT SET login_time = SYSTIMESTAMP, update_time = SYSTIMESTAMP, login_ip = ?, SESSION_ID = ?, SERVER_ID = ?, DEVICE_TYPE = ?, platform_type = ?, LOGIN_FAIL = 0 "
				+ " WHERE user_id = ? and website_type = ? ";
		return DBQueryRunner
			.update(conn, sql, loginIP, sessionId, serverId, deviceType, platformType, userID, webSiteType);
	}

	/*
	 * Player logout
	 */
	public static int clearSessionAndServerId(final Connection conn, String pureUserID, int webSiteType,
		String sessionId, String serverId) throws SQLException {

		String sql = "UPDATE ACCOUNT SET update_time = SYSTIMESTAMP, SESSION_ID = null, SERVER_ID = null WHERE SESSION_ID = ? AND SERVER_ID = ? AND user_id = ? and website_type = ? ";
		return DBQueryRunner.update(conn, sql, sessionId, serverId, pureUserID, webSiteType);
	}

	public static int clearSessionAndServerId(final Connection conn, String pureUserID, int webSiteType)
		throws SQLException {

		String sql = "UPDATE ACCOUNT SET update_time = SYSTIMESTAMP, SESSION_ID = null, SERVER_ID = null WHERE user_id = ? and website_type = ? ";
		return DBQueryRunner.update(conn, sql, pureUserID, webSiteType);
	}

	/*
	 * Server init
	 */
	public static int clearWebSessionAndServerId(final Connection conn) throws SQLException {

		String sql =
			"UPDATE account SET update_time = SYSTIMESTAMP, session_id = null, server_id = null "
				+ " WHERE server_id = ? "
				+ " AND session_id > '!' " // 根據ASCII碼，以最小的符號(Symbol)作為清除條件
				+ " AND platform_type = 1 "; // 只處理Web, H5交給 TokenSessionIdClearJob

		return DBQueryRunner.update(conn, sql, SystemInfo.getInstance().getServerID());
	}

	/*
	 * MEMO: for session cluster, when Player change server
	 * 因為 sessionId 不會變, 所以不用改, 只要改 serverId
	 */
	public static int updateServerId(final Connection conn, int webSiteType, String userId, String serverId)
		throws SQLException {

		String sql = "UPDATE account SET update_time = SYSTIMESTAMP, SERVER_ID = ? WHERE  website_type = ? AND user_id = ?";
		return DBQueryRunner.update(conn, sql, serverId, webSiteType, userId);
	}

	public static PageResult<Account> getAccountByUserId(Connection conn, WebSiteType webSiteType, String userId,
		Set<Integer> accessCurrencySet, PageInfo pageInfo, String sortCondition,
		DBOrderType orderType) throws Exception {

		List<Object> params = new ArrayList<>();

		StringBuilder sql = new StringBuilder("SELECT * FROM account WHERE website_type = ? AND user_id like ?");
		params.add(webSiteType.unique());
		params.add("%" + userId + "%");

		if (!accessCurrencySet.isEmpty()) {
			sql.append(" AND currency_type_id IN (");
			sql.append(StringUtils.repeat("?", ",", accessCurrencySet.size()));
			sql.append(")");
			params.addAll(accessCurrencySet);
		}

		sql.append(" ORDER BY ");
		sql.append(sortCondition);
		sql.append(" ");
		sql.append(orderType.getSqlString());

		return DBQueryRunner.getPageResult(conn, Account.class, sql.toString(), pageInfo.getPageNumber(),
			pageInfo.getTotalCount(), params);
	}

	public static boolean exists(Connection conn, String userId, WebSiteType webSiteType) throws SQLException {
		String sql = "SELECT COUNT(*) count FROM account WHERE user_id = ? AND website_type = ? ";

		return DBQueryRunner.getNumber(conn, sql, userId, webSiteType.unique()).intValue() > 0;
	}

	// TODO: need local cache
	//	@CacheServer
	// getAccountByUserIdWithoutDbQueryType
	public static Account getAccountByUserId(Connection conn, String userId, WebSiteType webSiteType)
		throws SQLException {

		return DBQueryRunner.getBean(conn, Account.class,
			"SELECT * FROM account WHERE user_id = ? AND website_type = ? ", userId, webSiteType.unique());
	}

	//	@CacheServer
	public static Account getAccountByUserId(Connection conn, String userId, WebSiteType webSiteType, DBQueryType type)
		throws SQLException {
		StringBuilder sql = new StringBuilder("SELECT * FROM account WHERE user_id = ? AND website_type = ? ");
		if (type != null) {
			sql.append(type.getSqlString());
		}

		return DBQueryRunner.getBean(conn, Account.class, sql.toString(), userId, webSiteType.unique());
	}

	public static Account getAccountByEmail(Connection conn, String email, WebSiteType webSiteType)
		throws SQLException {

		String sql = "SELECT a.*, aci.content as email "
			+ "FROM account a, accountcontactinfo aci "
			+ "WHERE aci.contact_type = ? AND aci.content = ? AND a.website_type = ? AND a.user_id = aci.user_id ";

		return DBQueryRunner.getBean(conn, Account.class, sql, ContactType.Email.unique(), email, webSiteType.unique());
	}

	/**
	 * String serverId = SystemInfo.getInstance().getServerID();
	 */
	private static final String syncLoginAccountCacheWithDbForBeaSql =
		"SELECT website_type, user_id, status, update_time, "
			+ "session_id,  server_id, "
			+ "password, reset_password, login_fail, "
			+ "bio_token, device_type, "
			+ "vip_level, auto_upgrade_vip, summary_convertion_point, "
			+ "first_deposit, balance, use_first_deposit_bonus, "
			+ "min_force_serve, min_deposit_amount, "
			+ "auto_verification, auto_verification_amount, allow_game_type, "
			+ "user_name, birthday, "
			+ "contact_verified, favorite_game, "
			+ "sign_up_time, login_time, login_ip, "
			+ "first_deposit_time, last_deposit_time, first_withdrawal_time, last_withdrawal_time, "
			+ "friend_refer_code, friend_referrer, "
			+ "use_kyc_bonus, use_kyc_bonus_document, use_kyc_bonus_personal_info, "
			+ "updated_attribute FROM account WHERE update_time > ? "
		// temp fix
		//	+ "AND server_id = ? "
		;

	public static Timestamp syncLoginAccountCacheWithDbForBeaLocal(Connection conn,
		Timestamp queryTimestamp,
		Set<Account> invalidAccounts,
		Consumer<Account> checkUpdateAttributeCallback) throws SQLException {

		boolean hasNewData = false;

		final String serverIdInSystem = SystemInfo.getInstance().getServerID();

		Timestamp maxUpdateTime = queryTimestamp;

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(syncLoginAccountCacheWithDbForBeaSql);
			ps.setTimestamp(1, queryTimestamp);
			//			ps.setString(2, serverIdInSystem);
			rs = ps.executeQuery();

			/*
			 * TODO: 如果要優化, 這個 for 可以改成 multi-thread 或是 PubSub
			 */
			while (rs.next()) {

				final int webSiteType = rs.getInt("website_type");
				final String userID = rs.getString("user_id");
				final int status = rs.getInt("status");

				final String userKey = AccountUtils.getUserKey(WebSiteType.getInstance(webSiteType), userID);

				Account userInCache = PlayerLocalCache.getInstance().get(userKey);
				if (userInCache == null) {
					continue;
				}

				Timestamp updateTimeInDb = rs.getTimestamp("update_time");

				if (updateTimeInDb.after(maxUpdateTime)) {
					maxUpdateTime = updateTimeInDb;
				}

				if (AccountUtils.isInactived(status) || AccountUtils.isLocked(status)) {
					// locked player will be removed from Player Cache
					invalidAccounts.add(userInCache);
				}

				final boolean firstLogin = userInCache.isFirstLogin();
				final boolean updateTimeChanged = userInCache.getUpdateTime().getTime() != updateTimeInDb.getTime();

				// UPDATE
				if (firstLogin || updateTimeChanged) {

					hasNewData = true;

					// Check for session change to detect multiple concurrent logins
					final String sessionIdInDb = rs.getString("session_id");
					final String serverIdInDb = rs.getString("server_id");

					String currentSessionId = userInCache.getSessionId();
					String currentServerId = userInCache.getServerId();

					if (currentSessionId == null) {
						currentSessionId = "";
					}
					if (currentServerId == null) {
						currentServerId = "";
					}

					// If sessionId changed and not first login, user logged in from elsewhere
					boolean sessionOrServerChanged = !currentSessionId.equals(sessionIdInDb)
						|| !currentServerId.equals(serverIdInDb);

					if (!firstLogin && sessionOrServerChanged) {

						if (StringUtils.isNotEmpty(currentSessionId)) {
							try {
								boolean invalidated = PlayerLocalCache.getInstance()
									.invalidateAndRemove(currentSessionId);

								if (invalidated) {
									LogUtils.SYS.info(
										"Cache sync detected session change for user {} - invalidated old session {} in favor of new session {}",
										userID, currentSessionId, sessionIdInDb);
								}
							} catch (Exception e) {
								LogUtils.SYS.warn("Failed to invalidate old session {} during cache sync: {}",
									currentSessionId, e.getMessage());
							}
						}
					}

					userInCache.setSessionId(sessionIdInDb);
					userInCache.setServerId(serverIdInDb);

					userInCache.setPassword(rs.getString("password"));
					userInCache.setResetPassword(rs.getInt("reset_password"));
					userInCache.setLoginFail(rs.getInt("login_fail"));

					userInCache.setBioToken(rs.getString("bio_token"));

					userInCache.setStatus(rs.getInt("status"));
					// 1=Mobile
					userInCache.setDeviceType(rs.getInt("device_type"));

					userInCache.setVipLevel(rs.getInt("vip_level"));
					userInCache.setAutoUpgradeVip(rs.getInt("auto_upgrade_vip"));
					userInCache.setSummaryConvertionPoint(rs.getBigDecimal("summary_convertion_point"));

					userInCache.setFirstDeposit(rs.getBigDecimal("first_deposit"));

					try {
						BigDecimal walletBalance = SeamlessWalletApiService.getInstance().getBalance(userKey);

						userInCache.setBalance(walletBalance);

					} catch (Exception e) {
						LogUtils.SYS.error(e.getMessage(), e);
					}

					userInCache.setUseFirstDepositBonus(rs.getInt("use_first_deposit_bonus"));

					if (rs.getObject("min_force_serve") == null) {
						userInCache.setMinForceServe(null);
					} else {
						userInCache.setMinForceServe(rs.getBigDecimal("min_force_serve"));
					}
					if (rs.getObject("min_deposit_amount") == null) {
						userInCache.setMinDepositAmount(null);
					} else {
						userInCache.setMinDepositAmount(rs.getInt("min_deposit_amount"));
					}

					userInCache.setAutoVerification(rs.getInt("auto_verification"));
					userInCache.setAutoVerificationAmount(rs.getBigDecimal("auto_verification_amount"));
					userInCache.setAllowGameType(rs.getInt("allow_game_type"));

					userInCache.setUserName(rs.getString("user_name"));
					userInCache.setBirthday(rs.getTimestamp("birthday"));

					userInCache.setContactVerified(rs.getInt("contact_verified"));
					userInCache.setFavoriteGame(rs.getString("favorite_game"));

					userInCache.setSignUpTime(rs.getTimestamp("sign_up_time"));
					userInCache.setLoginTime(rs.getTimestamp("login_time"));
					userInCache.setLoginIp(rs.getString("login_ip"));

					userInCache.setFirstDepositTime(rs.getTimestamp("first_deposit_time"));
					userInCache.setLastDepositTime(rs.getTimestamp("last_deposit_time"));
					userInCache.setFirstWithdrawalTime(rs.getTimestamp("first_withdrawal_time"));
					userInCache.setLastWithdrawalTime(rs.getTimestamp("last_withdrawal_time"));

					//KYC
					userInCache.setUseKycBonus(rs.getInt("use_kyc_bonus"));
					userInCache.setUseKycBonusDocument(rs.getString("use_kyc_bonus_document"));
					userInCache.setUseKycBonusPersonalInfo(rs.getInt("use_kyc_bonus_personal_info"));

					final int updateAttribute = rs.getInt("updated_attribute");
					userInCache.setUpdatedAttribute(updateAttribute);
					/*
					 */
					checkUpdateAttributeCallback.accept(userInCache);
				}

				// 故意放在最後更新
				userInCache.setUpdateTime(updateTimeInDb);
			}

			if (hasNewData) {
				return new Timestamp(maxUpdateTime.getTime() - PlayerLocalCache.ERROR_VALUE);
			} else {
				final long someTimeAgo = System.currentTimeMillis() - 10 * 1000;
				//MEMO: 若超過 10 秒鐘都沒有資料更新, 則將下次查詢的時間推進 2 秒, 以免一直撈到沒有異動的舊資料
				if (maxUpdateTime.getTime() < someTimeAgo) {
					return new Timestamp(maxUpdateTime.getTime() + 2000);
				} else {
					return maxUpdateTime;
				}
			}

		} finally {
			DbUtils.close(rs);
			DbUtils.close(ps);
		}
	}

	/*
	 *
	 */
	public static Account selectForUpdateForApprove(Connection conn, String userId, int websiteType)
		throws SQLException {

		String sql =
			"SELECT user_id, website_type, first_deposit, first_deposit_time, last_deposit_time, update_time, balance, status, "
				+ "use_first_deposit_bonus, affiliate_id, currency_type_id, country_type, friend_referrer, "
				+ "first_bonus_time, auto_verification, vip_level, allow_force_serve "
				+ "FROM account WHERE user_id = ? AND website_type = ? FOR UPDATE";

		return DBQueryRunner.getBean(conn, Account.class, sql, userId, websiteType);
	}

	public static boolean updateForApproveWithdrawal(Connection conn, String userId, WebSiteType webSiteType,
		boolean isFirstWithdrawal, Timestamp now) throws SQLException {
		StringBuilder sql = new StringBuilder("UPDATE account SET last_withdrawal_time = ?, update_time = ?");

		List<Object> params = new ArrayList<>();
		params.add(now);
		params.add(now);

		if (isFirstWithdrawal) {
			sql.append(", first_withdrawal_time = ?");
			params.add(now);
		}

		sql.append(" WHERE user_id = ? AND website_type = ? ");
		params.add(userId);
		params.add(webSiteType.unique());

		return DBQueryRunner.update(conn, sql.toString(), params) > 0;
	}

	public static String getRegisterUsers(Connection conn, WebSiteType webSiteType, Timestamp startDate,
		Timestamp endDate, int currency, PageInfo pageInfo, String column,
		DBOrderType orderType, long affiliateId, LangMessage lang) throws Exception {

		String selectSql = prepareSelectSqlForRegisterUser();

		StringBuilder sharedSql = prepareShareSqlForRegisterUser();

		List<Object> paramsList = new ArrayList<>();
		paramsList.add(webSiteType.unique());
		paramsList.add(currency);
		if (startDate != null) {
			sharedSql.append(" AND a.sign_up_time >= ? ");
			paramsList.add(startDate);
		}
		if (endDate != null) {
			sharedSql.append(" AND a.sign_up_time <= ? ");
			paramsList.add(endDate);
		}
		if (affiliateId != -1) {
			sharedSql.append(" AND a.affiliate_id = ? ");
			paramsList.add(affiliateId);
		}

		final String sql;

		if (pageInfo.getPageSize() <= 0) {
			sql = selectSql + sharedSql.append(" ORDER BY ").append(column).append(orderType.getSqlString());
		} else {
			String countSql = " SELECT COUNT(*) FROM ( " + selectSql + sharedSql + ")";
			int totalCount = DBQueryRunner.getNumber(conn, countSql, paramsList).intValue();
			pageInfo.setTotalCount(totalCount);

			paramsList.add(pageInfo.getLastRowNumber());
			paramsList.add(pageInfo.getFirstRowNumber());

			sql = OracleUtils.getCalculatedPageSQL(
				selectSql + sharedSql.append(" ORDER BY ").append(column).append(orderType.getSqlString()));
		}

		PreparedStatement ps = null;
		ResultSet rs = null;
		StringWriter out = new StringWriter();
		JsonGenerator jGenerator = null;
		try {
			ps = conn.prepareStatement(sql);

			DBQueryRunner.fillStatement(ps, ps.getParameterMetaData(), paramsList);
			rs = ps.executeQuery();
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartArray();

			int pageCount = 0;
			while (rs.next()) {
				pageCount++;

				jGenerator.writeStartObject();
				jGenerator.writeStringField("userName", rs.getString("user_name"));
				jGenerator.writeStringField("userId", rs.getString("user_id"));
				jGenerator.writeStringField("signUpCountry", rs.getString("sign_up_country"));
				jGenerator.writeStringField("signUpState", rs.getString("sign_up_state"));
				jGenerator.writeStringField("signUpCity", rs.getString("sign_up_city"));
				jGenerator.writeStringField("signUpTime", FormatUtils.dateFormat(rs.getTimestamp("sign_up_time")));
				jGenerator.writeStringField("affiliate", rs.getString("affiliate"));
				jGenerator.writeNumberField("depositAmount", rs.getBigDecimal("deposit_amount"));
				jGenerator.writeNumberField("currencyTypeId", rs.getInt("currency_type_id"));

				jGenerator.writeStringField("userChannelType",
					lang.get("ui.text.member.userChannelType.DIRECT"));

				jGenerator.writeStringField("channelName", "");
				jGenerator.writeEndObject();
			}

			if (pageInfo.getPageSize() <= 0) {
				pageInfo.setTotalCount(pageCount);
			}

			jGenerator.writeEndArray();

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.closeAll(ps, rs);
			JSONUtils.close(jGenerator);
		}
		return out.toString();

	}

	public static void updateForRejectWithdrawal(Connection conn, BigDecimal amount, String userId,
		int websiteType, long moneyTransactionId) throws SQLException {

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(
				"SELECT update_time, balance, status, server_id, updated_attribute, currency_type_id FROM account " +
					"WHERE user_id = ? AND website_type = ? " +
					"AND status != ? " +
					"FOR UPDATE",
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);

			DBQueryRunner.fillStatement(ps, ps.getParameterMetaData(), userId, websiteType,
				AccountStatusType.SUSPEND.unique());

			rs = ps.executeQuery();

			if (rs.next()) {
				//				BigDecimal withdrawalAmount = rs.getBigDecimal("withdrawal_amount");
				BigDecimal balance = rs.getBigDecimal("balance");
				BigDecimal oldBalance = balance;
				balance = balance.add(amount);
				BigDecimal newBalance = balance;

				rs.updateTimestamp("update_time", new Timestamp(System.currentTimeMillis()));
				rs.updateBigDecimal("balance", balance);
				rs.updateInt("updated_attribute",
					rs.getInt("updated_attribute") | UpdatedAttributeType.MULTIPLE_TRANSACTION.unique());

				rs.updateRow();

			} else {
				throw new Deviation("moneyTransactionId: " + moneyTransactionId + " updateForRejectWithdrawal fail");
			}
		} finally {
			DbUtils.closeAll(ps, rs);
		}
	}

	public static Timestamp getLastLoginTime(Connection conn, int webSiteType, String userId)
		throws SQLException {
		String sql = "SELECT login_time FROM account WHERE website_type = ? AND user_id = ? ";

		return DBQueryRunner.getTimeStamp(conn, sql, webSiteType, userId);
	}

	public static int updateCloseAutoVerification(Connection conn, int websiteTypeId, String userId)
		throws SQLException {
		String sql = "UPDATE Account SET auto_verification = 0, update_time = SYSTIMESTAMP WHERE user_id = ? AND website_type = ? AND auto_verification = 1";
		return DBQueryRunner.update(conn, sql, userId, websiteTypeId);
	}

	public static int updateFavoriteGame(Connection conn, String favoriteGames, int webSiteType, String userId)
		throws SQLException {
		String sql = "UPDATE Account SET favorite_game = ?, update_time = SYSTIMESTAMP WHERE website_type = ? AND user_id = ?";
		return DBQueryRunner.update(conn, sql, favoriteGames, webSiteType, userId);
	}

	public static boolean updateForAdjustment(Connection conn, String userId, WebSiteType webSiteType,
		boolean isFirstAdjustment, Timestamp now) throws SQLException {
		StringBuilder sql = new StringBuilder("UPDATE account SET last_adjustment_time = ?, update_time = ? ");

		List<Object> params = new ArrayList<>();
		params.add(now);
		params.add(now);

		if (isFirstAdjustment) {
			sql.append(", first_adjustment_time = ?");
			params.add(now);
		}

		sql.append(" WHERE user_id = ? AND website_type = ? ");
		params.add(userId);
		params.add(webSiteType.unique());

		return DBQueryRunner.update(conn, sql.toString(), params) > 0;
	}

	/**
	 * 查找过去指定月数内没有创建MoneyTransaction的用户
	 *
	 * @param conn                    数据库连接
	 * @param websiteType             网站类型，可选
	 * @param includeInactiveAccounts 是否包含非活跃账户
	 * @return 无交易用户列表
	 * @throws SQLException
	 */

	// temp solution
	public static List<Account> findUsersWithoutTransactions(
		Connection conn,
		Integer websiteType,
		boolean includeInactiveAccounts) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT a.user_id, a.website_type, sw.balance ");
		sql.append("FROM account a ");
		sql.append("LEFT JOIN seamlessWallet sw ON sw.user_key = a.website_type || '_' || a.user_id ");
		sql.append("WHERE ");

		// 条件1: 过去24个月没有投注
		sql.append("(a.last_bet_time IS NULL OR a.last_bet_time < ADD_MONTHS(SYSDATE, -24)) ");

		// ADD_MONTHS(SYSDATE, -24)

		// 条件2: 过去24个月没有存款/提款
		sql.append("AND NOT EXISTS ( ");
		sql.append("    SELECT 1 FROM moneyTransaction mt ");
		sql.append("    WHERE mt.user_id = a.user_id ");
		sql.append("    AND mt.transaction_type IN (");
		//		sql.append(MoneyTransactionType.WITHDRAWALS.unique()).append(", ");
		//		sql.append(MoneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY.unique()).append(", ");
		sql.append(MoneyTransactionType.DEPOSIT.unique()).append(", ");
		sql.append(MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique());
		sql.append(") ");
		sql.append("    AND mt.create_time >= ADD_MONTHS(SYSDATE, -24) ");
		sql.append("    AND NOT (mt.creator = 'SYS' AND mt.status = ?) ");
		sql.append(") ");

		// 条件3: 钱包有余额
		sql.append("AND sw.balance > 0 ");

		// 条件4: 没有 Pending 状态的 SYS 提款
		sql.append("AND NOT EXISTS ( ");
		sql.append("    SELECT 1 FROM moneyTransaction mt ");
		sql.append("    WHERE mt.user_id = a.user_id ");
		sql.append("    AND mt.creator = 'SYS' ");
		sql.append("    AND mt.transaction_type in (");
		sql.append(MoneyTransactionType.WITHDRAWALS.unique()).append(", ");
		sql.append(MoneyTransactionType.WITHDRAWAL_PAYMENT_GATEWAY.unique()).append(") ");
		sql.append("    AND mt.status = ? ");  // PENDING
		sql.append(") ");

		List<Object> params = new ArrayList<>();
		params.add(MoneyTransactionStatusType.CLOSE.unique());
		params.add(MoneyTransactionStatusType.NEW.unique());

		sql.append("AND a.website_type = ? ");
		params.add(websiteType);

		if (!includeInactiveAccounts) {
			sql.append("AND a.status = 1 ");
		}

		sql.append("AND a.create_time >= ADD_MONTHS(SYSDATE, -24) ");

		return DBQueryRunner.getBeanList(conn, Account.class, sql.toString(), params);
	}

	public static boolean updateForApproveDeposit(Connection conn, Account account, boolean isFirstDeposit,
		BigDecimal depositAmount, Timestamp approveTime)
		throws SQLException {

		List<Object> params = new ArrayList<>();
		params.add(approveTime);
		params.add(approveTime);

		String sql = "UPDATE account SET update_time = ?, last_deposit_time = ?";

		if (isFirstDeposit) {
			sql += ", first_deposit_time = ?, first_deposit = ?";
			params.add(approveTime);
			params.add(depositAmount);
		}

		sql += " WHERE user_id = ? AND website_type = ?";

		params.add(account.getUserId());
		params.add(account.getWebsiteType());

		return DBQueryRunner.update(conn, sql, params) > 0;
	}

	public static int updateViewedPlayResponsibly(Connection conn, String userID, int webSiteType,
		int viewedPlayResponsibly)
		throws SQLException {
		String sql = "UPDATE ACCOUNT SET UPDATE_TIME = SYSTIMESTAMP, VIEWED_PLAY_RESPONSIBLY = ? "
			+ "WHERE USER_ID = ? AND WEBSITE_TYPE = ? ";

		return DBQueryRunner.update(conn, sql, viewedPlayResponsibly, userID, webSiteType);
	}

	/**
	 * Finds players with no successful bet or deposit within the configured inactivity window,
	 * who still have a positive wallet balance. These accounts are candidates for the daily
	 * auto-withdrawal job (see requirement: BO Payment - Withdrawal - Daily Auto Withdrawal Check).
	 *
	 * <h3>Inactivity window (configurable)</h3>
	 * <p>
	 * The lookback period defaults to <strong>24 months</strong> per the requirement spec
	 * ({@code docs/requirements/payment/bo/withdrawal_auto_check.md}).
	 * It can be overridden at runtime via the system property:
	 * </p>
	 * <pre>
	 *   -Dauto.withdrawal.inactivity.months=N
	 * </pre>
	 * <p>
	 * This is useful for development and QA testing without modifying production code.
	 * For example, to test with a 1-month window:
	 * </p>
	 * <pre>
	 *   mvn test -P CY -Dauto.withdrawal.inactivity.months=1
	 * </pre>
	 * <p>
	 * Setting it to 0 effectively treats ALL accounts as inactive (useful for smoke testing
	 * the withdrawal creation flow).
	 * </p>
	 *
	 * <h3>SQL compatibility</h3>
	 * <p>
	 * Uses {@code ADD_MONTHS(SYSDATE, -N)} which is Oracle-native. In the H2 test environment,
	 * {@link com.nv.test.H2DataSource.ConnectionProxy} automatically converts this to
	 * {@code DATEADD(MONTH, -N, CURRENT_TIMESTAMP)}.
	 * </p>
	 *
	 * @param conn                    database connection
	 * @param websiteType             website type identifier
	 * @param includeInactiveAccounts if true, includes accounts with status != ACTIVE
	 * @return list of accounts eligible for auto-withdrawal
	 */
	public static List<Account> findUsersWithoutSuccessfulTransactions(
		Connection conn,
		int websiteType,
		boolean includeInactiveAccounts) throws SQLException {

		// Read inactivity window from system property; default = 24 months per requirement spec.
		// Override with -Dauto.withdrawal.inactivity.months=N for dev/QA testing.

		int inactivityMonths = Integer.parseInt(
			System.getProperty("auto.withdrawal.inactivity.months", "24"));

		boolean isDev = SystemInfo.getInstance().isDev();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT a.user_id, a.website_type, sw.balance, a.currency_type_id ");
		sql.append("FROM account a ");
		sql.append("JOIN seamlessWallet sw ON sw.user_key = a.website_type || '_' || a.user_id ");
		sql.append("WHERE a.website_type = ? ");
		sql.append("AND sw.balance > 0 ");
		if (isDev) {
			sql.append("    AND a.sign_up_time <= SYSDATE - INTERVAL '10' MINUTE ");
		} else {
			sql.append("AND a.sign_up_time <= ADD_MONTHS(SYSDATE, -").append(inactivityMonths).append(") ");
			//			sql.append("AND a.sign_up_time <= SYSDATE - INTERVAL '1' DAY ");
			//			sql.append("    AND a.sign_up_time <= SYSDATE - INTERVAL '60' MINUTE ");
		}

		List<Object> params = new ArrayList<>();
		params.add(websiteType);
		if (!includeInactiveAccounts) {
			sql.append("AND a.status = ? ");
			params.add(AccountStatusType.ACTIVE.unique());
		}

		// Condition 1: no settled bet within the inactivity window
		sql.append("AND NOT EXISTS ( ");
		sql.append("    SELECT 1 FROM gameTransaction gt ");
		sql.append("    WHERE gt.user_id = a.user_id ");
		sql.append("    AND gt.settle_time IS NOT NULL ");

		if (isDev) {
			sql.append("    AND gt.settle_time >= SYSDATE - INTERVAL '10' MINUTE ");
		} else {
			sql.append("    AND gt.settle_time >= ADD_MONTHS(SYSDATE, -").append(inactivityMonths).append(") ");
			//			sql.append("    AND gt.settle_time >= SYSDATE - INTERVAL '60' MINUTE ");
		}
		sql.append(") ");

		// Condition 2: no confirmed deposit within the inactivity window
		sql.append("AND NOT EXISTS ( ");
		sql.append("    SELECT 1 FROM moneyTransaction mt ");
		sql.append("    WHERE mt.user_id = a.user_id ");
		sql.append("    AND mt.website_type = a.website_type ");
		sql.append("    AND mt.transaction_type IN (?, ?) ");
		sql.append("    AND mt.status = ? ");

		if (isDev) {
			sql.append("    AND mt.create_time >= SYSDATE - INTERVAL '10' MINUTE ");
		} else {
			sql.append("    AND mt.create_time >= ADD_MONTHS(SYSDATE, -").append(inactivityMonths).append(") ");
			//			sql.append("    AND mt.create_time >= SYSDATE - INTERVAL '60' MINUTE ");
		}
		sql.append(") ");
		params.add(MoneyTransactionType.DEPOSIT.unique());
		params.add(MoneyTransactionType.DEPOSIT_PAYMENT_GATEWAY.unique());
		params.add(MoneyTransactionStatusType.CONFIRMED.unique());

		return DBQueryRunner.getBeanList(conn, Account.class, sql.toString(), params);

	}
}
