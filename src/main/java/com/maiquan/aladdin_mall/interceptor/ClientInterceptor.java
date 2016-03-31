package com.maiquan.aladdin_mall.interceptor;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.aladdin.interaction.wx.service.WxInteractionService;
import com.maiquan.aladdin_mall.Principal;
import com.maiquan.aladdin_mall.util.WebUtil;

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
		String ua = request.getHeader("user-agent").toLowerCase();
		boolean isWx = ua.indexOf("micromessenger") > 0;// 是否微信浏览器
		Principal principal = WebUtil.getCurrentPrincipal();
		if (principal==null) {
			if (isWx) {
				/* 跳转到自动登陆页面 */
				WebUtil.getSession().setAttribute(WebUtil.SAVE_REQUEST_KEY, request.getRequestURL());
				if(!response.isCommitted()){
					String requestId=UUID.randomUUID().toString().replace("-", "");
					String invitation=request.getParameter("invite");
					response.sendRedirect(wxInteractionService.oauth2buildAuthorizationUrl(requestId, WxConsts.OAUTH2_SCOPE_BASE, invitation));
					return false;
				}
			}
		}else{
			System.out.println("mqId:"+principal.getMqId());
			System.out.println("openId:"+principal.getOpenId());
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
