package com.nv.commons.utils;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

/**
 * Oracle 資料庫相關工具類
 *
 * @author SYSTEM
 */
public class OracleUtils {

	public static String getCalculatedPageSQL(String sql) {
		//		return "SELECT * FROM ( SELECT row_.*, rownum rownum_ FROM ( " + sql + " ) row_ WHERE rownum <= ? ) WHERE rownum_ > ? ";
		return "SELECT * FROM ( SELECT row_.*, rownum rownum_ FROM ( " + sql
			+ " ) row_ ) WHERE rownum_ <= ? AND rownum_ > ? ";
	}

	public static String getNamedPageSQL(String sql) {
		//		return "SELECT * FROM ( SELECT row_.*, rownum rownum_ FROM ( " + sql + " ) row_ WHERE rownum <= :lastRowNumber ) WHERE rownum_ > :firstRowNumber ";
		return "SELECT * FROM ( SELECT row_.*, rownum rownum_ FROM ( " + sql
			+ " ) row_ ) WHERE rownum_ <= :lastRowNumber AND rownum_ > :firstRowNumber ";
	}

	/**
	 * 合併 Native SQL 為單一 SQL
	 *
	 * @param nativeSqls
	 * @return
	 */
	public static String mergeNativeSql(List<String> nativeSqls) {

		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN ");
		for (String nativeSql : nativeSqls) {
			if (nativeSql.endsWith(";")) {
				sb.append(nativeSql);
			} else {
				sb.append(nativeSql).append(";");
			}
		}
		sb.append(" END;");

		return sb.toString();

	}

	private OracleUtils() {
		throw new AssertionError();
	}

	public static Connection getNativeConnection(Connection wrapConn) throws SQLException {
		if (wrapConn.isWrapperFor(oracle.jdbc.OracleConnection.class)) {
			return wrapConn.unwrap(oracle.jdbc.OracleConnection.class);
		}
		return wrapConn;

	}

	@SuppressWarnings("PMD.CloseResource")
	public static Object getOracleARRAY(Connection conn, String descriptor, Object elements) throws SQLException {
		Connection nativeConn = OracleUtils.getNativeConnection(conn);
		ArrayDescriptor des = ArrayDescriptor.createDescriptor(descriptor, nativeConn);

		return new ARRAY(des, nativeConn, elements);
	}

	public static byte[] getBlob(Blob blob) throws SQLException {
		if (blob == null) {
			return null;
		}

		try {
			return blob.getBytes(1, (int) blob.length());
		} finally {
			blob.free();
		}

	}

	/**
	 * 群集條件
	 * <p>
	 * DBA suggest:
	 * -- if array size unknown, cardinalitySize =1
	 * -- if knowing array size < 100, cardinalitySize = [array size]
	 *
	 * @param length 若已知查詢條件的size，length為該size；否則傳0
	 * @return String
	 */
	public static String getGroupCondition(int length) {
		return String.format("SELECT /*+ CARDINALITY(T1, %d) */ * FROM TABLE(?) T1",
			(length == 0 || length <= 100) ? 1 : 10000);
	}

	// for unit test only
	public static String getGroupCondition(int length, String descriptor) {
		return getGroupCondition(length);
	}
}
