package com.nv.commons.utils;

import com.nv.commons.model.database.PoolManager;

public class JUnitUtils {

	private JUnitUtils() {
		throw new AssertionError();
	}

//	public static String getRequestInfo(HttpServletRequest request) {
//		StringBuilder content = new StringBuilder("\n");
//		Map<String, String[]> temp = request.getParameterMap();
//		for (Entry<String, String[]> entry : temp.entrySet()) {
//			String name = entry.getKey();
//			String[] values = entry.getValue(); //request.getParameterValues(name);
//			String par = StringUtils.join(values, "\",\"");
//			content.append("request.getParameterValues(\"" + name + "\", new String[] { \"" + par + "\" } );\n");
//			//content.append("request.setParameter(\"" + name + "\", \"" + value + "\");\n");
//		}
//		return content.toString();
//	}

	private static PoolManager mockPoolManager;

	public static PoolManager getMockPoolManager() {
		return mockPoolManager;
	}

	public static void setMockPoolManager(PoolManager mockPoolManager) {
		JUnitUtils.mockPoolManager = mockPoolManager;
	}
}
