package com.nv.commons.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.nv.commons.constants.HttpMethodType;
import com.nv.commons.exceptions.Deviation;
import com.nv.module.backendapi.controller.ApiRequest;
import com.nv.module.backendapi.controller.BaseServlet;
import com.nv.module.backendapi.controller.JsonRequestWrapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/admin/*"})
public class AdminServlet extends BaseServlet {

	@Override
	protected void process(HttpServletRequest request, HttpServletResponse response, String pathInfo) {
		try {
			var lowerCasePathInfo = pathInfo.toLowerCase();
			if (lowerCasePathInfo.equals("/dbquery")) {
				dbQuery(request, response);
				return;
			}
			if (lowerCasePathInfo.equals("/loglist")) {
				logList(request, response);
				return;
			}
			if (lowerCasePathInfo.equals("/downloadlog")) {
				download(request, response);
				return;
			}

			HttpMethodType methodType = HttpMethodType.valueOf(request.getMethod().toUpperCase());
			ApiRequest apiRequest = new JsonRequestWrapper(request);

			for (HttpMethodExecutor methodExecutor : methodExecutors) {

				if (methodExecutor.isMatch(methodType, pathInfo)) {
					methodExecutor.execute(apiRequest, response);
					return;
				}
			}
			throw new Deviation(pathInfo + " not found");
		} catch (Deviation e) {
			handleException(response, e);
		} catch (Exception e) {
			handleException(response, e);
		}
	}

	public void dbQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher("/page/admin/dbQuery.jsp").forward(request, response);

	}

	public void logList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher("/page/admin/logList.jsp").forward(request, response);
	}

	private static final String FILE_DIR = System.getProperty("catalina.base") + "/logs";

	protected void download(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		String filename = request.getParameter("filename");
		if (filename == null || filename.contains("..")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "非法檔名");
			return;
		}

		File file = new File(FILE_DIR, filename);
		if (!file.exists()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "找不到檔案");
			return;
		}

		response.setContentType(getServletContext().getMimeType(file.getName()));
		response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
		response.setContentLengthLong(file.length());

		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream())) {
			byte[] buffer = new byte[8192];
			int length;
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
		}
	}

}
