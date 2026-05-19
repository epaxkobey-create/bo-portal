package com.nv.commons.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 分析處理帶命名參數的SQL語句。使用Map存儲參數，然後將參數替換成? 如果有效能上的考量, 請避免使用 這邊並沒有實作全部的method, 如有需要請自行增加
 */
public class NamedQuery {

	private String sql = null;

	private List<String> names = null;
	private List<Object> values = null;

	public NamedQuery() {

	}


	public String getNativeSQL() {
		return this.sql;
	}

	public Object[] getParameterArray() {
		return this.values.toArray();
	}

	/**
	 * 分析處理帶命名參數的SQL語句。使用Map存儲參數，然後將參數替換成?
	 *
	 * @param namedSql
	 * @return
	 * @throws SQLException
	 */
	public String parseSql(String namedSql) {
		String regex = "(:(\\w+))";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(namedSql);

		this.names = new ArrayList<>();
		this.values = new ArrayList<>();

		StringBuffer sb = new StringBuffer();

		while (m.find()) {
			//參數名稱可能有重複，使用序號來做Key
			names.add(m.group(2));
			values.add(null);
			m.appendReplacement(sb, "?");
		}
		m.appendTail(sb);

		this.sql = sb.toString();

		return this.sql;
	}

	public void setObject(String name, Object value) {
		if (this.names == null) {
			throw new RuntimeException("parameters is null");
		}
		for (int i = 0; i < this.names.size(); i++) {
			if (this.names.get(i).equals(name)) {
				this.values.set(i, value);
			}
		}
	}


}
