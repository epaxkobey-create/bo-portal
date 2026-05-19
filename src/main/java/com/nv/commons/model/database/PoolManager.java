package com.nv.commons.model.database;

import java.sql.Connection;

public interface PoolManager {

	String getReservedConnections();

	Connection get();

	Connection get(int i);

	void closeDataSource();


	int getAvailableNodesSize();

	void checkDBTime();
}
