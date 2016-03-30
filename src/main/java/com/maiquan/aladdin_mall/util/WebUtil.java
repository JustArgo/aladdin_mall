package com.maiquan.aladdin_mall.util;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.maiquan.aladdin_mall.Principal;

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
	private static final long serialVersionUID = 283892857645407691L;
	/** 在session中登陆前请求地址的key */
	public static final String SAVE_REQUEST_KEY = "saveRequesKey";

	/**
	 * 获取当前用户身份信息，若未登录返回null
	 * 
	 * @return 身份信息，未登录为null
	 */
	public static Principal getCurrentPrincipal() {
		return (Principal) getSession().getAttribute(Principal.ATTRIBUTE_KEY);
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
