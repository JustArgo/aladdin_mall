package com.maiquan.aladdin_mall.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aladdin.interaction.wx.service.WxInteractionService;
import com.aladdin.user.entity.WxUser;
import com.aladdin.user.service.UserService;
import com.maiquan.aladdin_mall.Principal;
import com.maiquan.aladdin_mall.util.WebUtil;

import me.chanjar.weixin.mp.bean.result.WxMpUser;

/**
 * 微信验证接口
 * 
 * @author JSC
 *
 */
@Controller
@RequestMapping("/wx")
public class WxController {
	@Autowired
	private WxInteractionService wxInteractionService;
	@Autowired
	private UserService userService;

	/**
	 * 验证服务器地址的有效性
	 * 
	 * @param signature
	 *            微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
	 * @param timestamp
	 *            时间戳
	 * @param nonce
	 *            随机数
	 * @param echostr
	 *            随机字符串
	 * @return 有效则返回echostr
	 */
	@RequestMapping(value = "/checkSignature", method = RequestMethod.GET)
	@ResponseBody
	public String checkSignature(String signature, String timestamp, String nonce, String echostr) {
		if (wxInteractionService.checkSignature(timestamp, nonce, signature)) {
			return echostr;
		}
		return "invalid request";
	}

	/**
	 * 微信验证回调接口
	 * 
	 * @throws Exception
	 */
	@RequestMapping(value = "/callback", method = RequestMethod.GET)
	@ResponseBody
	public void login(HttpServletResponse response, String code, String state) throws Exception {
		WxMpUser wxMpUser = wxInteractionService.getSnsapiBaseUserInfo(code);
		String openId = wxMpUser.getOpenId();
		System.out.println("get the openId:" + openId);
		Principal principal = new Principal(null, openId);
		WebUtil.login(principal);
		response.sendRedirect(String.valueOf(WebUtil.getSession().getAttribute(WebUtil.SAVE_REQUEST_KEY)));
	}
}
