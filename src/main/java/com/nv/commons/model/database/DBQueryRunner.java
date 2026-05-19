package com.nv.commons.model.database;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.dto.PageResult;
import com.nv.commons.model.PageInfo;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.OracleUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Copy from org.apache.commons.dbutils.QueryRunner Executes SQL queries with
 * pluggable strategies for handling <code>ResultSet</code>s. This class is
 * thread safe.
 *
 * @author Neutec
 */
public class DBQueryRunner {

	public static final SingleValueProcessor<Timestamp> TIMESTAMP_PROCESSOR = (index, rs) -> rs.getTimestamp(1);

	public static final SingleValueProcessor<String> STRING_PROCESSOR = (index, rs) -> rs.getString(1);

	public static final SingleValueProcessor<Number> NUMBER_PROCESSOR = (index, rs) -> (Number) rs.getObject(1);

	public static int[] batch(Connection conn, String sql, Object[][] params) throws SQLException {
		PreparedStatement stmt = null;
		int[] rows = null;
		Object[] cursor = null;
		try {
			stmt = conn.prepareStatement(sql);
			ParameterMetaData pmd = null;
			if (params.length > 0) {
				pmd = stmt.getParameterMetaData();
			}
			for (Object[] param : params) {
				cursor = param;
				fillStatement(stmt, pmd, cursor);
				stmt.addBatch();
			}
			rows = stmt.executeBatch();

		} catch (SQLException e) {
			rethrow(e, sql, cursor);
		} finally {
			DbUtils.close(stmt);
		}

		return rows;
	}

	public static int[] batch(Connection conn, String sql, Collection<Object[]> params) throws SQLException {
		PreparedStatement stmt = null;
		int[] rows = null;
		Object[] cursor = null;
		try {
			stmt = conn.prepareStatement(sql);
			ParameterMetaData pmd = null;
			if (!params.isEmpty()) {
				pmd = stmt.getParameterMetaData();
			}
			for (Object[] args : params) {
				cursor = args;
				fillStatement(stmt, pmd, cursor);
				stmt.addBatch();
			}
			rows = stmt.executeBatch();

		} catch (SQLException e) {
			rethrow(e, sql, cursor);
		} finally {
			DbUtils.close(stmt);
		}

		return rows;
	}

	public static void fillStatement(PreparedStatement stmt, ParameterMetaData pmd, Object... params)
		throws SQLException {
		if (params == null) {
			return;
		}

		if (pmd.getParameterCount() < params.length) {
			throw new SQLException("Too many parameters: expected " + pmd.getParameterCount()
								   + ", was given " + params.length);
		}

		for (int i = 0; i < params.length; i++) {
			if (params[i] != null) {
				Class<?> clazz = params[i].getClass();
				if (clazz.equals(byte[].class)) {
					// 針對byte[] 另外轉換
					byte[] bytes = (byte[]) params[i];
					stmt.setBinaryStream(i + 1, new ByteArrayInputStream(bytes), bytes.length);
				} else {
					stmt.setObject(i + 1, params[i]);
				}
			} else {
				// VARCHAR works with many drivers regardless
				// of the actual column type. Oddly, NULL and
				// OTHER don't work with Oracle's drivers.
				int sqlType = Types.VARCHAR;
				stmt.setNull(i + 1, sqlType);
			}
		}
	}

	public static void fillStatement(PreparedStatement stmt, ParameterMetaData pmd, Collection<Object> params)
		throws SQLException {
		if (params == null) {
			return;
		}
		if (pmd.getParameterCount() < params.size()) {
			throw new SQLException("Too many parameters: expected " + pmd.getParameterCount()
								   + ", was given " + params.size());
		}

		int i = 0;
		for (Object obj : params) {
			if (obj != null) {
				Class<?> clazz = obj.getClass();
				if (clazz.equals(byte[].class)) {
					// 針對byte[] 另外轉換
					byte[] bytes = (byte[]) obj;
					stmt.setBinaryStream(i + 1, new ByteArrayInputStream(bytes), bytes.length);
				} else {
					stmt.setObject(i + 1, obj);
				}
			} else {
				// VARCHAR works with many drivers regardless
				// of the actual column type. Oddly, NULL and
				// OTHER don't work with Oracle's drivers.
				int sqlType = Types.VARCHAR;
				stmt.setNull(i + 1, sqlType);
			}
			i++;
		}
	}

	private static void rethrow(SQLException cause, String sql, Object... params) throws SQLException {


		String causeMessage = cause.getMessage();
		if (causeMessage == null) {
			causeMessage = "";
		}
		StringBuilder msg = new StringBuilder(causeMessage);

		msg.append(" Query: ");
		msg.append(sql);
		msg.append(" Parameters: ");

		if (params == null) {
			msg.append("[]");
		} else {
			msg.append(Arrays.deepToString(params));
		}

		SQLException e = new SQLException(msg.toString(), cause.getSQLState(), cause.getErrorCode());
		e.setNextException(cause);

		throw e;
	}

	// ============== update ==============
	public static int update(Connection conn, String sql) throws SQLException {
		return update(conn, -1, sql, (Object[]) null);
	}

	public static int update(Connection conn, int timeout, String sql) throws SQLException {
		return update(conn, timeout, sql, (Object[]) null);
	}

	public static int update(Connection conn, String sql, Collection<Object> params) throws SQLException {
		return update(conn, -1, sql, params);
	}

	public static int update(Connection conn, String sql, Object... params) throws SQLException {

		// for test case
		if ("junit".equals(System.getProperty(SystemConstants.RUNTIME_ENV))) {
			if (params.length == 1 && params[0].getClass().isArray()) {

				Object[] array = (Object[]) params[0];
				return update(conn, -1, sql, array);
			}
		}
		return update(conn, -1, sql, params);
	}

	public static int update(Connection conn, int timeout, String sql, Collection<Object> params) throws SQLException {
		PreparedStatement stmt = null;
		int rows = 0;
		try {
			stmt = conn.prepareStatement(sql);
			if (params != null && !params.isEmpty()) {
				fillStatement(stmt, stmt.getParameterMetaData(), params);
			}
			if (timeout > 0) {
				stmt.setQueryTimeout(timeout);
			}
			rows = stmt.executeUpdate();
		} catch (SQLException e) {
			rethrow(e, sql, params);
		} finally {
			DbUtils.close(stmt);
		}
		return rows;
	}

	public static int update(Connection conn, int timeout, String sql, Object... params) throws SQLException {
		PreparedStatement stmt = null;
		int rows = 0;
		try {
			stmt = conn.prepareStatement(sql);
			if (params != null && params.length > 0) {
				fillStatement(stmt, stmt.getParameterMetaData(), params);
			}
			if (timeout > 0) {
				stmt.setQueryTimeout(timeout);
			}
			rows = stmt.executeUpdate();
		} catch (SQLException e) {
			rethrow(e, sql, params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			DbUtils.close(stmt);
		}
		return rows;
	}

	// ============== getBeanList ==============
	public static <E> List<E> getBeanList(Connection conn, Class<E> clazz, String sql) throws SQLException {
		return getBeanList(conn, clazz, -1, sql, (Object[]) null);
	}

	public static <E> List<E> getBeanList(Connection conn, Class<E> clazz, int timeout, String sql)
		throws SQLException {
		return getBeanList(conn, clazz, timeout, sql, (Object[]) null);
	}

	public static <E> List<E> getBeanList(Connection conn, Class<E> clazz, String sql, Object... params)
		throws SQLException {
		return getBeanList(conn, clazz, -1, sql, params);
	}

	public static <E> List<E> getBeanList(Connection conn, Class<E> clazz, String sql, Collection<Object> values)
		throws SQLException {
		return getBeanList(conn, clazz, -1, sql, values);
	}

	public static <E> List<E> getBeanList(Connection conn, Class<E> clazz, int timeout, String sql, Object... params)
		throws SQLException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			if (params != null && params.length > 0) {
				ParameterMetaData pmd = stmt.getParameterMetaData();
				fillStatement(stmt, pmd, params);
			}
			if (timeout > 0) {
				stmt.setQueryTimeout(timeout);
			}
			rs = stmt.executeQuery();
			return BeanConverter.toBeanList(rs, clazz);
		} catch (SQLException e) {
			LogUtils.SYS.error(e.getMessage(), e);
			rethrow(e, sql, params);
		} finally {
			DbUtils.closeAll(stmt, rs);
		}
		return null;
	}

	public static LinkedList<Long> getLongList(Connection conn, String sql, Object... params)
		throws SQLException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			if (params != null && params.length > 0) {
				ParameterMetaData pmd = stmt.getParameterMetaData();
				fillStatement(stmt, pmd, params);
			}

			rs = stmt.executeQuery();
			LinkedList<Long> result = new LinkedList<>();
			while (rs.next()) {
				result.add(rs.getLong(1));
			}
			return result;
		} catch (SQLException e) {
			rethrow(e, sql, params);
		} finally {
			DbUtils.closeAll(stmt, rs);
		}
		return null;
	}

	public static <E> List<E> getBeanList(Connection conn, Class<E> clazz, int timeout, String sql,
		Collection<Object> values) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			if (values != null && !values.isEmpty()) {
				ParameterMetaData pmd = stmt.getParameterMetaData();
				fillStatement(stmt, pmd, values);
			}
			if (timeout > 0) {
				stmt.setQueryTimeout(timeout);
			}
			rs = stmt.executeQuery();
			return BeanConverter.toBeanList(rs, clazz);
		} catch (SQLException e) {
			rethrow(e, sql, values);
		} finally {
			DbUtils.closeAll(stmt, rs);
		}
		return null;
	}

	// ============== processBeanResult ==============
	public static <E> void processBeanResult(Connection conn, DataBeanProcessor<E> processor, Class<E> clazz,
		String sql) throws SQLException {
		processBeanResult(conn, processor, clazz, -1, sql, (Object[]) null);
	}

	public static <E> void processBeanResult(Connection conn, DataBeanProcessor<E> processor, Class<E> clazz,
		int timeout, String sql) throws SQLException {
		processBeanResult(conn, processor, clazz, timeout, sql, (Object[]) null);
	}

	public static <E> void processBeanResult(Connection conn, DataBeanProcessor<E> processor, Class<E> clazz,
		String sql, Object... params) throws SQLException {
		processBeanResult(conn, processor, clazz, -1, sql, params);
	}

	public static <E> void processBeanResult(Connection conn, DataBeanProcessor<E> processor, Class<E> clazz,
		String sql, Collection<Object> values) throws SQLException {
		processBeanResult(conn, processor, clazz, -1, sql, values);
	}

	public static <E> void processBeanResult(Connection conn, DataBeanProcessor<E> processor, Class<E> clazz,
		int timeout, String sql, Object... params) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			if (params != null && params.length > 0) {
				fillStatement(stmt, stmt.getParameterMetaData(), params);
			}
			if (timeout > 0) {
				stmt.setQueryTimeout(timeout);
			}
			rs = stmt.executeQuery();
			BeanConverter.processBeanResult(rs, clazz, processor);
		} catch (SQLException e) {
			rethrow(e, sql, params);
		} finally {
			DbUtils.closeAll(stmt, rs);
		}
	}

	public static <E> void processBeanResult(Connection conn, DataBeanProcessor<E> processor, Class<E> clazz,
		int timeout, String sql, Collection<Object> values) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			if (values != null && !values.isEmpty()) {
				fillStatement(stmt, stmt.getParameterMetaData(), values);
			}
			if (timeout > 0) {
				stmt.setQueryTimeout(timeout);
			}
			rs = stmt.executeQuery();
			BeanConverter.processBeanResult(rs, clazz, processor);
		} catch (SQLException e) {
			rethrow(e, sql, values);
		} finally {
			DbUtils.closeAll(stmt, rs);
		}
	}

	// ============== getBeanListByPage ==============
	public static <E> List<E> getBeanListByPage(Connection conn, Class<E> clazz, String sharedSql, String selectSql,
		Collection<Object> values, PageInfo pageInfo) throws SQLException {

		String countSql = "SELECT COUNT(*) " + StringUtils.substringBefore(sharedSql, "ORDER BY");

		pageInfo.setTotalCount(getNumberWithDefault(conn, countSql, BigDecimal.ZERO, values.toArray()).intValue());

		values.add(pageInfo.getLastRowNumber());
		values.add(pageInfo.getFirstRowNumber());

		return DBQueryRunner.getBeanList(conn, clazz, OracleUtils.getCalculatedPageSQL(selectSql + " " + sharedSql),
			values);
	}

	public static <E> List<E> getBeanListByPage(Connection conn, Class<E> clazz, String sharedSql, String selectSql,
		String hint, Collection<Object> values, PageInfo pageInfo) throws SQLException {
		hint = hint == null ? "" : hint.trim();

		String countSql = "SELECT " + hint + " COUNT(*) " + StringUtils.substringBefore(sharedSql, "ORDER BY");

		pageInfo.setTotalCount(getNumberWithDefault(conn, countSql, BigDecimal.ZERO, values.toArray()).intValue());

		values.add(pageInfo.getLastRowNumber());
		values.add(pageInfo.getFirstRowNumber());
		return DBQueryRunner.getBeanList(conn, clazz, OracleUtils.getCalculatedPageSQL(selectSql + " " + sharedSql),
			values);
	}

	public static <E> List<E> getBeanListByPage(Connection conn, Class<E> clazz, String sql, PageInfo pageInfo,
		Collection<Object> values) throws SQLException {
		String segmentSql = sql.toLowerCase();
		//remove select ...
		int start = segmentSql.indexOf(" from ");

		int end = segmentSql.lastIndexOf(" order by ");
		if (end < 0) {
			end = segmentSql.length();
		}

		segmentSql = segmentSql.substring(start, end);

		Number totalCount = DBQueryRunner.getNumber(conn, "SELECT COUNT(1) " + segmentSql, values);

		pageInfo.setTotalCount(totalCount.intValue());

		values.add(pageInfo.getLastRowNumber());
		values.add(pageInfo.getFirstRowNumber());

		return DBQueryRunner.getBeanList(conn, clazz,
			String.format(
				" SELECT * FROM ( SELECT row_.*, rownum rownum_ FROM (%s) row_ WHERE rownum <= ? ) WHERE rownum_ > ? ",
				sql), values);
	}

	public static <E> List<E> getBeanListByPage(Connection conn, Class<E> clazz, String sql, String hint,
		PageInfo pageInfo, Collection<Object> values) throws SQLException {
		String segmentSql = sql.toLowerCase();
		//remove select ...
		int start = segmentSql.indexOf(" from ");

		int end = segmentSql.lastIndexOf(" order by ");
		if (end < 0) {
			end = segmentSql.length();
		}

		segmentSql = segmentSql.substring(start, end);

		Number totalCount = DBQueryRunner.getNumber(conn, "SELECT " + hint + "COUNT(1) " + segmentSql, values);

		pageInfo.setTotalCount(totalCount.intValue());

		values.add(pageInfo.getLastRowNumber());
		values.add(pageInfo.getFirstRowNumber());

		return DBQueryRunner.getBeanList(conn, clazz,
			String.format(
				" SELECT * FROM ( SELECT row_.*, rownum rownum_ FROM (%s) row_ WHERE rownum <= ? ) WHERE rownum_ > ? ",
				sql), values);
	}

	// ============== getBean ==============
	public static <E> E getBean(Connection conn, Class<E> clazz, String sql) throws SQLException {
		return getBean(conn, clazz, sql, (Object[]) null);
	}

	public static <E> E getBean(Connection conn, Class<E> clazz, int timeout, String sql) throws SQLException {
		return getBean(conn, clazz, timeout, sql, (Object[]) null);
	}

	public static <E> E getBean(Connection conn, Class<E> clazz, String sql, Object... params) throws SQLException {
		return getBean(conn, clazz, -1, sql, params);
	}

	public static <E> E getBean(Connection conn, Class<E> clazz, String sql, Collection<Object> values)
		throws SQLException {
		return getBean(conn, clazz, -1, sql, values);
	}

	public static <E> E getBean(Connection conn, Class<E> clazz, int timeout, String sql, Object... params)
		throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			if (params != null && params.length > 0) {
				ParameterMetaData pmd = stmt.getParameterMetaData();
				fillStatement(stmt, pmd, params);
			}
			if (timeout > 0) {
				stmt.setQueryTimeout(timeout);
			}
			rs = stmt.executeQuery();
			if (rs.next()) {
				return BeanConverter.toBean(rs, clazz);
			}
		} catch (SQLException e) {
			rethrow(e, sql, params);
		} finally {
			DbUtils.closeAll(stmt, rs);
		}
		return null;
	}

	public static <E> E getBean(Connection conn, Class<E> clazz, int timeout, String sql, Collection<Object> values)
		throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			if (values != null && !values.isEmpty()) {
				ParameterMetaData pmd = stmt.getParameterMetaData();
				fillStatement(stmt, pmd, values);
			}
			if (timeout > 0) {
				stmt.setQueryTimeout(timeout);
			}
			rs = stmt.executeQuery();
			if (rs.next()) {
				return BeanConverter.toBean(rs, clazz);
			}
		} catch (SQLException e) {
			rethrow(e, sql, values);
		} finally {
			DbUtils.closeAll(stmt, rs);
		}
		return null;
	}

	// ============== getNumberWithDefault ==============
	public static Number getNumberWithDefault(Connection conn, String sql, Number defaultValue) throws SQLException {
		Number number = processSingleValue(conn, NUMBER_PROCESSOR, sql);
		return (number == null ? defaultValue : number);
	}

	public static Number getNumberWithDefault(Connection conn, int timeout, String sql, Number defaultValue)
		throws SQLException {
		Number number = processSingleValue(conn, NUMBER_PROCESSOR, timeout, sql);
		return (number == null ? defaultValue : number);
	}

	public static Number getNumberWithDefault(Connection conn, int timeout, String sql, Number defaultValue,
		Object... params) throws SQLException {
		Number number = processSingleValue(conn, NUMBER_PROCESSOR, timeout, sql, params);
		return (number == null ? defaultValue : number);
	}

	public static Number getNumberWithDefault(Connection conn, String sql, Number defaultValue, Object... params)
		throws SQLException {
		Number number = processSingleValue(conn, NUMBER_PROCESSOR, sql, params);
		return (number == null ? defaultValue : number);
	}

	public static Number getNumberWithDefault(Connection conn, int timeout, String sql, Number defaultValue,
		Collection<Object> values) throws SQLException {
		Number number = processSingleValue(conn, NUMBER_PROCESSOR, timeout, sql, values);
		return (number == null ? defaultValue : number);
	}

	public static Number getNumberWithDefault(Connection conn, String sql, Number defaultValue,
		Collection<Object> values) throws SQLException {
		Number number = processSingleValue(conn, NUMBER_PROCESSOR, sql, values);
		return (number == null ? defaultValue : number);
	}

	// ============== getNumber ==============
	public static Number getNumber(Connection conn, String sql) throws SQLException {
		return getNumber(conn, sql, (Object[]) null);
	}

	public static Number getNumber(Connection conn, int timeout, String sql) throws SQLException {
		return getNumber(conn, timeout, sql, (Object[]) null);
	}

	public static Number getNumber(Connection conn, String sql, Object... params) throws SQLException {
		return getNumber(conn, -1, sql, params);
	}

	public static Number getNumber(Connection conn, int timeout, String sql, Object... params) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			if (params != null && params.length > 0) {
				ParameterMetaData pmd = stmt.getParameterMetaData();
				fillStatement(stmt, pmd, params);
			}
			if (timeout > 0) {
				stmt.setQueryTimeout(timeout);
			}
			rs = stmt.executeQuery();
			if (rs.next()) {
				return (Number) rs.getObject(1);
			}
		} catch (SQLException e) {
			rethrow(e, sql, params);
		} finally {
			DbUtils.closeAll(stmt, rs);
		}
		return BigDecimal.ZERO;
	}

	public static Number getNumber(Connection conn, String sql, Collection<Object> values) throws SQLException {
		return getNumber(conn, -1, sql, values);
	}

	public static Number getNumber(Connection conn, int timeout, String sql, Collection<Object> values)
		throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			if (values != null && !values.isEmpty()) {
				ParameterMetaData pmd = stmt.getParameterMetaData();
				fillStatement(stmt, pmd, values);
			}
			if (timeout > 0) {
				stmt.setQueryTimeout(timeout);
			}
			rs = stmt.executeQuery();
			if (rs.next()) {
				return (Number) rs.getObject(1);
			}
		} catch (SQLException e) {
			rethrow(e, sql, values);
		} finally {
			DbUtils.closeAll(stmt, rs);
		}
		return BigDecimal.ZERO;
	}

	// ============== getString ==============
	public static String getString(Connection conn, String sql) throws SQLException {
		return processSingleValue(conn, STRING_PROCESSOR, sql);
	}

	public static String getString(Connection conn, int timeout, String sql) throws SQLException {
		return processSingleValue(conn, STRING_PROCESSOR, timeout, sql);
	}

	public static String getString(Connection conn, int timeout, String sql, Object... params) throws SQLException {
		return processSingleValue(conn, STRING_PROCESSOR, timeout, sql, params);
	}

	public static String getString(Connection conn, String sql, Object... params) throws SQLException {
		return processSingleValue(conn, STRING_PROCESSOR, sql, params);
	}

	public static String getString(Connection conn, int timeout, String sql, Collection<Object> values)
		throws SQLException {
		return processSingleValue(conn, STRING_PROCESSOR, timeout, sql, values);
	}

	public static String getString(Connection conn, String sql, Collection<Object> values)
		throws SQLException {
		return processSingleValue(conn, STRING_PROCESSOR, sql, values);
	}

	// ============== getTimeStamp ==============
	public static Timestamp getTimeStamp(Connection conn, String sql) throws SQLException {
		return processSingleValue(conn, TIMESTAMP_PROCESSOR, sql);
	}

	public static Timestamp getTimeStamp(Connection conn, int timeout, String sql) throws SQLException {
		return processSingleValue(conn, TIMESTAMP_PROCESSOR, timeout, sql);
	}

	public static Timestamp getTimeStamp(Connection conn, int timeout, String sql, Object... params)
		throws SQLException {
		return processSingleValue(conn, TIMESTAMP_PROCESSOR, timeout, sql, params);
	}

	public static Timestamp getTimeStamp(Connection conn, String sql, Object... params) throws SQLException {
		return processSingleValue(conn, TIMESTAMP_PROCESSOR, sql, params);
	}

	public static Timestamp getTimeStamp(Connection conn, int timeout, String sql, Collection<Object> values)
		throws SQLException {
		return processSingleValue(conn, TIMESTAMP_PROCESSOR, timeout, sql, values);
	}

	public static Timestamp getTimeStamp(Connection conn, String sql, Collection<Object> values) throws SQLException {
		return processSingleValue(conn, TIMESTAMP_PROCESSOR, sql, values);
	}

	// ============== processResultSet ==============
	public static void processResultSet(Connection conn, ResultSetProcessor processor, String sql) throws SQLException {
		processResultSet(conn, processor, -1, sql, (Object[]) null);
	}

	public static void processResultSet(Connection conn, ResultSetProcessor processor, int timeout, String sql)
		throws SQLException {
		processResultSet(conn, processor, timeout, sql, (Object[]) null);
	}

	public static void processResultSet(Connection conn, ResultSetProcessor processor, String sql,
		Collection<Object> values) throws SQLException {
		processResultSet(conn, processor, -1, sql, values);
	}

	public static void processResultSet(Connection conn, ResultSetProcessor processor, String sql, Object... params)
		throws SQLException {
		processResultSet(conn, processor, -1, sql, params);
	}

	public static void processResultSet(Connection conn, ResultSetProcessor processor, int timeout, String sql,
		Collection<Object> values) throws SQLException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			if (values != null && !values.isEmpty()) {
				fillStatement(stmt, stmt.getParameterMetaData(), values);
			}
			if (timeout > 0) {
				stmt.setQueryTimeout(timeout);
			}
			rs = stmt.executeQuery();
			long i = 0;
			while (rs.next()) {
				processor.process(i++, rs);
			}
		} catch (SQLException e) {
			rethrow(e, sql, values);
		} finally {
			DbUtils.close(stmt);
			DbUtils.close(rs);
		}
	}

	public static void processResultSet(Connection conn, ResultSetProcessor processor, int timeout, String sql,
		Object... params) throws SQLException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			if (params != null && params.length > 0) {
				fillStatement(stmt, stmt.getParameterMetaData(), params);
			}
			if (timeout > 0) {
				stmt.setQueryTimeout(timeout);
			}
			rs = stmt.executeQuery();
			long i = 0;
			while (rs.next()) {
				processor.process(i++, rs);
			}
		} catch (SQLException e) {
			rethrow(e, sql, params);
		} finally {
			DbUtils.close(stmt);
			DbUtils.close(rs);
		}
	}

	public static <T> PageResult<T> getPageResult(Connection conn, Class<T> clazz, String sql, long pageNumber,
		long showCount, Collection<Object> parameters) throws Exception {

		String segmentSql = sql.toLowerCase();
		//remove select ...
		int start = segmentSql.indexOf(" from ");

		int end = segmentSql.lastIndexOf(" order by ");
		if (end < 0) {
			end = segmentSql.length();
		}

		segmentSql = segmentSql.substring(start, end);

		Number totalCount = DBQueryRunner.getNumberWithDefault(conn, "SELECT COUNT(1) " + segmentSql, BigDecimal.ZERO,
			parameters);

		if (totalCount == null || totalCount.longValue() == 0) {
			return new PageResult<>();
		}

		if (showCount < 1) {
			showCount = totalCount.longValue();
		}

		long totalPage = totalCount.longValue() / showCount;
		if (totalCount.longValue() % showCount != 0) {
			totalPage = totalPage + 1;
		}

		if (pageNumber < 1) {
			pageNumber = 1;
		} else if (pageNumber > totalPage) {
			pageNumber = totalPage;
		}

		long offset = (pageNumber - 1) * showCount;

		List<Object> objects = new ArrayList<>(parameters);
		objects.add(showCount * pageNumber);
		objects.add(offset);

		List<T> resultList = DBQueryRunner.getBeanList(conn, clazz,
			String.format(
				" SELECT * FROM ( SELECT row_.*, rownum rownum_ FROM (%s) row_ ) WHERE rownum_ <= ? AND rownum_ > ? ",
				sql), objects);

		if (resultList == null || resultList.isEmpty()) {
			return new PageResult<>();
		}

		PageResult<T> pageResult = new PageResult<>();
		pageResult.setResultList(resultList);
		pageResult.setTotalCount(totalCount.longValue());
		pageResult.setShowCount(resultList.size());
		pageResult.setCurrentPage(pageNumber);
		pageResult.setTotalPage(totalPage);

		return pageResult;
	}

	public static <T> PageResult<T> getPageResult(Connection conn, Class<T> clazz, String sql, long pageNumber,
		long showCount, Object... parameters) throws Exception {

		return getPageResult(conn, clazz, sql, pageNumber, showCount, Arrays.asList(parameters));
	}

	// ============== processSingleValue ==============
	public static <T> T processSingleValue(Connection conn, SingleValueProcessor<T> processor, String sql)
		throws SQLException {
		return processSingleValue(conn, processor, -1, sql, (Object[]) null);
	}

	public static <T> T processSingleValue(Connection conn, SingleValueProcessor<T> processor, int timeout, String sql)
		throws SQLException {
		return processSingleValue(conn, processor, timeout, sql, (Object[]) null);
	}

	public static <T> T processSingleValue(Connection conn, SingleValueProcessor<T> processor, String sql,
		Collection<Object> values) throws SQLException {
		return processSingleValue(conn, processor, -1, sql, values);
	}

	public static <T> T processSingleValue(Connection conn, SingleValueProcessor<T> processor, String sql,
		Object... params) throws SQLException {
		return processSingleValue(conn, processor, -1, sql, params);
	}

	public static <T> T processSingleValue(Connection conn, SingleValueProcessor<T> processor, int timeout, String sql,
		Collection<Object> values) throws SQLException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			if (values != null && !values.isEmpty()) {
				fillStatement(stmt, stmt.getParameterMetaData(), values);
			}
			if (timeout > 0) {
				stmt.setQueryTimeout(timeout);
			}
			rs = stmt.executeQuery();
			if (rs.next()) {
				return processor.process(0, rs);
			}
		} catch (SQLException e) {
			rethrow(e, sql, values);
		} finally {
			DbUtils.close(stmt);
			DbUtils.close(rs);
		}
		return null;
	}

	public static <T> T processSingleValue(Connection conn, SingleValueProcessor<T> processor, int timeout, String sql,
		Object... params) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			if (params != null && params.length > 0) {
				fillStatement(stmt, stmt.getParameterMetaData(), params);
			}
			if (timeout > 0) {
				stmt.setQueryTimeout(timeout);
			}
			rs = stmt.executeQuery();
			if (rs.next()) {
				return processor.process(0, rs);
			}
		} catch (SQLException e) {
			rethrow(e, sql, params);
		} finally {
			DbUtils.close(stmt);
			DbUtils.close(rs);
		}
		return null;
	}

	// ============== processJsonStringValue ==============
	public static String processJsonStringValue(Connection conn, JsonValueProcessor processor, String sql)
		throws Exception {
		return processJsonStringValue(conn, processor, -1, sql, (Object[]) null);
	}

	public static String processJsonStringValue(Connection conn, JsonValueProcessor processor, int timeout, String sql)
		throws Exception {
		return processJsonStringValue(conn, processor, timeout, sql, (Object[]) null);
	}

	public static String processJsonStringValue(Connection conn, JsonValueProcessor processor, String sql,
		Collection<Object> values) throws Exception {
		return processJsonStringValue(conn, processor, -1, sql, values);
	}

	public static String processJsonStringValue(Connection conn, JsonValueProcessor processor, String sql,
		Object... params) throws Exception {
		return processJsonStringValue(conn, processor, -1, sql, params);
	}

	public static String processJsonStringValue(Connection conn, JsonValueProcessor processor, int timeout, String sql,
		Object... params) throws Exception {

		StringWriter out = new StringWriter();
		JsonGenerator jGenerator = null;

		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.prepareStatement(sql);
				if (params != null && params.length > 0) {
					fillStatement(stmt, stmt.getParameterMetaData(), params);
				}
				if (timeout > 0) {
					stmt.setQueryTimeout(timeout);
				}
				rs = stmt.executeQuery();
				long i = 0;
				while (rs.next()) {
					processor.process(i++, rs, jGenerator);
				}
			} catch (SQLException e) {
				rethrow(e, sql, params);
			} finally {
				DbUtils.close(stmt);
				DbUtils.close(rs);
			}
			jGenerator.writeEndObject();
		} catch (IOException e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		} finally {
			JSONUtils.close(jGenerator);
		}
		return out.toString();

	}

	public static String processJsonStringValue(Connection conn, JsonValueProcessor processor, int timeout, String sql,
		Collection<Object> values) throws Exception {

		StringWriter out = new StringWriter();
		JsonGenerator jGenerator = null;

		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartObject();
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.prepareStatement(sql);
				if (values != null && !values.isEmpty()) {
					fillStatement(stmt, stmt.getParameterMetaData(), values);
				}
				if (timeout > 0) {
					stmt.setQueryTimeout(timeout);
				}
				rs = stmt.executeQuery();
				long i = 0;
				while (rs.next()) {
					processor.process(i++, rs, jGenerator);
				}
			} catch (SQLException e) {
				rethrow(e, sql, values);
			} finally {
				DbUtils.close(stmt);
				DbUtils.close(rs);
			}
			jGenerator.writeEndObject();
		} catch (IOException e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		} finally {
			JSONUtils.close(jGenerator);
		}
		return out.toString();

	}

	// ============== processJsonArrayValue ==============
	public static String processJsonArrayValue(Connection conn, JsonValueProcessor processor, String sql)
		throws Exception {
		return processJsonArrayValue(conn, processor, -1, sql, (Object[]) null);
	}

	public static String processJsonArrayValue(Connection conn, JsonValueProcessor processor, int timeout, String sql)
		throws Exception {
		return processJsonArrayValue(conn, processor, timeout, sql, (Object[]) null);
	}

	public static String processJsonArrayValue(Connection conn, JsonValueProcessor processor, String sql,
		Collection<Object> values) throws Exception {
		return processJsonArrayValue(conn, processor, -1, sql, values);
	}

	public static String processJsonArrayValue(Connection conn, JsonValueProcessor processor, String sql,
		Object... params) throws Exception {
		return processJsonArrayValue(conn, processor, -1, sql, params);
	}

	public static String processJsonArrayValue(Connection conn, JsonValueProcessor processor, int timeout, String sql,
		Object... params) throws Exception {

		StringWriter out = new StringWriter();
		JsonGenerator jGenerator = null;

		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartArray();
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.prepareStatement(sql);
				if (params != null && params.length > 0) {
					fillStatement(stmt, stmt.getParameterMetaData(), params);
				}
				if (timeout > 0) {
					stmt.setQueryTimeout(timeout);
				}
				rs = stmt.executeQuery();
				long i = 0;
				while (rs.next()) {
					processor.process(i++, rs, jGenerator);
				}
			} catch (SQLException e) {
				rethrow(e, sql, params);
			} finally {
				DbUtils.close(stmt);
				DbUtils.close(rs);
			}
			jGenerator.writeEndArray();
		} catch (IOException e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		} finally {
			JSONUtils.close(jGenerator);
		}
		return out.toString();

	}

	public static String processJsonArrayValue(Connection conn, JsonValueProcessor processor, int timeout, String sql,
		Collection<Object> values) throws Exception {

		StringWriter out = new StringWriter();
		JsonGenerator jGenerator = null;

		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);
			jGenerator.writeStartArray();
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.prepareStatement(sql);
				if (values != null && !values.isEmpty()) {
					fillStatement(stmt, stmt.getParameterMetaData(), values);
				}
				if (timeout > 0) {
					stmt.setQueryTimeout(timeout);
				}
				rs = stmt.executeQuery();
				long i = 0;
				while (rs.next()) {
					processor.process(i++, rs, jGenerator);
				}
			} catch (SQLException e) {
				rethrow(e, sql, values);
			} finally {
				DbUtils.close(stmt);
				DbUtils.close(rs);
			}
			jGenerator.writeEndArray();
		} catch (IOException e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw e;
		} finally {
			JSONUtils.close(jGenerator);
		}
		return out.toString();

	}

	public static Map<String, Object> queryForMap(Connection c, String sql, Object... params) throws SQLException {
		PreparedStatement ps = c.prepareStatement(sql);
		for (int i = 0; i < params.length; i++) {
			ps.setObject(i + 1, params[i]);
		}

		try (ResultSet rs = ps.executeQuery()) {
			if (!rs.next()) {
				return Collections.emptyMap();
			}

			var metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();

			Map<String, Object> row = new LinkedHashMap<>(columnCount);

			for (int i = 1; i <= columnCount; i++) {
				String key = metaData.getColumnLabel(i);
				if (key == null || key.isBlank()) {
					key = metaData.getColumnName(i);
				}
				row.put(key, rs.getObject(i));
			}
			return row;
		}
	}


}
