package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.nv.commons.constants.BinaryStatusType;
import com.nv.commons.constants.DocumentGroupType;
import com.nv.commons.constants.DocumentStatusType;
import com.nv.commons.constants.DocumentType;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountDocument;
import com.nv.commons.model.database.DBQueryRunner;
import com.nv.commons.model.database.DataBeanProcessor;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.OracleUtils;

import com.nv.module.backendapi.cache.PlayerLocalCache;
import org.jetbrains.annotations.NotNull;

public class AccountDocumentDAO {

	public static long getAccountDocumentId(Connection conn) throws SQLException {
		String sql = "SELECT accountDocument_id_seq.NEXTVAL FROM DUAL";
		return DBQueryRunner.getNumber(conn, sql).longValue();
	}

	public static void batchInsert(Connection conn, AccountDocument... documents) throws SQLException {
		String sql = "INSERT INTO accountDocument("
					 + "  id, website_type, user_id, currency_type_id, document_type, "//5
					 + " document_index, document_no, expired_date, front_image_path, back_image_path, status, executor," //12
					 + " creator, create_time, updater, update_time, approved_userid," //16
					 + " approved_time, approved_remark, group_type)"//18
					 + " VALUES("
					 + "  accountDocument_id_seq.NEXTVAL, ?, ?, ?, ?,"//5
					 + "  ?, ?, ?, ?, ?, ?, ?, "//11
					 + "  ?, SYSTIMESTAMP, ?, SYSTIMESTAMP, ?,"//16
					 + "  ?, ?, ?)";//18

		List<Object[]> paramList = Arrays.stream(documents)
			.map(accountDocument -> new Object[] {
				accountDocument.getWebsiteType(),
				accountDocument.getUserId(),
				accountDocument.getCurrencyTypeId(),
				accountDocument.getDocumentType(),
				accountDocument.getDocumentIndex(),
				accountDocument.getDocumentNo(),
				accountDocument.getExpiredDate(),
				accountDocument.getFrontImagePath(),
				accountDocument.getBackImagePath(),
				accountDocument.getStatus(),
				accountDocument.getExecutor(),
				accountDocument.getCreator(),
				accountDocument.getUpdater(),
				accountDocument.getApprovedUserid(),
				accountDocument.getApprovedTime(),
				accountDocument.getApprovedRemark(),
				accountDocument.getGroupType()
			})
			.collect(Collectors.toList());

		DBQueryRunner.batch(conn, sql, paramList);
	}

	public static long insert(Connection conn, AccountDocument document) throws SQLException {

		String sql = "INSERT INTO accountDocument("
			+ "  id, website_type, user_id, currency_type_id, document_type, "//5
			+ " document_index, document_no, expired_date, front_image_path, back_image_path, status, executor," //12
			+ " creator, create_time, updater, update_time, approved_userid," //16
			+ " approved_time, approved_remark, group_type, residence_image_path)"//18
			+ " VALUES("
			+ "  ?, ?, ?, ?, ?,"//5
			+ "  ?, ?, ?, ?, ?, ?, ?, "//11
			+ "  ?, SYSTIMESTAMP, ?, SYSTIMESTAMP, ?,"//16
			+ "  ?, ?, ?, ?)";//18

		if (document.getId() == 0) {
			document.setId(getAccountDocumentId(conn));
		}

		int insertCount = DBQueryRunner.update(conn, sql,
			document.getId(), document.getWebsiteType(), document.getUserId(), document.getCurrencyTypeId(),
			document.getDocumentType(), document.getDocumentIndex(), document.getDocumentNo(),
			document.getExpiredDate(), document.getFrontImagePath(), document.getBackImagePath(),
			document.getStatus(), document.getExecutor(),
			document.getCreator(), document.getUpdater(), document.getApprovedUserid(),
			document.getApprovedTime(), document.getApprovedRemark(), document.getGroupType(), document.getResidenceImagePath()
		);

		return insertCount != 1 ? 0 : document.getId();
	}

	public static AccountDocument findById(Connection conn, long id)
		throws SQLException {

		return DBQueryRunner.getBean(conn, AccountDocument.class,
			"SELECT * FROM accountDocument WHERE id = ? ", id);
	}

	// 只有Document
	public static List<AccountDocument> findDocumentByUserId(Connection conn, String userId,
		WebSiteType webSiteType) throws SQLException {

		String sql = prepareSelectSqlForLatestDocument()
					 + "WHERE website_type = ? AND user_id = ? AND is_deleted = ? AND group_type = ?) "
					 + selectSqlForLatestDocument();

		return DBQueryRunner
			.getBeanList(conn, AccountDocument.class, sql, webSiteType.unique(), userId,
				BinaryStatusType.INACTIVE.unique(),
				DocumentGroupType.DOCUMENT.unique());
	}


	public static Map<String, List<AccountDocument>> getAccountDocumentByUserKeys(Connection conn,
		String[][] userKeys, Function<String, Account> getAccountFunc) throws SQLException {

		Map<String, List<AccountDocument>> accountDocumentMap = new HashMap<>();
		// MEMO: WITH lastRecord
		StringBuilder sql = new StringBuilder(prepareSelectSqlForLatestDocument());

		String groupCondition = OracleUtils.getGroupCondition(userKeys.length, "Q_COMBINE_USER_ID_ARRAY");
		String selectSql = selectSqlForLatestDocument();

		sql.append(" WHERE (user_id, website_type) IN (")
			.append(groupCondition)
			.append(") AND is_deleted = ? AND group_type = ? ) ");

		sql.append(selectSql);

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql.toString());
			// MEMO: h2 不支援 Oracle ARRAY, 所以參數數量會跟 oracle db 不同
			if ("junit".equalsIgnoreCase(
				System.getProperty(SystemConstants.RUNTIME_ENV))) {

				int webSiteType = Integer.parseInt(userKeys[0][0]);
				String userId = userKeys[0][1];

				ps.setString(1, userId);
				ps.setInt(2, webSiteType);
				ps.setInt(3, BinaryStatusType.INACTIVE.unique());
				ps.setInt(4, DocumentGroupType.DOCUMENT.unique());
			} else {
				ps.setObject(1, OracleUtils.getOracleARRAY(conn, "Q_COMBINE_USER_ID_ARRAY", userKeys));
				ps.setInt(2, BinaryStatusType.INACTIVE.unique());
				ps.setInt(3, DocumentGroupType.DOCUMENT.unique());
			}

			rs = ps.executeQuery();

			while (rs.next()) {

				WebSiteType webSiteType = WebSiteType.getInstance(rs.getInt("website_type"));

				String userKey = AccountUtils.getUserKey(webSiteType, rs.getString("user_id"));

				Account userInCache = getAccountFunc.apply(userKey);
				// TODO: why check userInCache?
				if (userInCache == null) {
					continue;
				}

				List<AccountDocument> accountDocumentList = accountDocumentMap.computeIfAbsent(userKey,
					k -> new ArrayList<>());

				AccountDocument accountDocument = new AccountDocument();
				accountDocument.setId(rs.getLong("id"));
				accountDocument.setWebsiteType(rs.getInt("website_type"));
				accountDocument.setUserId(rs.getString("user_id"));
				accountDocument.setDocumentType(rs.getInt("document_type"));
				accountDocument.setDocumentNo(rs.getString("document_no"));
				accountDocument.setDocumentIndex(rs.getInt("document_index"));
				accountDocument.setExpiredDate(rs.getTimestamp("expired_date"));
				accountDocument.setFrontImagePath(rs.getString("front_image_path"));
				accountDocument.setBackImagePath(rs.getString("back_image_path"));
				accountDocument.setStatus(rs.getInt("status"));
				accountDocument.setCreateTime(rs.getTimestamp("create_time"));
				accountDocument.setUpdateTime(rs.getTimestamp("update_time"));
				accountDocument.setCreator(rs.getString("creator"));
				accountDocument.setApprovedUserid(rs.getString("approved_userid"));
				accountDocument.setApprovedTime(rs.getTimestamp("approved_time"));
				accountDocument.setApprovedRemark(rs.getString("approved_remark"));

				accountDocumentList.add(accountDocument);
			}
		} finally {
			DbUtils.close(rs);
			DbUtils.close(ps);
		}

		return accountDocumentMap;
	}


	public static Map<String, EnumMap<DocumentType, List<AccountDocument>>> getOtherGroupDocumentByUserKeys(
		Connection conn,
		String[][] userKeys) throws SQLException {

		Map<String, EnumMap<DocumentType, List<AccountDocument>>> accountDocumentMap = new HashMap<>();

		String sql = "SELECT * FROM ( "
					 + "SELECT d.id, d.user_id, d.website_type, document_index, d.document_type, expired_date, status, "
					 + "front_image_path, back_image_path, d.create_time, d.creator, "
					 + "d.update_time, d.approved_time, d.approved_userid, d.approved_remark, "
					 + "document_no, group_type, "
					 + "ROW_NUMBER() OVER (PARTITION BY d.user_id, d.website_type,document_no ORDER BY d.create_time DESC) AS rn "
					 + "FROM accountBank b LEFT JOIN accountDocument d "
					 + "ON d.website_type = b.website_type AND d.user_id = b.user_id AND d.id = b.document_id "
					 + "WHERE (d.USER_ID, d.WEBSITE_TYPE) IN (" + OracleUtils.getGroupCondition(userKeys.length) + ") "
					 + "AND d.is_deleted = ? "
					 + "AND d.group_type NOT IN (?, ?)) "
					 + "WHERE rn = 1";

		WebSiteType webSiteType;
		String userKey;

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			ps = conn.prepareStatement(sql);
			ps.setObject(1, OracleUtils.getOracleARRAY(conn, "Q_COMBINE_USER_ID_ARRAY", userKeys));
			ps.setInt(2, BinaryStatusType.INACTIVE.unique());
			ps.setInt(3, DocumentGroupType.DOCUMENT.unique());
			ps.setInt(4, DocumentGroupType.BANK.unique());

			rs = ps.executeQuery();

			while (rs.next()) {

				webSiteType = WebSiteType.getInstance(rs.getInt("website_type"));

				userKey = AccountUtils.getUserKey(webSiteType, rs.getString("user_id"));

				Account userInCache = PlayerLocalCache.getInstance().get(userKey);

				if (userInCache == null) {
					continue;
				}

				EnumMap<DocumentType, List<AccountDocument>> groupDocumentMap = accountDocumentMap.computeIfAbsent(
					userKey, k -> new EnumMap<>(DocumentType.class));

				DocumentType documentType = DocumentType.getInstance(rs.getInt("document_type"));
				List<AccountDocument> documentList = groupDocumentMap.computeIfAbsent(documentType,
					group -> new ArrayList<>());

				AccountDocument accountDocument = new AccountDocument();
				accountDocument.setId(rs.getLong("id"));
				accountDocument.setWebsiteType(rs.getInt("website_type"));
				accountDocument.setUserId(rs.getString("user_id"));
				accountDocument.setDocumentType(rs.getInt("document_type"));
				accountDocument.setDocumentNo(rs.getString("document_no"));
				accountDocument.setDocumentIndex(rs.getInt("document_index"));
				accountDocument.setExpiredDate(rs.getTimestamp("expired_date"));
				accountDocument.setFrontImagePath(rs.getString("front_image_path"));
				accountDocument.setBackImagePath(rs.getString("back_image_path"));
				accountDocument.setStatus(rs.getInt("status"));
				accountDocument.setCreateTime(rs.getTimestamp("create_time"));
				accountDocument.setUpdateTime(rs.getTimestamp("update_time"));
				accountDocument.setCreator(rs.getString("creator"));
				accountDocument.setApprovedUserid(rs.getString("approved_userid"));
				accountDocument.setApprovedTime(rs.getTimestamp("approved_time"));
				accountDocument.setApprovedRemark(rs.getString("approved_remark"));
				accountDocument.setGroupType(rs.getInt("group_type"));

				documentList.add(accountDocument);

			}
		} finally {
			DbUtils.close(rs);
			DbUtils.close(ps);
		}

		return accountDocumentMap;
	}


	private static String prepareSelectSqlForLatestDocument() {
		return "WITH lastRecord AS ("
			   + "SELECT id, website_type, user_id, document_type, document_no, document_index, expired_date, front_image_path, back_image_path, status, create_time, creator, update_time, approved_userid, approved_time, approved_remark, group_type, residence_image_path, "
			   + "ROW_NUMBER() OVER (PARTITION BY user_id,website_type,document_type ORDER BY create_time DESC) AS rn FROM accountDocument ";
	}

	private static String selectSqlForLatestDocument() {
		return
			"SELECT id, website_type, user_id, document_type, document_no, document_index, expired_date, front_image_path, back_image_path, status, create_time, creator, update_time, approved_userid, approved_time, approved_remark, group_type, residence_image_path "
			+ "FROM lastRecord WHERE rn = 1";
	}


	public static int updateAfterSumsubReviewed(Connection conn, @NotNull AccountDocument accountDocument) throws SQLException {

		String sql = """
			UPDATE accountDocument
			SET front_image_path = ?, back_image_path = ?, residence_image_path = ?, expired_date = ?,
			approved_userid = ?, approved_remark = ?, approved_time = SYSTIMESTAMP, status = ?,
			updater = ?, update_time = SYSTIMESTAMP
			WHERE id = ? AND user_id = ? AND website_type = ? AND document_type = ?
			""";

		return DBQueryRunner.update(conn, sql,
			accountDocument.getFrontImagePath(), accountDocument.getBackImagePath(),
			accountDocument.getResidenceImagePath(), accountDocument.getExpiredDate(),
			accountDocument.getApprovedUserid(), accountDocument.getApprovedRemark(), accountDocument.getStatus(),
			accountDocument.getUpdater(),
			accountDocument.getId(), accountDocument.getUserId(), accountDocument.getWebsiteType(), accountDocument.getDocumentType()
		);
	}

	public static int updateStatus(Connection conn, long id, String userId, WebSiteType webSiteType, DocumentType documentType, DocumentStatusType status, String updater)
		throws SQLException {

		String sql = """
			UPDATE accountDocument SET status = ?, updater = ?, update_time = SYSTIMESTAMP
			WHERE id = ? AND user_id = ? AND website_type = ? AND document_type = ?
			""";

		return DBQueryRunner.update(conn, sql, status.unique(), updater, id, userId, webSiteType.unique(), documentType.unique());
	}

	public static int updateRemarkOrImagePathOrExpiryDate(Connection conn, long id, String userId, WebSiteType webSiteType, DocumentType documentType,
		String remark,
		String frontPhotoPath, String backPhotoPath, String residenceImagePath, String updater, Timestamp expirydate) throws SQLException {

		StringBuilder sql = new StringBuilder();
		List<Object> params = new ArrayList<>();

		sql.append("UPDATE accountDocument SET ");

		boolean hasUpdate = false;

		if (remark != null) {
			sql.append("approved_remark = ?");
			params.add(remark);
			hasUpdate = true;
		}

		if (frontPhotoPath != null) {
			if (hasUpdate) {
				sql.append(", ");
			}
			// Empty string means "clear this field to NULL"
			sql.append("front_image_path = ?");
			params.add(frontPhotoPath);
			hasUpdate = true;
		}

		if (backPhotoPath != null) {
			if (hasUpdate) {
				sql.append(", ");
			}
			// Empty string means "clear this field to NULL"
			sql.append("back_image_path = ?");
			params.add(backPhotoPath.trim().isEmpty() ? null : backPhotoPath);
			hasUpdate = true;
		}

		if (residenceImagePath != null) {
			if (hasUpdate) {
				sql.append(", ");
			}
			// Empty string means "clear this field to NULL"
			sql.append("residence_image_path = ?");
			params.add(residenceImagePath.trim().isEmpty() ? null : residenceImagePath);
			hasUpdate = true;
		}

		if(expirydate !=null){
			if (hasUpdate) {
				sql.append(", ");
			}
			sql.append("expired_date = ?");
			params.add(expirydate);
			hasUpdate = true;
		}

		if (params.isEmpty()) {
			return 0; // No update needed
		}

		sql.append(", updater = ?, update_time = SYSTIMESTAMP");
		sql.append(" WHERE id = ? AND user_id = ? AND website_type = ? AND document_type = ?");

		params.add(updater);
		params.add(id);
		params.add(userId);
		params.add(webSiteType.unique());
		params.add(documentType.unique());

		return DBQueryRunner.update(conn, sql.toString(), params.toArray());
	}
	
}
