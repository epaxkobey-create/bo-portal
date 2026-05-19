package com.nv.commons.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

public class Props extends Properties {

	/** serialId */
	private static final long serialVersionUID = -5447855636234943486L;

	/**
	 * Constructor
	 */
	public Props(String filename) {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
		if (is == null) {
			String baseDir = System.getProperty("properties.base.dir");
			baseDir = (baseDir == null) ? "" : (baseDir + File.separator);
			baseDir = "";
			String wholePath = baseDir + filename;
			File f = new File(wholePath);
			try {
				is = new FileInputStream(f);
			} catch (FileNotFoundException e) {
				LogUtils.SYS.error("Properties not found : " + wholePath, e);
			}
		}
		try {
			super.load(is);
		} catch (IOException e) {
			LogUtils.SYS.error("Properties load failure", e);
		} finally {
			FileUtils.close(is);
		}
	}



	/**
	 * Returns a <code>java.util.Properties</code> object containing all the
	 * properties of <code>this</code> object starting with the a prefix. <br/>
	 * For example, if this object contains a property whose key is
	 * <code>database.connection.data</code> and the prefix passed is
	 * <code>database</code> then the <code>HKRProperties</code> returned will
	 * contain a key <code>connection.data</code>.<br/>
	 * <br/>
	 * <b>Notes </b>
	 * <ul>
	 * <li>if a resulting key starts with a dot, the char is removed.
	 * <li>if the resulting key, after removing the prefix, is invalid, the
	 * key/value pair is ignored
	 * </ul>
	 * 
	 * @see Properties
	 * @param prefix
	 *            the common prefix of all the properties loaded from this
	 *            object
	 * @return all the subset of properties of this object whose keys start with
	 *         the specified prefix
	 */
	public Properties getProperties(String prefix) {
		Properties props = new Properties();
		if (prefix != null) {
			Enumeration<?> _enum = this.propertyNames();
			while (_enum.hasMoreElements()) {
				String key = (String) _enum.nextElement();
				if (key.startsWith(prefix)) {
					String newKey = key.substring(prefix.length());
					if (newKey.length() > 0) {
						if (newKey.charAt(0) == '.') {
							newKey = newKey.substring(1);
						}
						props.put(newKey, this.getProperty(key));
					}
				}
			}
		}
		return props;
	}
}
