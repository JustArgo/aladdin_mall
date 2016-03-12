package com.maiquan.aladingfront.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.aladdin.interaction.wx.service.WxInteractionService;
import com.maiquan.aladingfront.Principal;
import com.maiquan.aladingfront.util.WebUtil;

import me.chanjar.weixin.common.api.WxConsts;

/**
 * 客户端过滤器
 * 
 * @author JSC
 *
 */
public class ClientInterceptor extends HandlerInterceptorAdapter {
	@Autowired
	private WxInteractionService wxInteractionService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		try {
			System.out.println("openId:"+((Principal)WebUtil.getSession().getAttribute(Principal.ATTRIBUTE_KEY)).getOpenId());
		} catch (Exception e) {
		}
		String ua = request.getHeader("user-agent").toLowerCase();
		boolean isWx = ua.indexOf("micromessenger") > 0;// 是否微信浏览器
		boolean isAuthenticated = WebUtil.isAuthenticated();
		if (!isAuthenticated) {
			if (isWx) {
				/* 跳转到自动登陆页面 */
				WebUtil.getSession().setAttribute(WebUtil.SAVE_REQUEST_KEY, request.getRequestURL());
				if(!response.isCommitted()){
					System.out.println("redirect to wx!!!!!!!!");
					response.sendRedirect(wxInteractionService.oauth2buildAuthorizationUrl(WxConsts.OAUTH2_SCOPE_BASE, null));
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
	}

}
