package com.maiquan.aladdin_mall;

import java.io.Serializable;

/**
 * 登陆用户身份信息
 * 
 * @author JSC
 *
 */
public class Principal implements Serializable {
	private static final long serialVersionUID = 7534104684362489251L;
	/** 在session中身份信息的key */
	public static final String ATTRIBUTE_KEY = "principal";

	/** 麦圈用户Id */
	private String mqId;
	/** 微信openId */
	private String openId;

	public Principal(String mqId, String openId) {
		this.mqId = mqId;
		this.openId = openId;
	}

	public String getMqId() {
		return mqId;
	}

	public void setMqId(String mqId) {
		this.mqId = mqId;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

}
