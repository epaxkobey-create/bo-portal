package com.nv.commons.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.nv.commons.model.database.DBQueryRunner;
import com.nv.commons.utils.DateUtils;
import com.nv.module.kyc.sumsub.dto.SumsubVerificationRs;

public class SumsubReviewHistoryDAO {

	public static long getReviewHistoryId(Connection conn) throws SQLException {
		String sql = "SELECT SUMSUBREVIEWHISTORY_ID_SEQ.NEXTVAL FROM DUAL";
		return DBQueryRunner.getNumber(conn, sql).longValue();
	}

	public static int insertReviewHistory(Connection conn, SumsubVerificationRs sumsubVerificationRs)
		throws SQLException {

		long id = getReviewHistoryId(conn);

		// Use createdAtMs from webhook (Sumsub's timestamp) instead of SYSTIMESTAMP
		// This enables proper stale webhook detection by comparing timestamps
		Timestamp webhookTimestamp = DateUtils.parseSumsubCreatedAtMs(sumsubVerificationRs.getCreatedAtMs());

		String sql = """
			INSERT INTO sumsubreviewhistory(id, applicant_id, external_user_id, type, correlation_id, inspection_id,
			review_status, create_time)
			VALUES(?, ?, ?, ?, ?, ?, ?, ?)
			""";

		int insertCount = DBQueryRunner.update(conn, sql,
			id, sumsubVerificationRs.getApplicantId(), sumsubVerificationRs.getExternalUserId(),
			sumsubVerificationRs.getWebhookType(), sumsubVerificationRs.getCorrelationId(),
			sumsubVerificationRs.getInspectionId(), sumsubVerificationRs.getReviewStatus(),
			webhookTimestamp
		);

		if (insertCount == 1 && sumsubVerificationRs.getReviewResult() != null) {
			SumsubReviewResultDAO.insertReviewResult(conn, id, sumsubVerificationRs.getReviewResult());
		}

		return insertCount;
	}


	public static int updateAccountDocumentId(Connection conn, SumsubVerificationRs sumsubVerificationRs,
		long accountDocumentId) throws SQLException {

		String sql = """
			UPDATE sumsubreviewhistory
			SET account_document_id = ?
			WHERE applicant_id = ? AND external_user_id = ? AND inspection_id = ?
			""";

		return DBQueryRunner.update(conn, sql,
			accountDocumentId, sumsubVerificationRs.getApplicantId(), sumsubVerificationRs.getExternalUserId(),
			sumsubVerificationRs.getInspectionId()
		);
	}

	public static long getAccountDocumentId(Connection conn, SumsubVerificationRs sumsubVerificationRs)
		throws SQLException {

		String sql = """
			SELECT * FROM (
				SELECT account_document_id FROM sumsubreviewhistory
				WHERE applicant_id = ? AND external_user_id = ? AND inspection_id = ?
				AND account_document_id is not NULL
			) WHERE ROWNUM = 1
			""";

		return DBQueryRunner.getNumber(conn, sql,
			sumsubVerificationRs.getApplicantId(), sumsubVerificationRs.getExternalUserId(),
			sumsubVerificationRs.getInspectionId()
		).longValue();
	}

	/**
	 * Get the latest create_time for completed reviews (applicantReviewed webhooks) for a given applicant.
	 * Used for stale webhook detection - if a webhook's createdAtMs is before this timestamp,
	 * it's considered stale and should be ignored.
	 *
	 * @return the latest completed review timestamp, or null if no completed reviews exist
	 */
	public static Timestamp getLatestCompletedReviewTime(Connection conn, String applicantId, String inspectionId)
		throws SQLException {

		String sql = """
			SELECT MAX(create_time) FROM sumsubreviewhistory
			WHERE applicant_id = ? AND inspection_id = ?
			AND type = 'applicantReviewed' AND review_status = 'completed'
			""";

		return DBQueryRunner.getTimeStamp(conn, sql, applicantId, inspectionId);
	}
}
