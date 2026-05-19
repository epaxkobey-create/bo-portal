package com.nv.commons.model.database;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnProcessor<T> {

	public T process(Connection conn) throws Exception;

}