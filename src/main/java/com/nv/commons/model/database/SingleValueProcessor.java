package com.nv.commons.model.database;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface SingleValueProcessor<T> {

	/**
	 * 
	 * @param index
	 *            目前傳進來的是第幾筆
	 * @param resultSet
	 * @throws SQLException
	 */
	public T process(long index, ResultSet resultSet) throws SQLException;

}