package com.maiquan.aladdin_mall.util;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.maiquan.aladingfront.Principal;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Web工具
 * 
 * @author JSC
 *
 */
public class WebUtil extends HttpServlet {
	/** 在session中登陆前请求地址的key */
	public static final String SAVE_REQUEST_KEY = "saveRequesKey";

	/**
	 * 是否已登录
	 * 
	 * @return
	 */
	public static boolean isAuthenticated() {
		Principal principal = (Principal) getSession().getAttribute(Principal.ATTRIBUTE_KEY);
		if (principal != null) {
			return true;
		}
		return false;
	}

	/**
	 * 登陆
	 * 
	 * @return
	 */
	public static void login(Principal principal) {
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		if (requestAttributes != null) {
			HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
			request.getSession().setAttribute(Principal.ATTRIBUTE_KEY, principal);
		}
	}

	/**
	 * 获取当前session
	 * 
	 * @return
	 */
	public static HttpSession getSession() {
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
		return request.getSession();
	}

}
