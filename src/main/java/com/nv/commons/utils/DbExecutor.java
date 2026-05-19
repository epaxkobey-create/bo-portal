package com.nv.commons.utils;

import com.nv.commons.model.database.ConnProcessor;
import com.nv.commons.model.database.DBPool;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
public class DbExecutor {

	private DbExecutor() {
		throw new AssertionError();
	}

	public static <T> T query(ConnProcessor<T> connProcessor) throws Exception {
		return query(connProcessor, null);
	}

	public static <T> T query(ConnProcessor<T> connProcessor, Logger logger) throws Exception {
		Connection conn = null;
		try {
			conn = DBPool.getReadConnection();
			return connProcessor.process(conn);
		} catch (Exception ex) {
			if (logger != null) {
				logger.error(ex.getMessage(), ex);
			}
			throw ex;
		} finally {
			DbUtils.close(conn);
		}
	}

	public static <T> T update(ConnProcessor<T> connProcessor) throws Exception {
		return update(connProcessor, null);
	}

	public static <T> T update(ConnProcessor<T> connProcessor, Logger logger) throws Exception {
		Connection conn = null;
		T result;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);
			result = connProcessor.process(conn);
			conn.commit();
		} catch (Exception ex) {
			DbUtils.rollback(conn);
			if (logger != null && !DbUtils.isLockedException(ex)) {
				logger.error(ex.getMessage(), ex);
			}
			throw ex;
		} finally {
			DbUtils.close(conn);
		}
		return result;
	}
}
