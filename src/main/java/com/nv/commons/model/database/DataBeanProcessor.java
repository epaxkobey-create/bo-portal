package com.nv.commons.model.database;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface DataBeanProcessor<T> {

	/**
	 * 
	 * @param bean
	 * @throws SQLException
	 */
	public void process(ResultSet resultSet, T bean) throws SQLException;

}