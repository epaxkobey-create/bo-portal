package com.nv.commons.model.database;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.fasterxml.jackson.core.JsonGenerator;

@FunctionalInterface
public interface JsonValueProcessor {

	/**
	 * @param index      目前傳進來的是第幾筆
	 * @param rs
	 * @param jGenerator
	 * @throws SQLException
	 * @throws IOException
	 */
	void process(long index, ResultSet rs, JsonGenerator jGenerator) throws SQLException, IOException;
}
