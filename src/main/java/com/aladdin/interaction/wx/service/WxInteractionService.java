package com.aladdin.interaction.wx.service;

import me.chanjar.weixin.mp.bean.result.WxMpUser;

/**
 * 微信交互
 * 
 * @author JSC
 *
 */
public interface WxInteractionService {
	/**
	 * 验证服务器地址的有效性
	 * 
	 * @param signature
	 *            微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
	 * @param timestamp
	 *            时间戳
	 * @param nonce
	 *            随机数
	 * @return 是否有效
	 */
	boolean checkSignature(String timestamp, String nonce, String signature);

	/**
	 * snsapi_base权限获取用户信息
	 * 
	 * @param code
	 * @return 微信用户信息
	 * @throws Exception
	 */
	WxMpUser getSnsapiBaseUserInfo(String code) throws Exception;

	/**
	 * snsapi_userinfo权限获取用户信息
	 * 
	 * @param code
	 * @return 微信用户信息
	 * @throws Exception
	 */
	WxMpUser getSnsapiUserInfo(String code) throws Exception;

	/**
	 * 构造微信oauth2授权的url连接
	 * 
	 * @param scope
	 * @param state
	 * @return url
	 */
	String oauth2buildAuthorizationUrl(String scope, String state);
}
