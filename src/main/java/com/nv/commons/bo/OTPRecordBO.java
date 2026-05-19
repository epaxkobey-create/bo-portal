package com.nv.commons.bo;

import java.sql.Connection;

import com.nv.commons.cache.OTPRecordCache;
import com.nv.commons.dao.OTPRecordDAO;
import com.nv.commons.dto.OTPRecord;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.LogUtils;

public class OTPRecordBO {

	public static void insert(OTPRecord otpRecord) throws Exception {
		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			OTPRecordDAO.insert(conn, otpRecord);

			conn.commit();

			OTPRecordCache.getInstance().update();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.close(conn);
		}
	}

}