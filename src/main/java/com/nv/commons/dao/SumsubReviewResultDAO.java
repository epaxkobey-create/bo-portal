package com.nv.commons.dao;

import com.nv.commons.model.database.DBQueryRunner;
import com.nv.module.kyc.sumsub.dto.SumsubVerificationRs;

import java.sql.Connection;
import java.sql.SQLException;

// 這邊要拆成兩個檔案
public class SumsubReviewResultDAO {


	public static int insertReviewResult(Connection conn, long sumsubReviewHistoryId,
		SumsubVerificationRs.ReviewResult reviewResult) throws SQLException {

		String sql = """
			INSERT INTO sumsubreviewresult(sumsub_review_history_id, review_answer, review_reject_type,
			client_comment, moderation_comment, create_time)
			VALUES(?, ?, ?, ?, ?, SYSTIMESTAMP)
			""";

		return DBQueryRunner.update(conn, sql,
			sumsubReviewHistoryId, reviewResult.getReviewAnswer(), reviewResult.getReviewRejectType(),
			reviewResult.getClientComment(), reviewResult.getModerationComment()
		);
	}

}
