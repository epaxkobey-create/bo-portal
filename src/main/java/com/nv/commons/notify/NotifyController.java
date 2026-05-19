package com.nv.commons.notify;

import java.io.IOException;

import com.nv.commons.cache.RemotingCaller;
import com.nv.commons.utils.LogUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/notifycontroller", "/sessionExtend"})
public class NotifyController extends HttpServlet {

	/**
	 * serialId
	 */
	private static final long serialVersionUID = 182913969434687819L;

	public void init() throws ServletException {
	}

	//接收的部分
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			if (request.getRequestURI().contains("sessionExtend")) {
				LogUtils.SYS.info("Received Session Success : {}", request.getSession(false).getId());
			} else {
				LogUtils.SYS.info("received reload sync");
				RemotingCaller.getInstance().receive(request, response);
			}
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	//接收的部分, 有些資料傳地比較大量, 所以用post
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
