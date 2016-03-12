package com.aladdin.user.service;

import java.util.Map;

import com.aladdin.user.entity.WxUser;

/**
 * 用户中心服务
 * 
 * @author JSC
 *
 */
public interface UserService {
	/**
	 * 微信用户注册
	 * 
	 * @param uuid
	 *            日志标识
	 * @param wxOpenId
	 *            用户的微信openId
	 * @param wxUnionId
	 *            用户的微信unionId
	 * @param wxNickname
	 *            用户的微信昵称
	 * @return
	 */
	Map<String, Object> createWx(String uuid, String wxOpenId, String wxUnionId, String wxNickname);



	/**
	 * 查找微信用户
	 * 
	 * @param uuid
	 *            日志标识
	 * @param openId
	 *            微信openId
	 * @return
	 */
	WxUser findWxUser(String uuid, String openId);
}
