package com.nv.commons.model.database;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.nv.commons.utils.LogUtils;

/**
 * 主要三個功能
 * 1. 顯示執行的SQL
 * 2. 判斷是否有connection沒有close
 * 3. 如果設定autocommit為false，當conn close的時候是否沒有commit
 */
public class ConnectionProxy implements InvocationHandler {

	public static final String[] SET_PARAMETER_METHODS = { "setString", "setInt", "setShort", "setBoolean",
			"setLong", "setFloat", "setDouble", "setBigDecimal", "setDate", "setTime", "setTimestamp", "setClob",
			"setBlob", "setSQLXML", "setNString", "setNCharacterStream", "setNClob", "setByte", "setBytes",
			"setNull", "setAsciiStream", "setBinaryStream", "setObject", "setCharacterStream", "setURL",
			"setRowId" };
	
	private Connection conn;

	private long ts= System.currentTimeMillis();
	
	private long seq = ConnectionMonitor.getInstance().getAtomicInteger();
	
	private String sql;
	
	private String stackTrace;
	
	private boolean autoCommit = true;
	
	private boolean commit = false;
	
	public long getSeq() {
		return seq;
	}
	
	public long getTimeStamp() {
		return ts;
	}
	
	public String getStackTrace() {
		return this.stackTrace;
	}
	
	public ConnectionProxy(Connection value) {
		try {
			throw new Exception("");
		} catch(Exception e) {
			StringWriter trace = new StringWriter();
			e.printStackTrace(new PrintWriter(trace));
			this.stackTrace = trace.toString();
		}
		this.conn = value;
	}
	
	public String getSql() {
		return this.sql;
	}

	@SuppressWarnings("PMD.CloseResource")
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String name = method.getName();
		if ("close".equals(name)) {
			if (!this.autoCommit && !commit) {
				LogUtils.SYS.error("[No Commit] sql : " + getSql() + "\n" + this.stackTrace);
			}
			ConnectionMonitor.getInstance().removeConnection(this);
		} else if ("createStatement".equals(name)) {
			Statement statement = (Statement) method.invoke(conn, args);
			Class<?> clazz = statement.getClass();
			return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), new StatementProxy(statement));
		} else if ("prepareStatement".equals(name)) {
			this.sql = (String) args[0];
			PreparedStatement statement = (PreparedStatement) method.invoke(conn, args);
			Class<?> clazz = statement.getClass();
			return (Statement) Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(),
					new PreparedStatementProxy(statement));
		}
		if ("prepareCall".equals(name) || "nativeSQL".equals(name)) {
			this.sql = (String) args[0];
		}
		if ("setAutoCommit".equals(name)) {
			this.autoCommit = (Boolean) args[0];
		}
		if ("commit".equals(name) || "rollback".equals(name)) {
			this.commit = true;
		}
		return method.invoke(conn, args);
	}

	
	class StatementProxy implements InvocationHandler {

		private Statement statement = null;

		public StatementProxy(Statement obj) {
			if (obj == null) {
				throw new RuntimeException("Statement is null");
			}
			this.statement = obj;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String name = method.getName();
			if ("executeQuery".equals(name) || "executeUpdate".equals(name) || "execute".equals(name)
					|| "addBatch".equals(name)) {
				if (args.length > 0) {
					ConnectionProxy.this.sql = (String) args[0];
				}
				LogUtils.sqlPrint.info(ConnectionProxy.this.sql);
			} else if ("getConnection".equals(name)) {
				return ConnectionProxy.this;
			}
			return method.invoke(statement, args);
		}
	}
	
	class PreparedStatementProxy implements InvocationHandler {

		private PreparedStatement statement = null;
		private Map<Integer, Object> parameterMap = new TreeMap<Integer, Object>();

		private void printSQL() {
			StringBuilder msg = new StringBuilder();

			msg.append(" Query: ");
			msg.append(ConnectionProxy.this.sql);
			msg.append(" Parameters: ");

			if (parameterMap.isEmpty()) {
				msg.append("[]");
			} else {
				Collection<Object> c = parameterMap.values();
				Object[] objs = c.toArray(new Object[0]);
				msg.append(Arrays.deepToString(objs));
			}
			LogUtils.sqlPrint.info(msg.toString());
		}

		public PreparedStatementProxy(PreparedStatement obj) {
			if (obj == null) {
				throw new RuntimeException("PreparedStatement is null");
			}
			this.statement = obj;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String name = method.getName();

			if ("getConnection".equals(name)) {
				return ConnectionProxy.this;
			}

			if ("executeQuery".equals(name) || "executeUpdate".equals(name) || "execute".equals(name)
					|| "addBatch".equals(name)) {
				if (args != null && args.length > 0) {
					ConnectionProxy.this.sql = (String) args[0];
				}
				printSQL();
			} else if (name.startsWith("set")) {
				for (int i = 0; i < SET_PARAMETER_METHODS.length; i++) {
					if (SET_PARAMETER_METHODS[i].equals(name)) {
						this.parameterMap.put((Integer) args[0], args[1]);
					}
				}
			}
			return method.invoke(statement, args);
		}
	}

}
