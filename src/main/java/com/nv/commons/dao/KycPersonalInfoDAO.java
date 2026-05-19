package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;

import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.KycPersonalInfo;
import com.nv.commons.model.database.DBQueryRunner;

public class KycPersonalInfoDAO {

	public static KycPersonalInfo find(Connection conn, long accountDocumentId, String userId, WebSiteType webSiteType)
		throws SQLException {

		return DBQueryRunner.getBean(conn, KycPersonalInfo.class,
			"SELECT * FROM kycpersonalinfo WHERE account_document_id = ? AND user_id = ? AND website_type = ? ",
			accountDocumentId, userId, webSiteType.unique());
	}

	public static int insert(Connection conn, KycPersonalInfo kycPersonalInfo) throws SQLException {

		String sql = """
			INSERT INTO kycpersonalinfo (account_document_id, user_id, website_type,
			document_no, first_name, last_name, dob, street, city, postal_code, country,
			creator, create_time, updater, update_time)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, ?, SYSTIMESTAMP)
			""";

		return DBQueryRunner.update(conn, sql,
			kycPersonalInfo.getAccountDocumentId(), kycPersonalInfo.getUserId(), kycPersonalInfo.getWebsiteType(),
			kycPersonalInfo.getDocumentNo(), kycPersonalInfo.getFirstName(), kycPersonalInfo.getLastName(), kycPersonalInfo.getDob(),
			kycPersonalInfo.getStreet(), kycPersonalInfo.getCity(), kycPersonalInfo.getPostalCode(), kycPersonalInfo.getCountry(),
			kycPersonalInfo.getCreator(), kycPersonalInfo.getUpdater()
		);
	}

	public static int update(Connection conn, KycPersonalInfo kycPersonalInfo) throws SQLException {

		String sql = """
			UPDATE kycpersonalinfo
			SET document_no = ?, first_name = ?, last_name = ?, dob = ?, street = ?, city = ?, postal_code = ?, country = ?,
			updater = ?, update_time = SYSTIMESTAMP
			WHERE account_document_id = ? AND user_id = ? AND website_type = ?
			""";

		return DBQueryRunner.update(conn, sql,
			kycPersonalInfo.getDocumentNo(), kycPersonalInfo.getFirstName(), kycPersonalInfo.getLastName(), kycPersonalInfo.getDob(),
			kycPersonalInfo.getStreet(), kycPersonalInfo.getCity(), kycPersonalInfo.getPostalCode(), kycPersonalInfo.getCountry(),
			kycPersonalInfo.getUpdater(),
			kycPersonalInfo.getAccountDocumentId(), kycPersonalInfo.getUserId(), kycPersonalInfo.getWebsiteType()
		);
	}

	public static boolean isExistRecord(Connection conn, long accountDocumentId, String userId, WebSiteType webSiteType) throws SQLException {
		Number count = DBQueryRunner.getNumber(conn,
			"SELECT COUNT(1) FROM kycpersonalinfo WHERE account_document_id = ? AND user_id = ? AND website_type = ?",
			accountDocumentId, userId, webSiteType.unique());
		return count != null && count.intValue() > 0;
	}
}
