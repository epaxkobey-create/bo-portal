package com.nv.commons.controller;

import java.util.Enumeration;

import com.nv.commons.system.SystemInfo;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.apache.jasper.servlet.JspServlet;

public class JspSecurityServlet extends JspServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void init(ServletConfig config) throws ServletException {
//		if (SystemInfo.getInstance().isProduction()) {
//			super.init(new JspServletConfig(config));
//		} else {
			super.init(config);
//		}
	}
	
	
	private static class JspServletConfig implements ServletConfig {
		
		private ServletConfig config = null;
		
		public JspServletConfig(ServletConfig config) {
			this.config = config;
		}

		@Override
		public String getServletName() {
			return config.getServletName();
		}

		@Override
		public ServletContext getServletContext() {
			return config.getServletContext();
		}

		@Override
		public String getInitParameter(String name) {
			// 如果suppressSmap設定為true，會導致eclipse的jsp沒辦法設定中斷點，所以只有在production才設定為true，其他的不動
			if ("suppressSmap".equals(name)) {
				return "true";
			}
			// 在正式環境, development 必須要是 false, 關掉 jsp recompile
			if ("development".equals(name)) {
				return "false";
			}
			
			// 在正式環境, 必須return false
			if("classdebuginfo".equals(name)){
				return "false";
			}
			return config.getInitParameter(name);
		}

		@Override
		public Enumeration<String> getInitParameterNames() {
			return config.getInitParameterNames();
		}
	}
}

