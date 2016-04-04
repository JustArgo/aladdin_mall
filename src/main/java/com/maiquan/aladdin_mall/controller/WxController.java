package com.maiquan.aladdin_mall.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.chanjar.weixin.mp.bean.result.WxMpUser;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aladdin.interaction.wx.service.WxInteractionService;
import com.aladdin.user.service.UserService;
import com.maiquan.aladdin_mall.Principal;
import com.maiquan.aladdin_mall.util.WebUtil;
import com.util.MapUtil;
import com.util.MapUtil.MapData;


/**
 * 微信验证接口
 * 
 * @author JSC
 *
 */
@Controller
@RequestMapping("/wx")
public class WxController {
	Logger logger=Logger.getLogger(this.getClass());
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
	public String checkSignature(String requestId,String signature, String timestamp, String nonce, String echostr) {
		if (wxInteractionService.checkSignature(UUID.randomUUID().toString(), timestamp, nonce, signature)) {
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
	public void login(String requestId,HttpServletResponse response, String code, String state) throws Exception {
		WxMpUser wxMpUser = wxInteractionService.getSnsapiBaseUserInfo(requestId, code);
		String openId = wxMpUser.getOpenId();
		String mqId=null;
		MapData data=MapUtil.newInstance(userService.findByOpenId(requestId, openId));
		if (data.getString("errcode").equals(UserService.FindByOpenIdErrcode.e0.getCode())) {
			mqId=data.getString("result");
			if(mqId==null){
				MapData data2=MapUtil.newInstance(userService.createWx(requestId,state, openId, null, null));
				if (data2.getString("errcode").equals(UserService.CreateWxErrcode.e0.getCode())) {
					mqId=data2.getString("result");
				}else {
					throw new Exception();
				}
			}
		}else{
			throw new Exception();
		}
		logger.info("");
		Principal principal = new Principal(mqId, openId);
		WebUtil.login(principal);
		response.sendRedirect(String.valueOf(WebUtil.getSession().getAttribute(WebUtil.SAVE_REQUEST_KEY)));
	}
	
	/**
	 * 初始化wx js配置 允许支付
	 * @return
	 */
	@RequestMapping(value="/config",method = RequestMethod.GET)
	@ResponseBody
	public Map<String,String> config(String requestId){
		
		Map<String,String> config = new HashMap<String,String>();
		
		config = wxInteractionService.getConfig(UUID.randomUUID().toString());
		
		return config;
		 
	}
	
}
