package com.nv.commons.system;

import com.nv.commons.annotation.HttpUpdate;

public class Setting {

	/*
	 * 因為 有 CDN 擋在前面, 所以不用自己做 flood control, 預設關掉
	 */
	/** 阻擋登入跟存提款行為 */
	@HttpUpdate
	public static boolean ENABLE_FLOOD_FILTER = true;

		//JavaScript File Version
	@HttpUpdate
	public static int JS_FILE_VERSION = 0;

	public static String SVN_REVISION = "-";

	@HttpUpdate
	public static int REMOVE_ABANDONED_TIMEOUT_READ = 300;

	@HttpUpdate
	public static int REMOVE_ABANDONED_TIMEOUT_WRITE = 600;

	public static long FILE_VERSION = 0;

	// svn上面保持false，開發人員有需要時在自己環境改成true
	@HttpUpdate
	public static boolean ENABLE_CONNECTION_DEBUG = false;

	public static boolean PRINT_SQL = false;

	public static boolean CREATE_H2_TABLE_FROM_FILE = true;

	public static boolean CHECK_PASSWORD = true;

}
