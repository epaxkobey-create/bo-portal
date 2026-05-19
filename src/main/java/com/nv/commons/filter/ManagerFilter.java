package com.nv.commons.filter;

import java.io.IOException;

import com.nv.commons.bo.ManagerBO;
import com.nv.commons.cache.ManagerCache;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.LoginStatusType;
import com.nv.commons.constants.ManagerStatusType;
import com.nv.commons.constants.SessionKeyConstants;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Manager;
import com.nv.commons.message.LangMessage;
import com.nv.commons.system.SystemInfo;
import com.nv.commons.utils.CountryLookup;
import com.nv.commons.utils.FrontendUtils;
import com.nv.commons.utils.HostAddressUtils;
import com.nv.commons.utils.HttpUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.RequestParser;
import com.nv.commons.utils.ResponseUtils;
import com.nv.commons.utils.ServerInfoUtils;
import com.nv.commons.utils.ThreadLocalUtils;
import com.nv.commons.utils.WebSiteTypeUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * @author Neutec
 */
//@WebFilter(urlPatterns = {"/manager/*", "/page/manager/*"}, dispatcherTypes={ DispatcherType.REQUEST })
public class ManagerFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	private int checkStatus(HttpSession session, WebSiteType webSiteType)
		throws Exception {

		if (session == null) {
			return LoginStatusType.NOT_LOGIN.unique();
		}
		Manager managerInSession = (Manager) session.getAttribute(SessionKeyConstants.ManagerRole);

		if (null == managerInSession) {
			return LoginStatusType.SESSION_TIMEOUT.unique();
		}
		//ManagerCache需執行update的機制, 否則無法取得最新的狀態.
		Manager managerInCache = ManagerCache.getInstance().getManager(webSiteType, managerInSession.getUserId());

		//目前的沒有, 判斷是否另一個使用者從別的server登入, 或者是狀態改變(被Close)
		if (managerInCache == null) {
			Manager managerInDB = ManagerBO.queryManagerByManagerID(webSiteType, managerInSession.getUserId());
			if (managerInDB == null) {
				return LoginStatusType.NOT_LOGIN.unique();
			}
			// 是否登入到別的server
			if (!managerInSession.getServerID().equals(managerInDB.getServerID())) {
				return LoginStatusType.MULTIPLE_LOGIN.unique();
			}
			// 帳號是否被INACTIVE
			if (ManagerStatusType.INACTIVE == ManagerStatusType.getInstanceOf(managerInDB.getStatus())) {
				return LoginStatusType.CLOSED.unique();
			}

			// 這裡是判斷當另一台server掛掉後，session搬來其他台，要能讓他work
			// 更新Account的ServerID資料
			if (managerInDB.getServerID() != null) {
				try {

					String systemServerID = SystemInfo.getInstance().getServerID();
					managerInSession.setServerID(systemServerID);

					// 新增 cache 資料
					ManagerCache.getInstance().add(managerInSession.getUserId(), managerInSession);

					// 更新 Manager table 的 ServerID 資料
					ManagerBO.updateServerID(systemServerID, managerInSession.getWebsiteType(),
						managerInSession.getUserId());

					return LoginStatusType.CORRECT.unique();
				} catch (Exception e) {
					LogUtils.SYS.error(e.getMessage(), e);
				}
			}
			// 這邊正常應該不會執行到, 但為了預防還是寫防護
			return LoginStatusType.SESSION_TIMEOUT.unique();
		}
		// 同一台server, 重複登入, 判斷cache內user的session id跟自己的session是否相同
		if (!session.getId().equals(managerInCache.getSessionID())) {
			//這樣做才可以把前一個user的session清掉，但不更新db與cache給第二個user用
			return LoginStatusType.MULTIPLE_LOGIN.unique();
		}

		return LoginStatusType.CORRECT.unique();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {

		if (HttpUtils.isPreCompileRequest(request)) {
			chain.doFilter(request, response);
			return;
		}

		HttpSession session = ((HttpServletRequest) request).getSession(false);
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		if (!ServerInfoUtils.isManagerServer()) {
			LogUtils.filter.warn("Request tried to access Manager content on non-Manager server. ServerID: {}, RequestURI: {}, RequestIP: {}",
				SystemInfo.getInstance().getServerID(),
				((HttpServletRequest) request).getRequestURI(),
				request.getRemoteAddr());
			httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String serverName = httpRequest.getServerName();

		WebSiteType webSiteType = WebSiteTypeUtils.getWebSiteByBoDomain(serverName);

		if (webSiteType == null) {
			LogUtils.filter.warn("Server name {} does not map to any WebSiteType for Manager access.", serverName);
			httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		if (SystemConstants.MANAGER_LOGIN_PAGE.equals(httpRequest.getRequestURI())) {
			chain.doFilter(request, response);
			return;
		}

		try {

			int status = FrontendUtils.needShowBoMaintain() ?
				LoginStatusType.KICK_OUT.unique() :
				checkStatus(session, webSiteType);

			if (status != 0) {
				if (status == LoginStatusType.NOT_LOGIN.unique()) {
					httpResponse.sendRedirect(SystemConstants.MANAGER_LOGIN_PAGE);
					return;
				}

				//AJAX - Terminate request, 交由前端catch error, 進行頁面Refresh, 導向autoLogout.jsp
				if (FrontendUtils.isAjaxRequest(httpRequest)) {
					httpResponse.setStatus(HttpServletResponse.SC_GONE);
					httpResponse.addHeader("refreshPage", "true");
					LangMessage langMessage = FrontendUtils
						.getLangMessage(session, httpRequest);
					ResponseUtils.sendJsonResponse(httpResponse,
						JSONUtils.getJSONString("error", langMessage.get("msg.login.loginStatusType." + status)));
					return;
				}
				request.setAttribute("roleType", SessionKeyConstants.ManagerRole);
				request.setAttribute("accountFilterStatus", String.valueOf(status));
				//在web.xml已設定ManagerFilter不對forward做filter, 所以不會造成無窮回圈filter
				request.getRequestDispatcher(SystemConstants.AUTO_LOGOUT_PAGE).forward(request, response);
			} else {
				if ("/manager".equals(httpRequest.getRequestURI())) {
					httpResponse.sendRedirect(FrontendUtils.getBackOffice());
					return;
				}

				/*
				 */
				ThreadLocalUtils.set(webSiteType);


				Object currencyObject = session.getAttribute("currency");
				int currencyTypeId =-1;
				if(currencyObject != null){
					currencyTypeId = (Integer) currencyObject;
				}
//				int currencyTypeId = 	RequestParser.getIntParameter(request, 2, "currencyTypeId", -1);

				if (currencyTypeId > -1) {

					CurrencyType currencyType = CurrencyType.getInstance(currencyTypeId);
					ThreadLocalUtils.set(currencyType);
				}

				chain.doFilter(request, response);
			}

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
			request.getRequestDispatcher(FrontendUtils.getExecuteFailPath()).forward(request, response);
		}
	}

	@Override
	public void destroy() {

	}
}
