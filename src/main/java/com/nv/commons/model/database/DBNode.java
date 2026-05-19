package com.nv.commons.model.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nv.commons.utils.DbUtils;
import org.apache.tomcat.jdbc.pool.DataSource;

public class DBNode {

	private int id;
	private int nextId = 0;

	private Map<String, String> properties = null;
	private String name;

	private volatile boolean enabled = false;
	private volatile boolean hidden = false;
	private volatile DataSource ds;
	private volatile DataSource dsMonitor;
	private ExecutorService mainES = Executors.newVirtualThreadPerTaskExecutor();

	private ExecutorService healthMonitorES = Executors.newVirtualThreadPerTaskExecutor();

	private AtomicInteger failCount = new AtomicInteger(0);
	private final static int SUCCESS = 0;
	private final static int ERROR = 1;
	private volatile boolean healthMonitor = true;

	private final static long RETRY_MILLISECOND = 60000;

	private static final Logger logger = Logger.getLogger(DBNode.class.getName());

	protected DBNode(int id, Map<String, String> properties) {
		this.id = id;
		this.properties = properties;
		this.name = properties.get("host") + " - jdbc/db : " + id;
		this.hidden = Boolean.parseBoolean(properties.get("hidden"));
	}

	/**
	 * monitor 測通以後才會初始化connection pool
	 * @throws Exception
	 */
	protected void startMonitor() {
		// 初始化監控的data source
		DBNode.this.initMonitorDataSource();

		Runnable r = () -> {
			while (healthMonitor) {
				try {
					// timeout or exception
					boolean monitorExp = false;
					// DBA是否手動關閉這個node
					boolean DBAExp = false;

					if (DBNode.this.dsMonitor == null) {
						// 可能之前初始化失敗
						DBNode.this.initMonitorDataSource();
						logger.info("monitor is null, init conn test " + DBNode.this.getName());
						monitorExp = true;
					} else {
						try {
							FutureTask<Integer> task = new FutureTask<Integer>(
								new HealthMonitorCallable(DBNode.this.getId()));

							healthMonitorES.submit(task);
							try {
								int taskStatus = task.get(4L, TimeUnit.SECONDS);
								if (taskStatus == DBNode.ERROR) {
									DBAExp = true;
								} else if (taskStatus == DBNode.SUCCESS && !DBNode.this.isEnabled()) {
									DBNode.this.initDataSource();
								}
							} catch (Exception ex2) {
								// 發生timeout錯誤
								logger.log(Level.FINE, ex2.toString(), ex2);
								monitorExp = true;
								task.cancel(true);
							}
						} catch (Exception ex) {
							// 發生例外
							logger.log(Level.FINE, ex.toString(), ex);
							monitorExp = true;
						}
					}

					if (monitorExp) {
						failCount.incrementAndGet();
						logger.warning(getGMTDatetime() + " - " + DBNode.this.getName()
							+ " -- Exception detected db node TIMEOUT 4sec FAIL_COUNT ["
							+ DBNode.this.failCount.get() + "]");
						// 執行超過五次，關閉node
						if (DBNode.this.failCount.get() >= 5) {
							if (DBNode.this.isEnabled()) {
								logger.warning("disabling node : " + DBNode.this.getName());
								DBNode.this.setEnabled(false);
							}
							logger.warning(getGMTDatetime() + " - " + DBNode.this.getName()
								+ " -- Exception ASSUME db node DOWN. FAIL_COUNT ["
								+ DBNode.this.failCount + "]. Retrying every "
								+ (RETRY_MILLISECOND / 1000) + " seconds.");
							Thread.sleep(RETRY_MILLISECOND);
							DBNode.this.initMonitorDataSource();
						}
					} else if (DBAExp) {
						if (DBNode.this.isEnabled()) {
							logger.warning("disabling node : " + DBNode.this.getName());
							DBNode.this.setEnabled(false);
						}
						logger.warning(getGMTDatetime() + " - " + DBNode.this.getName()
							+ " -- Exception ASSUME db node DOWN. Error infomation from table available_nodes. Retrying every "
							+ (RETRY_MILLISECOND / 1000) + " seconds.");
						Thread.sleep(RETRY_MILLISECOND);
						DBNode.this.initMonitorDataSource();
					}
				} catch (Exception ex1) {
					logger.log(Level.FINE, ex1.toString(), ex1);
				}

				try {
					Thread.sleep(2000L);
				} catch (InterruptedException e) {
					logger.log(Level.FINE, e.toString(), e);
				}
			}
		};
		mainES.submit(r);
	}


	private void initDataSource() throws Exception {
		setEnabled(false);

		DataSource dataSource = createDataSource();

		Connection conn = null;
		// 測試連線
		boolean success = false;
		try {
			conn = dataSource.getConnection();
			if (conn != null) {
				success = true;
			}
		} finally {
			// 測試 conn.close() 是否會丟 exception
			DbUtils.close(conn);
		}

		if (success) {
			DataSource temp = this.ds;
			this.ds = dataSource;
			this.failCount.set(0);
			this.setEnabled(true);
			String info = (temp == null ? "initalize" : "reinitalize");
			logger.info(getGMTDatetime() + " - " + getName() + " - " + info + " dataSource successfully");
			if (temp != null) {
				temp.close(true);
				temp = null;
			}
		}
	}

	public DataSource getDataSource() {
		return this.ds;
	}

	public int getActiveCount() {
		return this.ds.getNumActive();
	}

	public int getIdleCount() {
		return this.ds.getNumIdle();
	}

//	private void setDataSourceInfo(DataSource datasource) {
//
////		String url = "jdbc:mysql://" + properties.get("host") + ":" + properties.get("port") + "/" + properties.get("database");
//		String url = "jdbc:oracle:thin:@ (DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)(HOST = " + p.getProperty(
//			new StringBuilder().append("host").append(this.id).toString())
//			+ ")(PORT = " + p.getProperty(new StringBuilder().append("port").append(index).toString())
//			+ ")) (CONNECT_DATA=(SERVER = DEDICATED)(SERVICE_NAME = "
//			+ p.getProperty(new StringBuilder().append("serviceName").append(index).toString()) + ")(INSTANCE_NAME = "
//			+ p.getProperty(new StringBuilder().append("instanceName").append(index).toString()) + ")))";
//
//		datasource.setUrl(url);
//		datasource.setUsername(properties.get("user"));
//		datasource.setPassword(properties.get("pass"));
//		datasource.setDriverClassName("com.mysql.cj.jdbc.Driver");
//	}


	private DataSource createDataSource() {
		DBPoolProperties poolProperties = new DBPoolProperties();

		DataSource datasource = new DataSource(poolProperties);

		String url = "jdbc:oracle:thin:@ (DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)" //
			+ "(HOST = " + properties.get("host") + ")" //
			+ "(PORT = " + properties.get("port") + ")) " //
			+ "(CONNECT_DATA=(SERVER = DEDICATED)(SERVICE_NAME = " + properties.get("serviceName") + ")" //
			+ "(INSTANCE_NAME = " + properties.get("instanceName") + ")))";

		datasource.setUrl(url);
		datasource.setName("DBConnector \t" + properties.get("host") + " - jdbc/db");
		datasource.setUsername(properties.get("user"));
		datasource.setPassword(properties.get("pass"));
		datasource.setDriverClassName("oracle.jdbc.OracleDriver");

		datasource.setDefaultAutoCommit(false);

		// for init
		datasource.setInitialSize(Integer.parseInt(properties.get("initSize")));
		datasource.setMaxActive(Integer.parseInt(properties.get("maxActive")));

		// start PoolCleaner
		datasource.setTimeBetweenEvictionRunsMillis(60 * 1000);

		// PoolCleaner Task1
		datasource.setRemoveAbandoned(Boolean.parseBoolean(properties.get("removeAbandoned")));
		datasource.setRemoveAbandonedTimeout(Integer.parseInt(properties.get("removeAbandonedTimeout")));

		// PoolCleaner Task2
		datasource.setMinIdle(Integer.parseInt(properties.get("minIdle")));
		datasource.setMinEvictableIdleTimeMillis(-1);

		// PoolCleaner Task3
		datasource.setTestWhileIdle(false);

		// for borrowConnection
		datasource.setMaxWait(Integer.parseInt(properties.get("maxWait")));

		// for returnConnection
		datasource.setTestOnReturn(false);

		// for borrowConnection & returnConnection
		datasource.setLogAbandoned(Boolean.parseBoolean(properties.get("logAbandoned")));

		String interceptors = properties.get("JdbcInterceptors");
		if (interceptors != null && interceptors.length() > 0) {
			datasource.setJdbcInterceptors(interceptors);
		}

		return datasource;
	}

	private void initMonitorDataSource() {

		if (this.dsMonitor != null) {
			this.dsMonitor.close(true);
			this.dsMonitor = null;
		}

		try {
			DataSource testDataSource = createMonitorDataSource();
			Connection testConn = null;
			boolean testMonitorSuccess = false;
			try {
				testConn = testDataSource.getConnection();
				if (testConn != null) {
					testMonitorSuccess = true;
				}
			} finally {
				DbUtils.close(testConn);
			}

			// 測試成功
			if (testMonitorSuccess) {
				this.dsMonitor = testDataSource;
				logger.info(getGMTDatetime() + " - " + getName() + " - health monitor init successfully");
			}
		} catch (Exception ex) {
			logger.log(Level.INFO, "error occured while trying to init monitor datasource " + getName(), ex);
		}
	}

	private DataSource createMonitorDataSource() {

		DataSource dsMonitor = new DataSource();

		String url = "jdbc:oracle:thin:@ (DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)" //
			+ "(HOST = " + properties.get("host") + ")" //
			+ "(PORT = " + properties.get("port") + ")) " //
			+ "(CONNECT_DATA=(SERVER = DEDICATED)(SERVICE_NAME = " + properties.get("serviceName") + ")" //
			+ "(INSTANCE_NAME = " + properties.get("instanceName") + ")))";

		dsMonitor.setUrl(url);
		dsMonitor.setName("HealthMonitor Pool \t" + properties.get("host") + " - jdbc/db");
		dsMonitor.setUsername(properties.get("user"));
		dsMonitor.setPassword(properties.get("pass"));
		dsMonitor.setDriverClassName("oracle.jdbc.OracleDriver");

		// for init
		dsMonitor.setInitialSize(3);
		dsMonitor.setMaxActive(3);

		// start PoolCleaner
		dsMonitor.setTimeBetweenEvictionRunsMillis(60 * 1000);

		// PoolCleaner Task1
		dsMonitor.setRemoveAbandoned(true);
		dsMonitor.setRemoveAbandonedTimeout(10);

		// PoolCleaner Task2
		dsMonitor.setMinIdle(3);
		dsMonitor.setMinEvictableIdleTimeMillis(-1);

		// PoolCleaner Task3
		dsMonitor.setTestWhileIdle(false);

		// for borrowConnection
		dsMonitor.setMaxWait(5000);
		dsMonitor.setTestOnBorrow(false);

		// for returnConnection
		dsMonitor.setTestOnReturn(false);

		// for borrowConnection & returnConnection
		dsMonitor.setLogAbandoned(false);


		return dsMonitor;
	}

	public int getId() {
		return id;
	}

	public void setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			logger.warning(getName() + " Enabled: " + this.enabled + " -> " + enabled);
		}

		this.enabled = enabled;
		if (!enabled && this.ds != null) {
			this.ds.purge();
		}

	}

	public String getName() {
		return this.name;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void close() {
		if (this.ds == null) {
			return;
		}
		try {
			// 停止監控
			healthMonitor = false;
			mainES.shutdown();
			healthMonitorES.shutdown();

			setEnabled(false);
			this.ds.close();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}


	public Connection getConnection() throws SQLException {
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("Using DataSource: " + this.name + " " + "active : " + this.ds.getActive() + " idle : " + this.ds.getIdle() + " properties : " + this.ds);
		}
		return this.ds.getConnection();
	}

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private String getGMTDatetime() {
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		String date = sdf.format(new Date(System.currentTimeMillis()));
		return date + " GMT+8 ";
	}

	public int getNextId() {
		return nextId;
	}

	public void setNextId(int nextId) {
		this.nextId = nextId;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean value) {
		this.hidden = value;
	}

	public int countReservedConnections() {
		return (this.ds.getMaxActive() - this.ds.getActive());
	}

	class HealthMonitorCallable implements Callable<Integer> {
		private int nodeID = 0;

		HealthMonitorCallable(int nodeID) {
			this.nodeID = nodeID;
		}

		@Override
		public Integer call() throws Exception {
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try {
				conn = DBNode.this.dsMonitor.getConnection();
				pstmt = conn.prepareStatement("select status from available_nodes where id = ? ");
				// 這邊DB是從1開始
				pstmt.setInt(1, this.nodeID + 1);
				rs = pstmt.executeQuery();
				int status = -1;
				if (rs.next()) {
					status = rs.getInt("status");
				}
				if (status == DBNode.SUCCESS) {
					DBNode.this.failCount.set(0);
					return DBNode.SUCCESS;
				} else {
					return DBNode.ERROR;
				}
			} catch (SQLException e) {
				if (e.getErrorCode() == 1146 || e.getMessage().contains("doesn't exist")) {

					logger.log(Level.INFO, "available_nodes table does not exists !!", e);
				}
				throw e;
			} finally {
				DbUtils.closeAll(conn, pstmt, rs);
			}

		}

	}
}

