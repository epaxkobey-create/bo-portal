package com.nv.commons.model.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nv.commons.system.Setting;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ResourceUtils;
import org.apache.commons.io.IOUtils;

public class OraclePoolManager implements PoolManager {

	private int totalNodeCount;

	// 全部的nodes
	private DBNode[] nodes = null;

	// 目前指向的node
	private DBNode currentNode = null;

	private static final Logger logger = Logger.getLogger(PoolManager.class.getName());

	private DBProperty dbInfo = null;

	private String name = null;

	public OraclePoolManager(String name, String file) throws SQLException {
		this.name = name;

		try (InputStream in = ResourceUtils.getResourceAsStream(file)) {

			String json = IOUtils.toString(in, StandardCharsets.UTF_8);

			this.dbInfo = JSONUtils.jsonToObject(json, DBProperty.class);

			LogUtils.SYS.debug("DBPool-" + name + " : " + this.dbInfo.getNodes().get(0).get("database"));

			Map<String, String> defaultSettings = this.dbInfo.getDefaultSettings();
			List<Map<String, String>> nodes = this.dbInfo.getNodes();

			for (Map<String, String> properties : nodes) {
				for (Map.Entry<String, String> entry : defaultSettings.entrySet()) {
					// 如果沒有特殊指定則放入預設值
					if (!properties.containsKey(entry.getKey())) {
						properties.put(entry.getKey(), entry.getValue());
					}
				}
			}

			initDB();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	private void initDB() throws SQLException {
		this.totalNodeCount = this.dbInfo.getNodes().size();
		this.nodes = new DBNode[this.totalNodeCount];

		List<Map<String, String>> nodes = this.dbInfo.getNodes();

		for (int i = 0; i < this.totalNodeCount; i++) {
			Map<String, String> map = nodes.get(i);
			try {
				DBNode node = new DBNode(i, map);
				this.nodes[i] = node;
				// 這邊設定為陣列的索引值
				node.setNextId(i + 1 >= this.totalNodeCount ? 0 : i + 1);
				node.startMonitor();
			} catch (Exception ex) {
				logger.log(Level.SEVERE,
					"Error trying to initDB , datasource url : " + map.get("host"), ex);
			}
		}

		// 把當前的指向第一個
		this.currentNode = this.nodes[0];
	}

	public void setReservedNode(int id) {
		this.nodes[id].setHidden(true);
	}

	public int getAvailableNodesSize() {
		int count = 0;
		for (int i = 0; i < this.totalNodeCount; i++) {
			if (this.nodes[i].isEnabled()) {
				count++;
			}
		}
		return count;
	}

	@Override
	public String getReservedConnections() {
		StringWriter out = new StringWriter();
		JsonGenerator jGenerator = null;

		try {
			jGenerator = JSONUtils.getFactory().createGenerator(out);

			jGenerator.writeStartObject();
			for (int i = 0; i < this.totalNodeCount; i++) {
				if (this.nodes[i].isEnabled()) {
					jGenerator.writeNumberField("node" + (i + 1), this.nodes[i].countReservedConnections());
				} else {
					jGenerator.writeNumberField("node" + (i + 1), -1);
				}
			}
			jGenerator.writeEndObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			JSONUtils.close(jGenerator);
		}
		return out.toString();
	}

	@Override
	public Connection get() {
		return get(-1);
	}

	private DBNode findPublicNode() {
		DBNode targetNode = null;
		DBNode hiddenNode = null;
		for (int i = 0; i < this.totalNodeCount; i++) {
			int id = this.currentNode.getNextId();
			this.currentNode = this.nodes[id];
			if (currentNode.isEnabled()) {
				if(currentNode.isHidden()) {
					// 找到hidden node存起來備用
					if(hiddenNode == null) {
						hiddenNode = this.currentNode;
					}
				} else {
					// 找到可用且公開的node，可以直接使用
					targetNode = this.currentNode;
					break;
				}
			}
		}
		// 都找不到可用且公開的node，嘗試使用隱藏的node
		if(targetNode == null && hiddenNode != null) {
			targetNode = hiddenNode;
			this.currentNode = hiddenNode;
		}
		return targetNode;
	}

	// 表示有指定，指定不管公開或是隱藏的node
	private DBNode findNode(int nodeId) {
		DBNode targetNode = this.nodes[nodeId];
		// 指定的可能出問題了，往下找
		if (!targetNode.isEnabled()) {
			for (int i = 0; i < this.totalNodeCount; i++) {
				int id = targetNode.getNextId();
				targetNode = this.nodes[id];
				if (targetNode.isEnabled()) {
					break;
				}
			}
		}
		return targetNode;
	}

	private synchronized DBNode findAvailableNode(int nodeId) {
		// 異常範圍則修正成 -1
		if (nodeId >= this.totalNodeCount) {
			nodeId = -1;
		}
		if (nodeId < 0) {
			// 表示不指定，找非隱藏的node
			return findPublicNode();
		} else {
			return findNode(nodeId);
		}
	}

	public Connection get(int nodeId) {
		if (this.totalNodeCount == 0) {
			logger.info("Exception : No " + this.name + " servers left to remove. Size 0.");
			return null;
		}

		try {
			DBNode node = findAvailableNode(nodeId);
			if (node == null || !node.isEnabled()) {
				throw new Exception("Exception : No " + this.name + " servers left to remove. Invalid Node.");
			}
			Connection conn = node.getConnection();
			if (Setting.ENABLE_CONNECTION_DEBUG) {
				Class<?> clazz = conn.getClass();
				ConnectionProxy connectionProxy = new ConnectionProxy(conn);
				//Connection connection = (Connection) Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), connectionProxy);
				Connection connection = (Connection) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { Connection.class }, connectionProxy);
				ConnectionMonitor.getInstance().addConnection(connectionProxy);
				return connection;
			} else {
				return conn;
			}
		} catch (Exception ex) {
			logger.info("Exception occurred while trying to get " + this.name + " connection");
			ex.printStackTrace();
		}

		try {
			//如果都沒有，停個一秒繼續找
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			logger.log(Level.FINE, e.toString(), e);
		}
		return get(nodeId);
	}

	@Override
	public void closeDataSource() {
		for (int i = 0; i < this.totalNodeCount; i++) {
			nodes[i].close();
		}
	}

	public void checkDBTime() {
		if(!this.dbInfo.isCheckDBTime()) {
			return;
		}
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		Timestamp ts = null;

		try {
			conn = get();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT SYSTIMESTAMP FROM DUAL");
			while (rs.next()) {
				ts = rs.getTimestamp(1);
			}

			// 跟DB的時間比較，誤差不應該超過一分鐘
			if (Math.abs(ts.getTime() - System.currentTimeMillis()) > 60_000L) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S [z]");
				String localtimeStr = sdf.format(new Date());
				LogUtils.SYS.error(String.format("Different DBTime %s with ClientTime %s", ts.toString(), localtimeStr));
			}

		} catch (Exception e) {
			LogUtils.SYS.error("check db time error ", e);
		} finally {
			DbUtils.closeAll(conn, stmt, rs);
		}
	}
}
