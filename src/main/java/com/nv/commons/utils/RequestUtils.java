package com.nv.commons.utils;

import java.util.Enumeration;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;

public class RequestUtils {

	public static String getPathInfo(HttpServletRequest request) {
		String pathInfo = request.getPathInfo();
		int questionMark = pathInfo.indexOf("?");

		if (questionMark > -1) {
			pathInfo = pathInfo.substring(0, questionMark);
		}

		return pathInfo;
	}

	public static void printRequestContent(HttpServletRequest req){
		printRequestContent(req, (Logger) LogUtils.SYS);
	}

	public static void printRequestContent(HttpServletRequest req, Logger logger){
		Enumeration<String> headerNames = req.getHeaderNames();
		while(headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			logger.info("Header Name - " + headerName + ", Value - " + req.getHeader(headerName));
		}
		Enumeration<String> params = req.getParameterNames();
		while(params.hasMoreElements()){
			String paramName = params.nextElement();
			logger.info("Parameter Name - "+paramName+", Value - "+req.getParameter(paramName));
		}
	}

}
