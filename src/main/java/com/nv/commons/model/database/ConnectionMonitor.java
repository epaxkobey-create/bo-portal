package com.nv.commons.model.database;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.nv.commons.utils.LogUtils;

public class ConnectionMonitor {

	private static final ConnectionMonitor instance = new ConnectionMonitor();
	private ConcurrentHashMap<Long, ConnectionProxy> connectionMap = new ConcurrentHashMap<Long, ConnectionProxy>();
	private AtomicLong atomic = new AtomicLong(0);

	private ConnectionMonitor() {
		super();
		try {
			Thread t = new MonitorThread();
			t.setDaemon(true);
			t.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public final static ConnectionMonitor getInstance() {

		return instance;
	}

	public void addConnection(ConnectionProxy conn) {
		connectionMap.put(conn.getSeq(), conn);
	}

	public void removeConnection(ConnectionProxy conn) {
		connectionMap.remove(conn.getSeq());
	}

	public long getAtomicInteger() {
		return atomic.incrementAndGet();
	}

	public void checkConnection() {
		long ts = System.currentTimeMillis();
		if (connectionMap.size() == 0) {
			return;
		}
		Set<Long> removedConnectionProxy = null;
		for (ConnectionProxy conn : connectionMap.values()) {
			//執行超過五秒顯示
			if (ts - conn.getTimeStamp() > 5000) {
				LogUtils.SYS.error("connection not close over "
									   + (System.currentTimeMillis() - conn.getTimeStamp()) + " ms : " + conn.getSql() + "\n" + conn.getStackTrace());
			}
			//超過一分鐘就不再顯示了
			if (ts - conn.getTimeStamp() > 60000) {
				if (removedConnectionProxy == null) {
					removedConnectionProxy = new HashSet<Long>();
				}
				removedConnectionProxy.add(conn.getSeq());
			}
		}
		if (removedConnectionProxy != null) {
			for (Long seq : removedConnectionProxy) {
				connectionMap.remove(seq);
			}
		}
	}
}

class MonitorThread extends Thread {

	public MonitorThread() {
	}

	public void run() {

		while (true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}

			try {
				ConnectionMonitor.getInstance().checkConnection();
			} catch (Exception ex1) {
				ex1.printStackTrace(System.out);
			}

		}
	}

}